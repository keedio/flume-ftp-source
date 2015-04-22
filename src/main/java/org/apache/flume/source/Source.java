/**
 * *****************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * *****************************************************************************
 */
package org.apache.flume.source;

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
import org.apache.commons.net.ftp.FTPConnectionClosedException;

import org.apache.flume.source.utils.FTPSourceEventListener;

import org.apache.flume.metrics.FtpSourceCounter;
import java.util.List;

import com.foundationdb.tuple.ByteArrayUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.flume.client.factory.SourceFactory;
import org.apache.flume.client.KeedioSource;

/**
 *
 * @author luislazaro lalazaro@keedio.com - KEEDIO
 *
 */
public class Source extends AbstractSource implements Configurable, PollableSource {

    private SourceFactory sourceFactory = new SourceFactory();
    private KeedioSource keedioSource;

    private static final Logger log = LoggerFactory.getLogger(Source.class);
    private static final int CHUNKSIZE = 1024;  //event size in bytes
    private static final short ATTEMPTS_MAX = 3; //  max limit attempts reconnection
    private static final long EXTRA_DELAY = 10000;
    private static int COUNTER = 0;
    private FTPSourceEventListener listener = new FTPSourceEventListener();
    private FtpSourceCounter ftpSourceCounter;

    /**
     * Request keedioSource to the factory
     *
     * @param context
     * @return
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
        ftpSourceCounter = new FtpSourceCounter("SOURCE." + getName());
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
            log.error("Exception thrown in proccess, try to reconnect " + COUNTER, e);

            if (!keedioSource.connect()) {
                COUNTER++;
            } else {
                keedioSource.checkPreviousMap();
            }

            if (COUNTER < ATTEMPTS_MAX) {
                process();
            } else {
                log.error("Server connection closed without indication, reached limit reconnections " + COUNTER);
                try {
                    Thread.sleep(keedioSource.getRunDiscoverDelay() + EXTRA_DELAY);
                    COUNTER = 0;
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
        log.info("FTP Source {} starting. Metrics: {}", getName(), ftpSourceCounter);
        super.start();
        ftpSourceCounter.start();
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
        ftpSourceCounter.stop();
        super.stop();
    }

    /**
     * discoverElements: find files to process them
     *
     * @param keedioSource
     * @param parentDir, will be the directory retrieved by the server when
     * connected
     * @param currentDir, actual dir int the recursive method
     * @param level, deep to search
     * @throws IOException
     */
    @SuppressWarnings("UnnecessaryContinue")
    public void discoverElements(KeedioSource keedioSource, String parentDir, String currentDir, int level) throws IOException {

        String dirToList = parentDir;
        if (!currentDir.equals("")) {
            dirToList += "/" + currentDir;
        }
        List<Object> list = keedioSource.listFiles(dirToList);
        if (list != null && list.size() > 0) {

            for (Object aFile : list) {
                String currentFileName = keedioSource.getObjectName(aFile);
                if (currentFileName.equals(".") || currentFileName.equals("..")) {
                    log.info("Skip parent directory and directory itself");
                    continue;
                }

                if (keedioSource.isDirectory(aFile)) {
                    log.info("[" + currentFileName + "]");
                    keedioSource.changeToDirectory(parentDir);

                    discoverElements(keedioSource, dirToList, currentFileName, level + 1);
                    continue;

                } else if (keedioSource.isFile(aFile)) { //aFile is a regular file
                    keedioSource.changeToDirectory(dirToList);
                    keedioSource.getExistFileList().add(dirToList + "/" + currentFileName);  //control of deleted files in server                    
                    if (!(keedioSource.getFileList().containsKey(dirToList + "/" + currentFileName))) { //new file
                        ftpSourceCounter.incrementFilesCount();
                        InputStream inputStream = null;
                        try {
                            inputStream = keedioSource.getInputStream(aFile);
                            listener.fileStreamRetrieved();
                            readStream(inputStream, 0);
                            boolean success = inputStream != null && keedioSource.particularCommand(); //mandatory if FTPClient
                            if (success) {
                                keedioSource.getFileList().put(dirToList + "/" + currentFileName, keedioSource.getObjectSize(aFile));
                                keedioSource.saveMap();
                                ftpSourceCounter.incrementFilesProcCount();
                                log.info("discovered: " + currentFileName + " ," + this.keedioSource.getFileList().size());
                            } else {
                                handleProcessError(currentFileName);
                            }
                        } catch (IOException e) {
                            handleProcessError(currentFileName);
                            log.error("Ftp server closed connection ", e);
                            continue;
                        }

                        keedioSource.changeToDirectory(dirToList);
                        continue;

                    } else { //known file
                        long dif = (keedioSource.getObjectSize(aFile) - keedioSource.getFileList().get(dirToList + "/" + currentFileName));
                        if (dif > 0) { //known and modified
                            long prevSize = keedioSource.getFileList().get(dirToList + "/" + currentFileName);
                            InputStream inputStream = null;
                            try {
                                inputStream = keedioSource.getInputStream(aFile);
                                listener.fileStreamRetrieved();
                                readStream(inputStream, prevSize);

                                boolean success = inputStream != null && keedioSource.particularCommand(); //mandatory if FTPClient
                                if (success) {
                                    keedioSource.getFileList().put(dirToList + "/" + currentFileName, keedioSource.getObjectSize(aFile));
                                    keedioSource.saveMap();
                                    ftpSourceCounter.incrementCountModProc();
                                    log.info("modified: " + currentFileName + " ," + this.keedioSource.getFileList().size());
                                } else {
                                    handleProcessError(currentFileName);
                                }
                            } catch (FTPConnectionClosedException e) {
                                log.error("Ftp server closed connection ", e);
                                handleProcessError(currentFileName);
                                continue;
                            }
                            keedioSource.changeToDirectory(dirToList);
                            continue;
                        } else if (dif < 0) { //known and full modified
                            keedioSource.getExistFileList().remove(dirToList + "/" + currentFileName); //will be rediscovered as new file
                            keedioSource.saveMap();
                            continue;
                        }
                        keedioSource.changeToDirectory(dirToList);
                        continue;
                    }
                } else if (keedioSource.isLink(aFile)) {
                    log.info(currentFileName + " is a link of " + this.keedioSource.getLink(aFile) + " access denied");
                    keedioSource.changeToDirectory(dirToList);
                    continue;
                } else {
                    log.info(currentFileName + " unknown type of file");
                    keedioSource.changeToDirectory(dirToList);
                    continue;
                }

            } //fin de bucle
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
     * @see
     * <a href="https://foundationdb.com/key-value-store/documentation/javadoc/index.html?com/foundationdb/tuple/ByteArrayUtil.html">ByteArrayUtil</a>
     */
    public boolean readStream(InputStream inputStream, long position) {
        if (inputStream == null) {
            return false;
        }

        boolean successRead = true;

        if (keedioSource.isFlushLines()) {
            try {
                inputStream.skip(position);
                BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
                String line = null;

                while ((line = in.readLine()) != null) {
                    processMessage(line.getBytes());
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                successRead = false;
            }
        } else {

            try {
                inputStream.skip(position);
                byte[] bytesArray = new byte[CHUNKSIZE];
                int bytesRead = -1;
                while ((bytesRead = inputStream.read(bytesArray)) != -1) {
                    try (ByteArrayOutputStream baostream = new ByteArrayOutputStream(CHUNKSIZE)) {
                        baostream.write(bytesArray, 0, bytesRead);
                        byte[] data = baostream.toByteArray();
                        String carriage = System.getProperty("line.separator");
                        byte[] carriageB = carriage.getBytes();
                        String emptyString = "";
                        byte[] emptyB = emptyString.getBytes();
                        byte[] data_mutate = ByteArrayUtil.replace(data, CHUNKSIZE - 1, 0, carriageB, emptyB);
                        processMessage(data_mutate);
                        data = null;
                        data_mutate = null;
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
        ftpSourceCounter.incrementFilesProcCountError();
    }

    /**
     *
     * @param ftpSourceCounter
     */
    public void setFtpSourceCounter(FtpSourceCounter ftpSourceCounter) {
        this.ftpSourceCounter = ftpSourceCounter;
    }

    /**
     * @return KeedioSource
     */
    public KeedioSource getKeedioSource() {
        return keedioSource;
    }
} //endclass
