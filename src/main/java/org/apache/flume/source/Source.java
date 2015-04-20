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

import org.apache.flume.client.sources.SFTPSource;
import org.apache.flume.client.sources.FTPSSource;
import org.apache.flume.client.sources.FTPSource;
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

import org.apache.flume.source.AbstractSource;
import org.apache.flume.source.utils.FTPSourceEventListener;


import org.apache.flume.metrics.FtpSourceCounter;
import java.util.List;



import com.foundationdb.tuple.ByteArrayUtil;
import org.apache.flume.client.KeedioSource;

/*
 * @author Luis Lazaro // lalazaro@keedio.com
 KEEDIO
 */
public class Source extends AbstractSource implements Configurable, PollableSource {

    private KeedioSource keedioSource;

    private static final Logger log = LoggerFactory.getLogger(Source.class);
    private static final int CHUNKSIZE = 1024;   //event size in bytes
    private static final short ATTEMPTS_MAX = 3; //  max limit attempts reconnection
    private static final long EXTRA_DELAY = 10000;
    private static int COUNTER = 0;
    private FTPSourceEventListener listener = new FTPSourceEventListener();
    private FtpSourceCounter ftpSourceCounter;

    @Override
    public void configure(Context context) {
        keedioSource = initSource(context);
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
                    + keedioSource.getSizeFileList().size());
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

    @Override
    /**
     * @return void
     */
    public void start() {
        log.info("Starting Keedio source ...", this.getName());
        log.info("FTP Source {} starting. Metrics: {}", getName(), ftpSourceCounter);
        super.start();
        ftpSourceCounter.start();
    }

    @Override
    /**
     * @return void
     */
    public void stop() {
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
     * @return void
     * @param KeedioSource, String, int
     */
    // @SuppressWarnings("UnnecessaryContinue")
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
                    //String fileName = aFile.getName();
                    if (!(keedioSource.getSizeFileList().containsKey(dirToList + "/" + currentFileName))) { //new file
                        ftpSourceCounter.incrementFilesCount();
                        InputStream inputStream = null;
                        try {
                            inputStream = keedioSource.getInputStream(aFile);
                            listener.fileStreamRetrieved();
                            readStream(inputStream, 0);
                            boolean success = inputStream != null && keedioSource.particularCommand(); //mandatory if FTPClient
                            if (success) {
                                keedioSource.getSizeFileList().put(dirToList + "/" + currentFileName, keedioSource.getObjectSize(aFile));
                                keedioSource.saveMap();
                                ftpSourceCounter.incrementFilesProcCount();
                                log.info("discovered: " + currentFileName + " ," + this.keedioSource.getSizeFileList().size());
                            } else {
                                handleProcessError(currentFileName);
                            }
                        } catch (IOException e) {
                            handleProcessError(currentFileName);
                            log.error("Ftp server closed connection ", e);
                            continue;
                        }

                        keedioSource.changeToDirectory(dirToList);
                        //ftpClient.changeWorkingDirectory(dirToList);
                        continue;

                    } else { //known file
                        long dif = keedioSource.getObjectSize(aFile)
                                - keedioSource.getSizeFileList().get(dirToList + "/" + currentFileName);
                        //long dif = aFile.getSize() - sizeFileList.get(dirToList + "/" + aFile.getName());
                        if (dif > 0) { //known and modified
                            long prevSize = keedioSource.getSizeFileList().get(dirToList + "/" + currentFileName);
                            InputStream inputStream = null;
                            try {
                                inputStream = keedioSource.getInputStream(aFile);
                                listener.fileStreamRetrieved();
                                readStream(inputStream, prevSize);

                                boolean success = inputStream != null && keedioSource.particularCommand(); //mandatory if FTPClient
                                if (success) {
                                    keedioSource.getSizeFileList().put(dirToList + "/" + currentFileName, keedioSource.getObjectSize(aFile));
                                    keedioSource.saveMap();
                                    ftpSourceCounter.incrementCountModProc();
                                    log.info("modified: " + currentFileName + " ," + this.keedioSource.getSizeFileList().size());
                                } else {
                                    handleProcessError(currentFileName);
                                }
                            } catch (FTPConnectionClosedException e) {
                                log.error("Ftp server closed connection ", e);
                                handleProcessError(currentFileName);
                                continue;
                            }
                            keedioSource.changeToDirectory(dirToList);
                            //ftpClient.changeWorkingDirectory(dirToList);
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
     * read retrieved stream from ftpclient into byte[] and process
     *
     * @return boolean
     * @param inputStream, position
     */
    public boolean readStream(InputStream inputStream, long position) {
        if (inputStream == null) {
            return false;
        }

        boolean successRead = true;
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
                    byte[] data_mutate = ByteArrayUtil.replace(data, CHUNKSIZE - 1,0, carriageB, emptyB);
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
        return successRead;
    }

    /**
     * @void process last appended data to files
     * @param lastInfo byte[]
     */
    public synchronized void processMessage(byte[] lastInfo) {
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
     * @param context of the source
     * @return KeedioSource
     */
    public KeedioSource initSource(Context context) {
        switch (context.getString("client.source")) {
            case "ftp":
                keedioSource = new FTPSource();
                initCommonParam(context);
                break;
            case "sftp":
                keedioSource = new SFTPSource();
                SFTPSource sftpSource = new SFTPSource();
                sftpSource.setKnownHosts(context.getString("knownHosts"));
                keedioSource = sftpSource;
                initCommonParam(context);
                break;
            case "ftps":
                keedioSource = new FTPSSource();
                FTPSSource ftpsSource = new FTPSSource();
                ftpsSource.setProtocolSec(context.getString("security.cipher"));
                ftpsSource.setSecurityMode(context.getBoolean("security.enabled"));
                ftpsSource.setSecurityCert(context.getBoolean("security.certificate.enabled"));
                keedioSource = ftpsSource;
                initCommonParam(context);
                break;
            default:
                log.error("Source not found in context");
                System.exit(1);
        }
        return keedioSource;
    }

    /**
     * @void initialize common parameters for all sources
     * @param context of source
     */
    public void initCommonParam(Context context) {
        keedioSource.setBufferSize(context.getInteger("buffer.size"));
        keedioSource.setServer(context.getString("name.server"));
        keedioSource.setUser(context.getString("user"));
        keedioSource.setPassword(context.getString("password"));
        keedioSource.setRunDiscoverDelay(context.getInteger("run.discover.delay"));
        keedioSource.setWorkingDirectory(context.getString("working.directory"));
        keedioSource.setPort(context.getInteger("port"));
        keedioSource.setFolder(context.getString("folder"));
        keedioSource.setFileName(context.getString("file.name"));
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
    public KeedioSource getKeedioSource(){
        return keedioSource;
    }
} //endclass
