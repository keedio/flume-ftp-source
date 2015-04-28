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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.PollableSource;
import org.apache.flume.conf.Configurable;
import org.apache.flume.event.SimpleEvent;
import org.apache.flume.source.utils.FTPSourceEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.flume.ChannelException;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ByteArrayOutputStream;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;


/*
 * @author Luis Lazaro // lalazaro@keedio.com
 KEEDIO
 */
public class FTPSource extends AbstractSource implements Configurable, PollableSource {

    private static final Logger log = LoggerFactory.getLogger(FTPSource.class);
    private Map<String, Long> sizeFileList = new HashMap<>();
    private Set<String> existFileList = new HashSet<>();
    private static final int CHUNKSIZE = 1024;   //event size in bytes
    private static final short ATTEMPTS_MAX = 3; //  max limit attempts reconnection
    private static final long EXTRA_DELAY = 10000;
    private FTPSourceUtils ftpSourceUtils;
    private FtpSourceCounter ftpSourceCounter;
    private Path pathTohasmap = Paths.get("");
    private Path hasmap = Paths.get("");
    private Path absolutePath = Paths.get("");
    private int counter = 0;

    public void setListener(FTPSourceEventListener listener) {
        this.listener = listener;
    }

    private FTPSourceEventListener listener = new FTPSourceEventListener();

    @Override
    public void configure(Context context) {
        ftpSourceUtils = new FTPSourceUtils(context);
        pathTohasmap = Paths.get(ftpSourceUtils.getFolder());
        if (checkFolder()) {
            hasmap = Paths.get(ftpSourceUtils.getFileName());
            absolutePath = Paths.get(pathTohasmap.toString(), hasmap.toString());
        } else {
            log.error("Folder " + pathTohasmap.toString() + " not exists");
            System.exit(1);
        }
        ftpSourceUtils.connectToserver();
        ftpSourceCounter = new FtpSourceCounter("SOURCE." + getName());
        checkPreviousMap(Paths.get(pathTohasmap.toString(), hasmap.toString()));
    }

    /*
     @enum Status , process source configured from context
     */
    @Override
    public PollableSource.Status process() throws EventDeliveryException {

        try {
            log.info("Actual dir: "
                    + ftpSourceUtils.getFtpClient().printWorkingDirectory() + " files proccesed: " + sizeFileList.size());
            discoverElements(ftpSourceUtils.getFtpClient(), ftpSourceUtils.getFtpClient().printWorkingDirectory(), "", 0);
            cleanList(sizeFileList); //clean list according existing actual files
            existFileList.clear();
        } catch (IOException e) {
            log.error("Exception thrown in process, try to reconnect " + counter, e);

            if (!ftpSourceUtils.connectToserver()) {
                counter++;
            } else {
                checkPreviousMap(Paths.get(pathTohasmap.toString(), hasmap.toString()));
            }

            if (counter < ATTEMPTS_MAX) {
                process();
            } else {
                log.error("Server connection closed without indication, reached limit reconnections " + counter);
                try {
                    Thread.sleep(ftpSourceUtils.getRunDiscoverDelay() + EXTRA_DELAY);
                    counter = 0;
                } catch (InterruptedException ce) {
                    log.error("InterruptedException", ce);
                }
            }
        }

        //saveMap(sizeFileList);
        try {
            Thread.sleep(ftpSourceUtils.getRunDiscoverDelay());
            return PollableSource.Status.READY;     //source was successfully able to generate events
        } catch (InterruptedException inte) {
            log.error("Exception thrown in process while putting to sleep", inte);
            return PollableSource.Status.BACKOFF;   //inform the runner thread to back off for a bit		
        }
    }

    @Override
    public void start() {
        log.info("Starting ftp source {} ...", getName());
        log.info("FTP Source {} starting. Metrics: {}", getName(), ftpSourceCounter);
        super.start();
        ftpSourceCounter.start();
    }

    @Override
    public void stop() {
        saveMap(sizeFileList);
        try {
            if (ftpSourceUtils.getFtpClient().isConnected()) {
                ftpSourceUtils.getFtpClient().logout();
                ftpSourceUtils.getFtpClient().disconnect();
            }
            ftpSourceCounter.stop();
            log.info("FTP Source " + this.getName() + " stopped. Metrics: {}", getName(), ftpSourceCounter);
            super.stop();
        } catch (IOException ex) {
            log.error("Exception thrown stoping proccess", ex);
        }

    }

    /*
     @void process last append to files
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
        ftpSourceCounter.incrementEventCount();
    }

    /*
     discoverElements: find files to process them
     @return void 
     */
    @SuppressWarnings("UnnecessaryContinue")
    public void discoverElements(FTPClient ftpClient, String parentDir, String currentDir, int level) throws IOException {

        String dirToList = parentDir;
        if (!currentDir.equals("")) {
            dirToList += "/" + currentDir;
        }
        FTPFile[] subFiles = ftpClient.listFiles(dirToList);
        if (subFiles != null && subFiles.length > 0) {

            for (FTPFile aFile : subFiles) {
                String currentFileName = aFile.getName();
                if (currentFileName.equals(".") || currentFileName.equals("..")) {
                    log.info("Skip parent directory and directory itself");
                    continue;
                }

                if (aFile.isDirectory()) {
                    log.info("[" + aFile.getName() + "]");
                    ftpClient.changeWorkingDirectory(parentDir);
                    discoverElements(ftpClient, dirToList, aFile.getName(), level + 1);
                    continue;
                } else if (aFile.isFile()) { //aFile is a regular file
                    ftpClient.changeWorkingDirectory(dirToList);
                    existFileList.add(dirToList + "/" + aFile.getName());  //control of deleted files in server
                    String fileName = aFile.getName();
                    if (!(sizeFileList.containsKey(dirToList + "/" + aFile.getName()))) { //new file
                        ftpSourceCounter.incrementFilesCount();
                        InputStream inputStream = null;
                        try {
                            inputStream = ftpClient.retrieveFileStream(aFile.getName());
                            listener.fileStreamRetrieved();
                            readStream(inputStream, 0);
                            boolean success = inputStream != null && ftpClient.completePendingCommand(); //mandatory
                            if (success) {
                                sizeFileList.put(dirToList + "/" + aFile.getName(), aFile.getSize());
                                saveMap(sizeFileList);
                                ftpSourceCounter.incrementFilesProcCount();
                                log.info("discovered: " + fileName + " ," + sizeFileList.size());
                            } else {
                                handleProcessError(fileName);
                            }
                        } catch (FTPConnectionClosedException e) {
                            handleProcessError(fileName);
                            log.error("Ftp server closed connection ", e);
                            continue;
                        }

                        ftpClient.changeWorkingDirectory(dirToList);
                        continue;

                    } else { //known file                        
                        long dif = aFile.getSize() - sizeFileList.get(dirToList + "/" + aFile.getName());
                        if (dif > 0) { //known and modified
                            long prevSize = sizeFileList.get(dirToList + "/" + aFile.getName());
                            InputStream inputStream = null;
                            try {
                                inputStream = ftpClient.retrieveFileStream(aFile.getName());
                                listener.fileStreamRetrieved();
                                readStream(inputStream, prevSize);

                                boolean success = inputStream != null && ftpClient.completePendingCommand(); //mandatory
                                if (success) {
                                    sizeFileList.put(dirToList + "/" + aFile.getName(), aFile.getSize());
                                    saveMap(sizeFileList);
                                    ftpSourceCounter.incrementCountModProc();
                                    log.info("modified: " + fileName + " ," + sizeFileList.size());
                                } else {
                                    handleProcessError(fileName);
                                }
                            } catch (FTPConnectionClosedException e) {
                                log.error("Ftp server closed connection ", e);
                                handleProcessError(fileName);
                                continue;
                            }

                            ftpClient.changeWorkingDirectory(dirToList);
                            continue;
                        } else if (dif < 0) { //known and full modified
                            existFileList.remove(dirToList + "/" + aFile.getName()); //will be rediscovered as new file
                            saveMap(sizeFileList);
                            continue;
                        }
                        ftpClient.changeWorkingDirectory(parentDir);
                        continue;
                    }
                } else if (aFile.isSymbolicLink()) {
                    log.info(aFile.getName() + " is a link of " + aFile.getLink() + " access denied");
                    ftpClient.changeWorkingDirectory(parentDir);
                    continue;
                } else if (aFile.isUnknown()) {
                    log.info(aFile.getName() + " unknown type of file");
                    ftpClient.changeWorkingDirectory(parentDir);
                    continue;
                } else {
                    ftpClient.changeWorkingDirectory(parentDir);
                    continue;
                }

            } //fin de bucle
        } //el listado no es vacío
    }//fin de método

    private void handleProcessError(String fileName) {
        log.info("failed retrieving stream from file, will try in next poll :" + fileName);
        ftpSourceCounter.incrementFilesProcCountError();
    }

    /*
     @void Serialize hashmap
     */
    public void saveMap(Map<String, Long> map) {
        try {
            FileOutputStream fileOut = new FileOutputStream(absolutePath.toString());
            try (ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
                out.writeObject((HashMap) map);
            }
        } catch (FileNotFoundException e) {
            log.error("Error saving map File", e);
        } catch (IOException e) {
            log.error("Error saving map IO:", e);
        }
    }

    /*
     @return HashMap<String,Long> objects
     */
    public Map<String, Long> loadMap(String name) throws ClassNotFoundException, IOException {
        FileInputStream map = new FileInputStream(name);
        HashMap hasMap;
        try (ObjectInputStream in = new ObjectInputStream(map)) {
            hasMap = (HashMap) in.readObject();
        }
        return hasMap;
    }

    /*
     @void, delete file from hashmaps if deleted from ftp
     */
    public void cleanList(Map<String, Long> map) {
        for (Iterator<String> iter = map.keySet().iterator(); iter.hasNext();) {
            final String fileName = iter.next();
            if (!(existFileList.contains(fileName))) {
                iter.remove();
            }
        }
    }

    /*
     read retrieved stream from ftpclient into byte[] and process
     @return void
     */
    public boolean readStream(InputStream inputStream, long position) {
        if (inputStream == null) {
            return false;
        }

        boolean successRead = true;

        if (ftpSourceUtils.isFlushLines()) {
            try {
                inputStream.skip(position);
                BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
                String line = null;

                while ((line = in.readLine()) != null) {
                    processMessage(line.getBytes());
                }
                inputStream.close();
                in.close();
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
                        processMessage(data);
                        data = null;
                    }
                }

                inputStream.close();
            } catch (IOException e) {
                log.error("on readStream", e);
                successRead = false;
            }
        }
        //end condition for only byte

        return successRead;
    }

    public void setFtpSourceCounter(FtpSourceCounter ftpSourceCounter) {
        this.ftpSourceCounter = ftpSourceCounter;
    }

    /*
     @return void, check if there are previous files to load
     */
    public void checkPreviousMap(Path file1) {
        try {
            if (Files.exists(file1)) {
                sizeFileList = loadMap(file1.toString());
                log.info("Found previous map of files flumed");
            } else {
                log.info("Not found preivous map of files flumed");

            }

        } catch (IOException | ClassNotFoundException e) {
            log.info("Exception thrown checking previous map ", e);
        }
    }

    /*
     return boolean, folder exists
     */
    public boolean checkFolder() {
        boolean folderExits = false;
        if (Files.exists(pathTohasmap)) {
            folderExits = true;
        }
        return folderExits;
    }

    public FTPClient getFTPClient() {
        return ftpSourceUtils.getFtpClient();
    }

} //end of class

