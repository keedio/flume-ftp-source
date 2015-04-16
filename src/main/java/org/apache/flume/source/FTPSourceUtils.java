/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.flume.source;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.util.TrustManagerUtils;

import org.apache.flume.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import org.apache.commons.net.ftp.FTPReply;



/**
 * @author luis lazaro
 */


public class FTPSourceUtils {
    private FTPClient ftpClient;
    private String server, user, password, protocolSec, folder, fileName;
    private Integer port;
    private int runDiscoverDelay;
    private String workingDirectory;
    private static final Logger log = LoggerFactory.getLogger(FTPSourceUtils.class);
    private Integer bufferSize;
    private boolean securityMode, securityCert;
    private boolean flushLines;

    public FTPSourceUtils(Context context) {
        bufferSize = context.getInteger("buffer.size");
        server = context.getString("name.server");
        user = context.getString("user");
        password = context.getString("password");
        runDiscoverDelay = context.getInteger("run.discover.delay");
        workingDirectory = context.getString("working.directory");
        port = context.getInteger("port");
        securityMode = context.getBoolean("security.enabled");
        securityCert = context.getBoolean("security.certificate.enabled");
        protocolSec = context.getString("security.cipher");
        folder = context.getString("folder");
        fileName = context.getString("file.name");
        flushLines = context.getBoolean("flushlines");
        
        
        if (securityMode){
            FTPSClient ftpsClient = new FTPSClient(protocolSec);
            if (securityCert){
                ftpsClient.setTrustManager(TrustManagerUtils.getValidateServerCertificateTrustManager());
                ftpClient = ftpsClient;
            } else {
                ftpsClient.setTrustManager(TrustManagerUtils.getAcceptAllTrustManager());
                ftpClient = ftpsClient;
            }
            
        } else {    //plain text
            ftpClient = new FTPClient();
        }
    }

    /*
    @return boolean, Opens a Socket connected to a server
    and login to return True if successfully completed, false if not.
    */
    public boolean connectToserver() {
        boolean success = true;
        try {
            ftpClient.connect(server, port);
            int replyCode = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                ftpClient.disconnect();
                log.error("Connect Failed due to FTP server refused connection.");
                success = false;
            }

            if (!(ftpClient.login(user, password))) {
                log.error("Could not login to the server");
                success = false;
            }
            ftpClient.enterLocalPassiveMode();
            if (workingDirectory != null) {
                ftpClient.changeWorkingDirectory(workingDirectory);
            }
            if (bufferSize != null) {
                ftpClient.setBufferSize(bufferSize);
            }
        } catch (IOException e) {
            success = false;
            log.error("IOException trying connect from configure source", e);
        }
        return success;
    }

    /*
    @return FTPClient
    */
    public FTPClient getFtpClient() {
        return ftpClient;
    }

    /*
    @return String, name of host to ftp
    */
    public String getServer() {
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
    public int getRunDiscoverDelay() {
        return runDiscoverDelay;
    }

    /*
    @return folder
    */
    public String getFolder(){
        return folder;
    }
    
    /*
    return fileName
    */
    public String getFileName(){
        return fileName;
    }

    /**
     * @return the flushLines
     */
    public boolean isFlushLines() {
        return flushLines;
    }

    /**
     * @param flushLines the flushLines to set
     */
    public void setFlushLines(boolean flushLines) {
        this.flushLines = flushLines;
    }
}

