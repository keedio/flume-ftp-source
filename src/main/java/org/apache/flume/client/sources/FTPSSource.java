/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.flume.client.sources;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.flume.client.KeedioSource;
import org.apache.commons.net.ftp.FTPSClient;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.util.TrustManagerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luis Lázaro lalazaro@keedio.com Keedio
 */
public class FTPSSource extends KeedioSource {

    private static final Logger log = LoggerFactory.getLogger(FTPSource.class);
    //private FTPFile afile = (FTPFile) file;
    
    private boolean securityMode, securityCert;
    private String protocolSec;
    private FTPSClient ftpsClient;

    public FTPSSource() {
        ftpsClient = new FTPSClient(protocolSec);
        checkIfCertificate();
    }

    /**
     * @return boolean Opens a Socket connected to a server and login to return
     */
    @Override
    public boolean connect() {
        setConnected(true);
        try {
            ftpsClient.connect(getServer(), getPort());
            int replyCode = ftpsClient.getReplyCode();

            if (!FTPReply.isPositiveCompletion(replyCode)) {
                ftpsClient.disconnect();
                log.error("Connect Failed due to FTP server refused connection.");
                this.setConnected(false);
            }

            if (!(ftpsClient.login(user, password))) {
                log.error("Could not login to the server");
                this.setConnected(false);
            }

            if (getWorkingDirectory() != null) {
                ftpsClient.changeWorkingDirectory(getWorkingDirectory());
            }

            if (getBufferSize() != null) {
                ftpsClient.setBufferSize(getBufferSize());
            }

        } catch (IOException e) {
            this.setConnected(false);
            log.error("IOException trying connect from configure source", e);
        }
        return isConnected();
    }

    /**
     * Disconnect and logout from current connection to server
     *
     * @return void
     */
    public void disconnect() {
        try {
            ftpsClient.logout();
            ftpsClient.disconnect();
            setConnected(false);
        } catch (IOException e) {
            log.error("Source " + this.getClass().getName() + " failed disconnect");
        }
    }

    @Override
    /**
     * @return void
     * @param String destination
     */
    public void changeToDirectory(String directory) {
        try {
            ftpsClient.changeWorkingDirectory(directory);
        } catch (IOException e) {
            log.error("Could not change to directory " + directory);
        }
    }

    @Override
    /**
     * @return list with objects in directory
     * @param current directory
     */
    public List<Object> listFiles(String directory) {
        List<Object> list = new ArrayList<>();
        try {
            FTPFile[] subFiles = ftpsClient.listFiles(directory);
            for (FTPFile file : subFiles) {
                list.add((Object) file);
            }
        } catch (IOException e) {
            log.error("Could not list list files from  " + directory);
        }
        return list;
    }

    @Override
    /**
     * @param Object
     * @return InputStream
     */
    public InputStream getInputStream(Object file) throws IOException {
        InputStream inputStream = null;
        FTPFile afile = (FTPFile) file;
        try {
            inputStream = ftpsClient.retrieveFileStream(afile.getName());
        } catch (IOException e) {
            log.error("Error trying to retrieve inputstream");
        }
        return inputStream;
    }

    @Override
    /**
     * @return name of the file
     * @param object as file
     */
    public String getObjectName(Object file) {
        FTPFile afile = (FTPFile) file;
        return afile.getName();
    }

    @Override
    /**
     * @return boolean
     * @param Object to check
     */
    public boolean isDirectory(Object file) {
        FTPFile afile = (FTPFile) file;
        return afile.isDirectory();
    }

    @Override
    /**
     * @return boolean
     * @param Object to check
     */
    public boolean isFile(Object file) {
        FTPFile afile = (FTPFile) file;
        return afile.isFile();
    }

    @Override
    /**
     * @return boolean
     */
    public boolean particularCommand() {
        boolean success = true;
        try {
            success = ftpsClient.completePendingCommand();
        } catch (IOException e) {
            log.error("Error on command completePendingCommand of FTPClient");
        }
        return success;
    }

    @Override
    /**
     * @return long size
     * @param object file
     */
    public long getObjectSize(Object file) {
        FTPFile afile = (FTPFile) file;
        return afile.getSize();
    }

    @Override
    /**
     * @return boolean is a link
     * @param object as file
     */
    public boolean isLink(Object file) {
        FTPFile afile = (FTPFile) file;
        return afile.isSymbolicLink();
    }

    @Override
    /**
     * @return String name of the link
     * @param object as file
     */
    public String getLink(Object file) {
        FTPFile afile = (FTPFile) file;
        return afile.getLink();
    }

    /**
     * @return the ftpsClient
     */
    public FTPSClient getFtpsClient() {
        return ftpsClient;
    }

    /**
     * @param ftpsClient the ftpsClient to set
     */
    public void setFtpsClient(FTPSClient ftpsClient) {
        this.ftpsClient = ftpsClient;
    }

    /**
     * @return the securityMode
     */
    public boolean isSecurityMode() {
        return securityMode;
    }

    /**
     * @param securityMode the securityMode to set
     */
    public void setSecurityMode(boolean securityMode) {
        this.securityMode = securityMode;
    }

    /**
     * @return the securityCert
     */
    public boolean isSecurityCert() {
        return securityCert;
    }

    /**
     * @param securityCert the securityCert to set
     */
    public void setSecurityCert(boolean securityCert) {
        this.securityCert = securityCert;
    }

    /**
     * @return the protocolSec
     */
    public String getProtocolSec() {
        return protocolSec;
    }

    /**
     * @param protocolSec the protocolSec to set
     */
    public void setProtocolSec(String protocolSec) {
        this.protocolSec = protocolSec;
    }

    /**
     * @void, check if trust all certifcates.
     */
    public void checkIfCertificate() {
        if (securityCert) {
            ftpsClient.setTrustManager(TrustManagerUtils.getValidateServerCertificateTrustManager());
        } else {
            ftpsClient.setTrustManager(TrustManagerUtils.getAcceptAllTrustManager());
        }
    }

    @Override
    /**
     *
     * @return String directory retrieved for server on connect
     */
    public String getDirectoryserver() {
        String printWorkingDirectory = "";
        try {
            printWorkingDirectory = ftpsClient.printWorkingDirectory();
        } catch (IOException e) {
            log.error("Error getting printworkingdirectory for server -ftpSsource");
        }
        return printWorkingDirectory;
    }

    /**
     *
     * @return object as cliente of ftpSsource
     */
    public Object getClientSource() {
        return ftpsClient;
    }
}
