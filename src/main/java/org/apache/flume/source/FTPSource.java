/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package org.apache.flume.source;



import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.*;

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


import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ByteArrayOutputStream;


import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

/*
 * @author Luis Lazaro // lalazaro@keedio.com
    KEEDIO
 */
public class FTPSource extends AbstractSource implements Configurable, PollableSource {
    
    private static final Logger log = LoggerFactory.getLogger(FTPSource.class);
    private HashMap<String, Long> sizeFileList = new HashMap<>();
    private HashSet<String> existFileList = new HashSet<>();
    private final int CHUNKSIZE = 1024;   //event size in bytes
    private FTPSourceUtils ftpSourceUtils;
    private long eventCount;
    private FtpSourceCounter ftpSourceCounter;

    public void setListener(FTPSourceEventListener listener) {
        this.listener = listener;
    }

    private FTPSourceEventListener listener = new FTPSourceEventListener();

    @Override
    public void configure(Context context) {            
        ftpSourceUtils = new FTPSourceUtils(context);
        ftpSourceUtils.connectToserver();
        ftpSourceCounter = new FtpSourceCounter("SOURCE." + getName());       
        
       try {
            sizeFileList = loadMap("hasmap.ser");
            eventCount = loadCount("eventCount.ser");
       } catch(IOException | ClassNotFoundException e) {
            log.error("Exception thrown in configure ", e);
       }
    }
    
    /*
    @enum Status , process source configured from context
    */
    @Override
    public PollableSource.Status process() throws EventDeliveryException {
         
            try {
                  log.info("data processed: " + eventCount/1024  + " MBytes" + " actual dir " + 
                   ftpSourceUtils.getFtpClient().printWorkingDirectory() + " files : " +
                  sizeFileList.size());
                  discoverElements(ftpSourceUtils.getFtpClient(),ftpSourceUtils.getFtpClient().printWorkingDirectory(), "", 0);           
                } catch(IOException e){
                    log.error("Exception thrown in process ", e);
                }
       
                cleanList(sizeFileList);
                existFileList.clear();
                
                saveMap(sizeFileList);
                saveCount(eventCount);

        try
        {  
            Thread.sleep(ftpSourceUtils.getRunDiscoverDelay());
            return PollableSource.Status.READY;     //source was successfully able to generate events
        } catch(InterruptedException inte){
            log.error("Exception thrown in process while puttin to sleep", inte);
            return PollableSource.Status.BACKOFF;   //inform the runner thread to back off for a bit		
        }
    }

 
    @Override
    public void start() {
        log.info("Starting sql source {} ...", getName());
        log.info("FTP Source {} stopped. Metrics: {}", getName(), ftpSourceCounter);
        super.start();
       ftpSourceCounter.start();
    }
    

    @Override
    public void stop() {
        saveMap(sizeFileList);
        saveCount(eventCount);
            try {
                if (ftpSourceUtils.getFtpClient().isConnected()) {
                    ftpSourceUtils.getFtpClient().logout();
                    ftpSourceUtils.getFtpClient().disconnect();
                }
            } catch (IOException ex) {
                log.error("Exception thrown stoping proccess", ex);
            }
            ftpSourceCounter.stop();
            log.info("FTP Source {} stopped. Metrics: {}", getName(), ftpSourceCounter);
            super.stop();
    }
    
    
    /*
    @void process last append to files
    */
    public void processMessage(byte[] lastInfo){
        byte[] message = lastInfo;
        Event event = new SimpleEvent();
        Map<String, String> headers =  new HashMap<>();  
        headers.put("timestamp", String.valueOf(System.currentTimeMillis()));
        event.setBody(message);
        event.setHeaders(headers);
        getChannelProcessor().processEvent(event);
    }
    
    
    /*
    discoverElements: find files to process them
    @return void 
    */
    @SuppressWarnings("UnnecessaryContinue")
    public void discoverElements( FTPClient ftpClient, String parentDir, String currentDir, int level) throws IOException {
        
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
                    existFileList.add(dirToList + "/" + aFile.getName());
                    final String fileName = aFile.getName();
                    
                    if (!(sizeFileList.containsKey(dirToList + "/" + aFile.getName()))){ //new file
                       ftpSourceCounter.incrementFilesCount();
                        sizeFileList.put(dirToList + "/" + aFile.getName(), aFile.getSize());
                        saveMap(sizeFileList);
                        final InputStream inputStream = ftpClient.retrieveFileStream(aFile.getName());

                        listener.fileStreamRetrieved();
                        if (inputStream != null) {
                        Thread threadNewFile = new Thread( new Runnable(){
                                    @Override
                                    public void run(){
                                        readStream(inputStream, "discovered: " + fileName, 0 );
                                    }
                                });
                                    threadNewFile.setName("hiloNewFile_" + aFile.getName());
                                    threadNewFile.start();

                            boolean success = ftpClient.completePendingCommand();
                            ftpSourceCounter.incrementFilesProcCount();
                        } else {
                            log.info("failed retrieving stream from file :" + fileName);
                            existFileList.remove(dirToList + "/" + aFile.getName());
                            cleanList(sizeFileList);
                            ftpSourceCounter.incrementFilesProcCountError();
                        }
                        ftpClient.changeWorkingDirectory(dirToList);
                        continue;
                        
                        
                    } else  { //known file                        
                        long dif = aFile.getSize() - sizeFileList.get(dirToList + "/" + aFile.getName());
                        if (dif > 0 ){ //known and modified
                            final InputStream inputStream = ftpClient.retrieveFileStream(aFile.getName());
                            final long prevSize = sizeFileList.get(dirToList + "/" + aFile.getName());
                            sizeFileList.put(dirToList + "/" + aFile.getName(), aFile.getSize()); //save new size
                            saveMap(sizeFileList);
                            listener.fileStreamRetrieved();

                            if (inputStream != null) {
                            Thread threadOldFile = new Thread( new Runnable(){
                                    @Override
                                    public void run(){
                                        listener.fileStreamRetrieved();

                                        readStream(inputStream, "modified: " + fileName, prevSize );
                                    }
                                });
                                    threadOldFile.setName("hiloOldFile_" + aFile.getName());
                                    threadOldFile.start();

                                boolean success = ftpClient.completePendingCommand(); //wlways
                            } else {
                            log.info("failed retrieving stream from file modified :" + fileName);
                            }
                            ftpClient.changeWorkingDirectory(dirToList);
                            continue;
                        } else
                        if (dif < 0 ){ //known and full modified
                            existFileList.remove(dirToList + "/" + aFile.getName());
                            saveMap(sizeFileList);
                            continue;
                        }
                        ftpClient.changeWorkingDirectory(parentDir);
                        continue;
                    }
                } else if (aFile.isSymbolicLink()) {
                    log.info(aFile.getName() + " is a link of " + aFile.getLink() + " access denied" );
                    ftpClient.changeWorkingDirectory(parentDir);
                    continue;
                } else if (aFile.isUnknown()) {
                    log.info(aFile.getName() + " unknown type of file" );
                    ftpClient.changeWorkingDirectory(parentDir);
                    continue;
                } else {
                    ftpClient.changeWorkingDirectory(parentDir);
                    continue;
                }
                
            } //fin de bucle
        } //el listado no es vacío
    }//fin de método
    
    
   /*
    @void Serialize hashmap
    */
   
    public void saveMap(HashMap<String, Long> map){
        try { 
            FileOutputStream fileOut = new FileOutputStream("hasmap.ser");
            try (ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
                out.writeObject(map);
            }
        } catch(FileNotFoundException e){
            log.error("Error saving map File", e);
        } catch (IOException e){
            log.error("Error saving map IO:", e);
        }
    }
    
    
    /*
    @return HashMap<String,Long> objects
    */
    public HashMap<String,Long> loadMap(String name) throws ClassNotFoundException, IOException{
        FileInputStream map = new FileInputStream(name);
        HashMap hasMap;
        try (ObjectInputStream in = new ObjectInputStream(map)) {
            hasMap = (HashMap)in.readObject();
        }
        return hasMap;
    } 
    
    
    /*
    @void serialize long count
    */
    @SuppressWarnings("CallToPrintStackTrace")
    public void saveCount(long count){
        try {
            FileOutputStream fileOut = new FileOutputStream("eventCount.ser");
            try (ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
                out.writeObject(count);
            }
        } catch(FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        } 
    }
    
 
    /*
    @return long count
    */
    public long loadCount(String name) throws ClassNotFoundException, IOException{
        FileInputStream number = new FileInputStream(name);
        long count;
        try (ObjectInputStream in = new ObjectInputStream(number)) {
            count = (long)in.readObject();
        }
        return count;
    } 
    
   
   /*
    @void, delete file from hashmaps if deleted from ftp
    */
    public void cleanList(HashMap<String,Long> map) {
          for (Iterator<String> iter=map.keySet().iterator();iter.hasNext();) {
          final String fileName = iter.next();
          if (!(existFileList.contains(fileName))){ 
              iter.remove();
          }
        }
    }
   
    /*
    read retrieved stream from ftpclient into byte[] and process
    @return void
    */
    
    public void readStream(InputStream inputStream, String infoEvent, long position){
        log.info( infoEvent  + " ," + sizeFileList.size());
        try {
                inputStream.skip(position);
                byte[] bytesArray = new byte[CHUNKSIZE];
                int bytesRead = -1;
                while ((bytesRead = inputStream.read(bytesArray)) != -1) {
                    ByteArrayOutputStream baostream = new ByteArrayOutputStream(CHUNKSIZE);
                    baostream.write(bytesArray, 0, bytesRead);
                    byte[] data = baostream.toByteArray();
                    processMessage(data);
                }
                inputStream.close();
            } catch(IOException e ) {
                log.error("on readStream", e);
            }
    }
    
    
    public void setFtpSourceCounter(FtpSourceCounter ftpSourceCounter) {
        this.ftpSourceCounter = ftpSourceCounter;
    }
    
} //end of class

