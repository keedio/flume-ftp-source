/*
 * KEEDIO
 */
package org.keedio.flume.source.ftp.source;

import java.util.*;

import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.PollableSource;
import org.apache.flume.conf.Configurable;
import org.apache.flume.event.SimpleEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.flume.ChannelException;

import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;

import org.keedio.flume.source.ftp.source.utils.FTPSourceEventListener;

import org.keedio.flume.source.ftp.metrics.SourceCounter;
import java.util.List;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.keedio.flume.source.ftp.client.factory.SourceFactory;
import org.keedio.flume.source.ftp.client.KeedioSource;
import java.nio.charset.Charset;
import org.apache.flume.source.AbstractSource;

/**
 *
 * @author luislazaro lalazaro@keedio.com - KEEDIO
 *
 */
public class Source extends AbstractSource implements Configurable, PollableSource {

    private SourceFactory sourceFactory = new SourceFactory();
    private KeedioSource keedioSource;

    private static final Logger log = LoggerFactory.getLogger(Source.class);
    private static final short ATTEMPTS_MAX = 3; //  max limit attempts reconnection
    private static final long EXTRA_DELAY = 10000;
    private int counterConnect = 0;
    private FTPSourceEventListener listener = new FTPSourceEventListener();
    private SourceCounter sourceCounter;

    /**
     * Request keedioSource to the factory
     *
     * @param context
     * @return KeedioSource
     */
    public KeedioSource orderKeedioSource(Context context) {
        keedioSource = sourceFactory.createKeedioSource(context);
        return keedioSource;
    }

    /**
     *
     * @param context
     */
    @Override
    public void configure(Context context) {
        keedioSource = orderKeedioSource(context);
        if (keedioSource.existFolder()) {
            keedioSource.makeLocationFile();
        } else {
            log.error("Folder " + keedioSource.getPathTohasmap().toString() + " not exists");
            System.exit(1);
        }
        keedioSource.connect();
        sourceCounter = new SourceCounter("SOURCE." + getName());
        keedioSource.checkPreviousMap();
    }

    /**
     * @return Status , process source configured from context
     * @throws org.apache.flume.EventDeliveryException
     */
    @Override
    public PollableSource.Status process() throws EventDeliveryException {
        try {
            log.info("Actual dir:  " + keedioSource.getDirectoryserver() + " files: "
                    + keedioSource.getFileList().size());
            discoverElements(keedioSource, keedioSource.getDirectoryserver(), "", 0);
            keedioSource.cleanList(); //clean list according existing actual files
            keedioSource.getExistFileList().clear();
        } catch (IOException e) {
            log.error("Exception thrown in proccess, try to reconnect " + counterConnect, e);

            if (!keedioSource.connect()) {
                counterConnect++;
            } else {
                keedioSource.checkPreviousMap();
            }

            if (counterConnect < ATTEMPTS_MAX) {
                process();
            } else {
                log.error("Server connection closed without indication, reached limit reconnections " + counterConnect);
                try {
                    Thread.sleep(keedioSource.getRunDiscoverDelay() + EXTRA_DELAY);
                    counterConnect = 0;
                } catch (InterruptedException ce) {
                    log.error("InterruptedException", ce);
                }
            }
        }
        keedioSource.saveMap();

        try {
            Thread.sleep(keedioSource.getRunDiscoverDelay());
            return PollableSource.Status.READY;     //source was successfully able to generate events
        } catch (InterruptedException inte) {
            log.error("Exception thrown in process while putting to sleep", inte);
            return PollableSource.Status.BACKOFF;   //inform the runner thread to back off for a bit
        }
    }

    /**
     * @return void
     */
    @Override
    public synchronized void start() {
        log.info("Starting Keedio source ...", this.getName());
        log.info("FTP Source {} starting. Metrics: {}", getName(), sourceCounter);
        super.start();
        sourceCounter.start();
    }

    /**
     * @return void
     */
    @Override
    public synchronized void stop() {
        keedioSource.saveMap();
        if (keedioSource.isConnected()) {
            keedioSource.disconnect();
        }
        sourceCounter.stop();
        super.stop();
    }

    /**
     * discoverElements: find files to process them
     *
     * @param <T>
     * @param keedioSource
     * @param parentDir, will be the directory retrieved by the server when
     * connected
     * @param currentDir, actual dir in the recursive method
     * @param level, deep to search
     * @throws IOException
     */
    // @SuppressWarnings("UnnecessaryContinue")
    public <T> void discoverElements(KeedioSource keedioSource, String parentDir, String currentDir, int level) throws IOException {

        long position = 0L;

        String dirToList = parentDir;
        if (!currentDir.equals("")) {
            dirToList += "/" + currentDir;
        }
        List<T> list = keedioSource.listElements(dirToList);
        if (list != null && list.size() > 0) {

            for (T element : list) {
                String elementName = keedioSource.getObjectName(element);
                if (elementName.equals(".") || elementName.equals("..")) {
                   // log.info("Skip parent directory and directory itself");
                    continue;
                }

                if (keedioSource.isDirectory(element)) {
                    log.info("[" + elementName + "]");
                    keedioSource.changeToDirectory(parentDir);

                    discoverElements(keedioSource, dirToList, elementName, level + 1);
                    continue;

                } else if (keedioSource.isFile(element)) { //aFile is a regular file
                    keedioSource.changeToDirectory(dirToList);
                    keedioSource.getExistFileList().add(dirToList + "/" + elementName);  //control of deleted files in server  

                    //test if file is new in collection
                    if (!(keedioSource.getFileList().containsKey(dirToList + "/" + elementName))) { //new file
                        sourceCounter.incrementFilesCount(); //include all files, even not yet processed
                        position = 0L;
                        log.info("Discovered: " + elementName + " ,size: " + keedioSource.getObjectSize(element));
                    } else { //known file
                        long prevSize = (long) keedioSource.getFileList().get(dirToList + "/" + elementName);
                        position = prevSize;
                        long dif = (keedioSource.getObjectSize(element) - (long) keedioSource.getFileList().get(dirToList + "/" + elementName));

                        if (dif > 0) {
                            log.info("Modified: " + elementName + " ,size: " + dif);
                        } else if (dif < 0) { //known and full modified
                            keedioSource.getExistFileList().remove(dirToList + "/" + elementName); //will be rediscovered as new file
                            keedioSource.saveMap();
                            continue;
                        } else {
                            continue;
                        }

                    } //end if known file

                    //common for all regular files
                    InputStream inputStream = null;
                    try {
                        inputStream = keedioSource.getInputStream(element);
                        listener.fileStreamRetrieved();

                        if (!readStream(inputStream, position)) {
                            inputStream = null;
                        }

                        boolean success = inputStream != null && keedioSource.particularCommand(); //mandatory if FTPClient
                        if (success) {
                            keedioSource.getFileList().put(dirToList + "/" + elementName, keedioSource.getObjectSize(element));
                            keedioSource.saveMap();

                            if (position != 0) {
                                sourceCounter.incrementCountModProc();
                            } else {
                                sourceCounter.incrementFilesProcCount();
                            }

                            log.info("Processed:  " + elementName + " ,total files: " + this.keedioSource.getFileList().size() + "\n");

                        } else {
                            handleProcessError(elementName);
                        }
                    } catch (IOException e) {
                        handleProcessError(elementName);
                        log.error("Failed retrieving inputStream on discoverElements ", e);
                        continue;
                    }

                    keedioSource.changeToDirectory(dirToList);
                    continue;

                    //end condition for a regular file
                } else if (keedioSource.isLink(element)) {
                    log.info(elementName + " is a link of " + this.keedioSource.getLink(element) + " could not retrieve size");
                    keedioSource.changeToDirectory(dirToList);
                    continue;
                } else {
                    log.info(elementName + " unknown type of file");
                    keedioSource.changeToDirectory(dirToList);
                    continue;
                }

            } //fin de recorrido de elementos devueltos
        } //el listado no es vacío
    }//fin de método

    /**
     * Read retrieved stream from ftpclient into byte[] and process. If
     * flushlines is true the retrieved inputstream will be readed by lines. And
     * the type of file is set to ASCII from KeedioSource.
     *
     * @return boolean
     * @param inputStream
     * @param position
     */
    public boolean readStream(InputStream inputStream, long position) {
        if (inputStream == null) {
            return false;
        }

        boolean successRead = true;

        if (keedioSource.isFlushLines()) {
            try {
                inputStream.skip(position);
                try (BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, Charset.defaultCharset()))) {
                    String line = null;
                    
                    while ((line = in.readLine()) != null) {
                        processMessage(line.getBytes());
                    }
                    
                }
                inputStream.close();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                successRead = false;
            }
        } else {

            try {
                inputStream.skip(position);
                int CHUNKSIZE = keedioSource.getChunkSize();
                byte[] bytesArray = new byte[CHUNKSIZE];
                int bytesRead = -1;
                while ((bytesRead = inputStream.read(bytesArray)) != -1) {
                    try (ByteArrayOutputStream baostream = new ByteArrayOutputStream(CHUNKSIZE)) {
                        baostream.write(bytesArray, 0, bytesRead);
                        byte[] data = baostream.toByteArray();
                        processMessage(data);
                    }
                }

                inputStream.close();
            } catch (IOException e) {
                log.error("on readStream", e);
                successRead = false;

            }
        }
        return successRead;
    }

    /**
     * @void process last appended data to files
     * @param lastInfo byte[]
     */
    public void processMessage(byte[] lastInfo) {
        byte[] message = lastInfo;
        Event event = new SimpleEvent();
        Map<String, String> headers = new HashMap<>();
        headers.put("timestamp", String.valueOf(System.currentTimeMillis()));
        event.setBody(message);
        event.setHeaders(headers);
        try {
            getChannelProcessor().processEvent(event);
        } catch (ChannelException e) {
            log.error("ChannelException", e);
        }
        sourceCounter.incrementCountSizeProc(message.length);
        sourceCounter.incrementEventCount();
    }

    /**
     * @param listener
     */
    public void setListener(FTPSourceEventListener listener) {
        this.listener = listener;
    }

    /**
     * @param fileName
     */
    public void handleProcessError(String fileName) {
        log.info("failed retrieving stream from file, will try in next poll :" + fileName);
        sourceCounter.incrementFilesProcCountError();
    }

    /**
     *
     * @param ftpSourceCounter
     */
    public void setFtpSourceCounter(SourceCounter ftpSourceCounter) {
        this.sourceCounter = ftpSourceCounter;
    }

    /**
     * @return KeedioSource
     */
    public KeedioSource getKeedioSource() {
        return keedioSource;
    }

} //endclass
