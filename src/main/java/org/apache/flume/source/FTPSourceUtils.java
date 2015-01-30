/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.flume.source;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import org.apache.flume.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.net.ftp.FTPConnectionClosedException;

import java.io.IOException;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.flume.instrumentation.SourceCounter;



/**
 *
 * @author luis lazaro 
 */


public class FTPSourceUtils {
    private FTPClient ftpClient;
    private String server,user,password;
    private Integer port;
    private int runDiscoverDelay;
    private static final Logger log = LoggerFactory.getLogger(FTPSourceUtils.class);
    private SourceCounter sourceCounter;
    private FtpSourceCounter ftpSourceCounter;
    
    
    public FTPSourceUtils(Context context){
        ftpClient = new FTPClient();
        server = context.getString("name.server");
        user = context.getString("user");
        password = context.getString("password");
        runDiscoverDelay = context.getInteger("run.discover.delay");
        port = context.getInteger("port");
        sourceCounter = new SourceCounter("native");
    }
    
    /*
    @return boolean, Opens a Socket connected to a server
    and login to return True if successfully completed, false if not.
    */
    public boolean connectToserver(){
        boolean success = false;
       try {
            ftpClient.connect(server,port);
            int replyCode = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                System.out.println("Connect failed");
                //return;
            }
            success = ftpClient.login(user, password);
            if (!success) {
                System.out.println("Could not login to the server");
                //return;
            }
                       
           
            } catch (IOException ex) {
                System.out.println("Oops! Something wrong happened");
                ex.printStackTrace();
            }
       return success;
    }
    
    /*
    @return FTPClient
    */
    public FTPClient getFtpClient(){
        return ftpClient;
    }
    
    /*
    @return String, name of host to ftp
    */
    public String getServer(){
        return server;
    }
    
    /*
    @return FTPFile[] list of directories in current directory
    */
    public FTPFile[] getDirectories() throws IOException {
            return ftpClient.listDirectories();
    }
    
    /*
    @return FTPFile[] list of files in current directory
    */
    public FTPFile[] getFiles() throws IOException {
            return ftpClient.listFiles();
    }
    
    /*
    @return int delay for thread
    */
    public int getRunDiscoverDelay(){
        return runDiscoverDelay;
    }
    
    /*
    @SourceCounter, native class flume from source metrics
    */
    public SourceCounter getSourceCounter(){
        return sourceCounter;
    }
       
    
    /*
    @FtpSourceCounter, custom class for specific flume FTP source
    */
    public FtpSourceCounter getFtpSourceCounter(){
        return ftpSourceCounter;
    }
}

