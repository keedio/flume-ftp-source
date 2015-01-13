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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.PollableSource;
import org.apache.flume.conf.Configurable;
import org.apache.flume.event.SimpleEvent;
import org.apache.flume.source.AbstractSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



import java.io.IOException;


import java.io.InputStream;
import java.io.OutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ByteArrayOutputStream;
import java.util.Iterator;
import java.io.File;
import java.io.BufferedOutputStream;


import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

@SuppressWarnings("CallToPrintStackTrace")
/*
 * @author Luis Lazaro // lalazaro@keedio.com
    KEEDIO
 */
public class FTPSource extends AbstractSource implements Configurable, PollableSource {
    
    private static final Logger log = LoggerFactory.getLogger(FTPSource.class);
    private HashMap<String, Long> sizeFileList = new HashMap<>();
    private HashSet<String> existFileList = new HashSet<>();
    private final int CHUNKSIZE = 4096;   //event size in bytes
    private FTPSourceUtils ftpSourceUtils;
    private long eventCount = 0;
    
    @Override
    public void configure(Context context) {            
       ftpSourceUtils = new FTPSourceUtils(context);
       ftpSourceUtils.connectToserver();
       try {
            sizeFileList = loadMap("hasmap.ser");
            eventCount = loadCount("eventCount.ser");
       } catch(IOException | ClassNotFoundException e) {
            e.printStackTrace();
       }
    }
    
    /*
    @enum Status , process source configured from context
    */
    @Override
    public PollableSource.Status process() throws EventDeliveryException {
       
<<<<<<< HEAD
       try {
           log.info("data processed: " + eventCount/CHUNKSIZE  + " MBytes" + " actual dir " + ftpSourceUtils.getFtpClient().printWorkingDirectory() + " files : " +
               sizeFileList.size());
           //String dirToList = "/home/mortadelo/ftp";
           discoverElements(ftpSourceUtils.getFtpClient(),ftpSourceUtils.getFtpClient().printWorkingDirectory(), "", 0);           
       } catch(IOException e){
           e.printStackTrace();
       }
       cleanList(sizeFileList);
       existFileList.clear();
       saveMap(sizeFileList,1);
       saveMap(markFileList,2);
       saveCount(eventCount);
=======
            try {
                  log.info("data processed: " + eventCount/1024  + " MBytes" + " actual dir " + 
                          ftpSourceUtils.getFtpClient().printWorkingDirectory() + " files : " +
                  sizeFileList.size());
                  discoverElements(ftpSourceUtils.getFtpClient(),ftpSourceUtils.getFtpClient().printWorkingDirectory(), "", 0);           
                } catch(IOException e){
                    e.printStackTrace();
                }
>>>>>>> origin/flume_ftp_dev
       
                cleanList(sizeFileList);
                existFileList.clear();
                
                saveMap(sizeFileList);
                saveCount(eventCount);

        try 
        {  
            Thread.sleep(10000);				
            return PollableSource.Status.READY;     //source was successfully able to generate events
        } catch(InterruptedException inte){
            inte.printStackTrace();
            return PollableSource.Status.BACKOFF;   //inform the runner thread to back off for a bit		
        }
    }

 
    public void start(Context context) {
        log.info("Starting sql source {} ...", getName());
        super.start();	    
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
                ex.printStackTrace();
            }
            super.stop();
    }
    
    
    /*
    @void process last append to files
    */
    public void processMessage(byte[] lastInfo){
        eventCount++;
        byte[] message = lastInfo;
        Event event = new SimpleEvent();
        Map<String, String> headers =  new HashMap<>();  
        headers.put("timestamp", String.valueOf(System.currentTimeMillis()));
        event.setBody(message);
        event.setHeaders(headers);
        getChannelProcessor().processEvent(event);
    }
    
    
<<<<<<< HEAD
    
    public void discoverElements( final FTPClient ftpClient, String parentDir, String currentDir, int level) throws IOException {
=======
    /*
    discoverElements: find files to process them
    @return void 
    */
    @SuppressWarnings("UnnecessaryContinue")
    public void discoverElements( FTPClient ftpClient, String parentDir, String currentDir, int level) throws IOException {
>>>>>>> origin/flume_ftp_dev
        
        String dirToList = parentDir;
        if (!currentDir.equals("")) {
            dirToList += "/" + currentDir;
        }
        FTPFile[] subFiles = ftpClient.listFiles(dirToList);
        if (subFiles != null && subFiles.length > 0) {
            
            for (FTPFile aFile : subFiles) {
                String currentFileName = aFile.getName();
                if (currentFileName.equals(".") || currentFileName.equals("..")) {
                    System.out.println("skip parent directory and directory itself");
                    continue;
                }
                
                if (aFile.isDirectory()) {
                    System.out.println("[" + aFile.getName() + "]");
                    ftpClient.changeWorkingDirectory(parentDir);
                    discoverElements(ftpClient, dirToList, aFile.getName(), level + 1);
                    continue;
                } else if (aFile.isFile()) { //aFile is a regular file
                    ftpClient.changeWorkingDirectory(dirToList);
                    final String longFileName = dirToList + "/" + aFile.getName();
                    final String fileName = aFile.getName();
                    existFileList.add(dirToList + "/" + aFile.getName());
<<<<<<< HEAD
                    
                    if (!(sizeFileList.containsKey(dirToList + "/" + aFile.getName()))){ //new file
                        sizeFileList.put(dirToList + "/" + aFile.getName(), aFile.getSize());
                        log.info("discovered: " + dirToList + "/" + aFile.getName() + "," + " ," + sizeFileList.size() + " , Actual "  + aFile.getSize());
                        
                        Thread threadNewFile = new Thread( new Runnable(){
                                    @Override
                                    public void run(){
                                        try {
                                            File downloadFile2 = new File("/var/log/flume-ftp/fichero.log");
                                             OutputStream outputStream2 = new BufferedOutputStream(new FileOutputStream(downloadFile2));
                                              InputStream  inputStream = ftpClient.retrieveFileStream(fileName);                                           
                                            byte[] bytesArray = new byte[4096];
                                             int bytesRead = -1;
                                            while ((bytesRead = inputStream.read(bytesArray)) != -1) {
                                               
                                               processMessage(bytesArray);
                                               // outputStream2.write(bytesArray, 0, bytesRead); //works well
                                                 outputStream2.write(bytesArray); //---> fail
                                            }
                                             
                                           boolean  success = ftpClient.completePendingCommand();
                                            if (success) {
                                                System.out.println("File #2 has been downloaded successfully.");
                                            }
                                            outputStream2.close();
                                            inputStream.close();
                                           
                                        } catch(IOException e) {
                                            e.printStackTrace();
                                        }
=======
                    final String fileName = aFile.getName();
                    
                    if (!(sizeFileList.containsKey(dirToList + "/" + aFile.getName()))){ //new file
                        sizeFileList.put(dirToList + "/" + aFile.getName(), aFile.getSize());
                        saveMap(sizeFileList);
                        final InputStream inputStream = ftpClient.retrieveFileStream(aFile.getName());
                        if (inputStream != null) {
                        Thread threadNewFile = new Thread( new Runnable(){
                                    @Override
                                    public void run(){
                                       readStream(inputStream, "discovered: " + fileName, 0 );
>>>>>>> origin/flume_ftp_dev
                                    }
                                });
                                    threadNewFile.setName("hiloNewFile_" + aFile.getName());
                                    threadNewFile.start();
<<<<<<< HEAD
                        //boolean success = ftpClient.completePendingCommand();
                        ftpClient.changeWorkingDirectory(dirToList);
                        //continue;
=======
                        boolean success = ftpClient.completePendingCommand();                        
                        } else {
                            log.info("failed retrieving stream from file :" + fileName);
                            existFileList.remove(dirToList + "/" + aFile.getName());
                            cleanList(sizeFileList);
                        }
                        ftpClient.changeWorkingDirectory(dirToList);
                        continue;
                        
                        
>>>>>>> origin/flume_ftp_dev
                    } else  { //known file                        
                        long dif = aFile.getSize() - sizeFileList.get(dirToList + "/" + aFile.getName());
                        if (dif > 0 ){ //known and modified
                            final InputStream inputStream = ftpClient.retrieveFileStream(aFile.getName());
                            final long prevSize = sizeFileList.get(dirToList + "/" + aFile.getName());
                            sizeFileList.put(dirToList + "/" + aFile.getName(), aFile.getSize()); //save new size
                            saveMap(sizeFileList);
                            if (inputStream != null) {
                            Thread threadOldFile = new Thread( new Runnable(){
                                    @Override
                                    public void run(){
<<<<<<< HEAD
                                        try {
                                            inputStream.skip(prevSize);
                                            byte[] bytesArray = new byte[CHUNKSIZE];
                                            while ((inputStream.read(bytesArray)) > 0) {
                                                processMessage(bytesArray);
                                            }
                                            inputStream.close();
                                           
                                        } catch(IOException e) {
                                            e.printStackTrace();                                            
                                        }  
=======
                                        readStream(inputStream, "modified: " + fileName, prevSize );
>>>>>>> origin/flume_ftp_dev
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
<<<<<<< HEAD
                            final InputStream inputStream = ftpClient.retrieveFileStream(aFile.getName());
                            final long prevSize = 0;
                            sizeFileList.put(dirToList + "/" + aFile.getName(), aFile.getSize()); //save new size
                            log.info("full modified: " + dirToList + "/" + aFile.getName() + " , dif " + dif + " ," + sizeFileList.size() + " , new size "  + aFile.getSize());
                            Thread threadOldFile = new Thread( new Runnable(){
                                    @Override
                                    public void run(){
                                        try {
                                            inputStream.skip(prevSize);
                                            byte[] bytesArray = new byte[CHUNKSIZE];
                                            while ((inputStream.read(bytesArray)) > 0) {                                                
                                                processMessage(bytesArray);
                                            }
                                            inputStream.close();
                                           
                                        } catch(IOException e) {
                                            e.printStackTrace();                                            
                                        } 
                                    }
                                });
                                    threadOldFile.setName("hiloOldFile_" + aFile.getName());
                                    threadOldFile.start();
                            boolean success = ftpClient.completePendingCommand(); //wlways
                            continue;
                              } //else 
//                        if (dif == 0 && aFile.getSize() > markFileList.get(dirToList + "/" + aFile.getName()) ){ //was not discovered at all
//                            final InputStream inputStream = ftpClient.retrieveFileStream(aFile.getName());
//                            final long prevSize = markFileList.get(dirToList + "/" + aFile.getName());
//                            sizeFileList.put(dirToList + "/" + aFile.getName(), aFile.getSize()); //save new size
//                            log.info("resuming: " + dirToList + "/" + aFile.getName() + " , remaining: " + (aFile.getSize() - prevSize) + " ," + sizeFileList.size() + " , new size "  + aFile.getSize());
//                            Thread threadOldFile = new Thread( new Runnable(){
//                                    @Override
//                                    public void run(){
//                                        try {
//                                            inputStream.skip(prevSize);
//                                            int count = 0;
//                                            byte[] bytesArray = new byte[CHUNKSIZE];
//                                            while ((inputStream.read(bytesArray)) > 0) {
//                                                processMessage(bytesArray);
//                                                count++;
//                                                markFileList.put(longFileName, prevSize + (long)count * CHUNKSIZE);
//                                            }
//                                            inputStream.close();
//                                           
//                                        } catch(IOException e) {
//                                            e.printStackTrace();                                            
//                                        } 
//                                    }
//                                });
//                                    threadOldFile.setName("hiloReFile_" + aFile.getName());
//                                    threadOldFile.start();
//                            boolean success = ftpClient.completePendingCommand(); //wlways
//                            continue;
//                        }
                        //System.out.println(dirToList);
=======
                            existFileList.remove(dirToList + "/" + aFile.getName());
                            saveMap(sizeFileList);
                            continue;
                        }
>>>>>>> origin/flume_ftp_dev
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
    @SuppressWarnings("CallToPrintStackTrace")
    public void saveMap(HashMap<String, Long> map){
        try { 
            FileOutputStream fileOut = new FileOutputStream("hasmap.ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(map);
            out.close();
        } catch(FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
    }
    
    
    /*
    @return HashMap<String,Long> objects
    */
    public HashMap<String,Long> loadMap(String name) throws ClassNotFoundException, IOException{
        FileInputStream map = new FileInputStream(name);
        ObjectInputStream in = new ObjectInputStream(map);
        HashMap hasMap = (HashMap)in.readObject();
        in.close();
        return hasMap;
    } 
    
    
    /*
    @void serialize long count
    */
    @SuppressWarnings("CallToPrintStackTrace")
    public void saveCount(long count){
        try {
            FileOutputStream fileOut = new FileOutputStream("eventCount.ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(count);
            out.close();
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
        ObjectInputStream in = new ObjectInputStream(number);
        long count = (long)in.readObject();
        in.close();
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
                //Thread.sleep(200);
            } catch(IOException e ) {
                e.printStackTrace();
            } //catch (InterruptedException e){
//                e.printStackTrace();
//            }
    }
    
    
} //end of class
