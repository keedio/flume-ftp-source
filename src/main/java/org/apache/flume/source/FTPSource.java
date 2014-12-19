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


import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;


/*
 * @author Luis Lazaro
 */
public class FTPSource extends AbstractSource implements Configurable, PollableSource {
    
    private static final Logger log = LoggerFactory.getLogger(FTPSource.class);
    private HashMap<String, Long> sizeFileList = new HashMap<>();
   
    private FTPSourceUtils ftpSourceUtils;
    
    @Override
    public void configure(Context context) {            
           ftpSourceUtils = new FTPSourceUtils(context);
           ftpSourceUtils.connectToserver();
    }
    
    /*
    @enum Status , process source configured from context
    */
    public PollableSource.Status process() throws EventDeliveryException {
       log.info("ejecuta process");
       try {
           //String dirToList = "/home/mortadelo/ftp";
           listDirectory(ftpSourceUtils.getFtpClient(),ftpSourceUtils.getFtpClient().printWorkingDirectory(), "", 0);
       } catch(IOException e){
           e.printStackTrace();
       }
       
       //discoverElements();
       
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
        byte[] message = lastInfo;
        Event event = new SimpleEvent();
        Map<String, String> headers =  new HashMap<String, String>();  
        headers.put("timestamp", String.valueOf(System.currentTimeMillis()));
        event.setBody(message);
        event.setHeaders(headers);
        getChannelProcessor().processEvent(event);
    }
    
    
    
    
    
    
    public void listDirectory(FTPClient ftpClient, String parentDir, String currentDir, int level) throws IOException {
        String dirToList = parentDir;
        if (!currentDir.equals("")) {
            dirToList += "/" + currentDir;
        }
        FTPFile[] subFiles = ftpClient.listFiles(dirToList);
        if (subFiles != null && subFiles.length > 0) {
            
            for (FTPFile aFile : subFiles) {
                String currentFileName = aFile.getName();
                if (currentFileName.equals(".") || currentFileName.equals("..")) {
                    // skip parent directory and directory itself
                    continue;
                }
                
                if (aFile.isDirectory()) {
                    System.out.println("[" + aFile.getName() + "]");
                    ftpClient.changeWorkingDirectory(aFile.getName());
                    listDirectory(ftpClient, dirToList, aFile.getName(), level + 1);
                    continue;
                } else { //aFile is a regular file
                    ftpClient.changeWorkingDirectory(dirToList);
                    if (!(sizeFileList.containsKey(aFile.getName()))){ //new file
                        sizeFileList.put(aFile.getName(), aFile.getSize());
                        log.info("discovered: " + aFile.getName() + "," + " ," + sizeFileList.size() + " , Actual "  + aFile.getSize());
                        final InputStream inputStream = ftpClient.retrieveFileStream(aFile.getName());
                        Thread threadNewFile = new Thread( new Runnable(){
                                    @Override
                                    public void run(){
                                        try {
                                            
                                            byte[] bytesArray = new byte[4096];
                                            while ((inputStream.read(bytesArray)) > 0) {
                                                processMessage(bytesArray);
                                            }
                                            inputStream.close();
                                           
                                        } catch(IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                                    threadNewFile.setName("hiloNewFile_" + aFile.getName());
                                    threadNewFile.start();
                        //inputStream.close();
                        boolean success = ftpClient.completePendingCommand();
                        continue;
                    } else  { //known file
                        long dif = aFile.getSize() - sizeFileList.get(aFile.getName());
                        if (dif > 0 ){ //known and modified
                            final InputStream inputStream = ftpClient.retrieveFileStream(aFile.getName());
                            final long prevSize = sizeFileList.get(aFile.getName());
                            sizeFileList.put(aFile.getName(), aFile.getSize()); //save new size
                            log.info("modified: " + aFile.getName() + " , dif " + dif + " ," + sizeFileList.size() + " , new size "  + aFile.getSize());
                            Thread threadOldFile = new Thread( new Runnable(){
                                    @Override
                                    public void run(){
                                        try {
                                            inputStream.skip(prevSize);
                                            byte[] bytesArray = new byte[4096];
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
                        }
                        continue;
                    }
                } 
                
            } //fin de bucle
        } //el listado no es vacío
    }//fin de método
    
   
    
    
} //end of class
