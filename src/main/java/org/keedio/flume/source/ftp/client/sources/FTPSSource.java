/*
 * KEEDIO
 */
package org.keedio.flume.source.ftp.client.sources;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.net.ftp.FTP;
import org.keedio.flume.source.ftp.client.KeedioSource;
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
public class FTPSSource extends KeedioSource<FTPFile> {

    private static final Logger log = LoggerFactory.getLogger(FTPSource.class);
    
    private boolean securityMode, securityCert;
    private String protocolSec;
    private FTPSClient ftpsClient;

    public FTPSSource(){}
    
    public FTPSSource(boolean securityMode, String protocolSec, boolean securityCert) {
        this.securityMode = securityMode;
        this.protocolSec = protocolSec;
        this.securityCert = securityCert;
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
            
            ftpsClient.enterLocalPassiveMode();
            ftpsClient.setControlKeepAliveTimeout(300);
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
     */
    @Override
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
    public List<FTPFile> listElements(String dir) {
        List<FTPFile> list = new ArrayList<>();
        try {
            FTPFile[] subFiles = getFtpsClient().listFiles(dir);
            list = Arrays.asList(subFiles);
        } catch (IOException e) {
            log.error("Could not list files from  " + dir);
        }
        return list;
    }

    @Override
    /**
     * @param Object
     * @return InputStream
     */
    public InputStream getInputStream(FTPFile file) throws IOException {
        InputStream inputStream = null;
        try {
            if (isFlushLines()) {
                this.setFileType(FTP.ASCII_FILE_TYPE);
            } else {
                this.setFileType(FTP.BINARY_FILE_TYPE);
            }
            inputStream = getFtpsClient().retrieveFileStream(file.getName());
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
    public String getObjectName(FTPFile file) {
        return file.getName();
    }

    @Override
    /**
     * @return boolean
     * @param FTPFile to check
     */
    public boolean isDirectory(FTPFile file) {
        return file.isDirectory();
    }

    @Override
   /**
    * @param FTPFile
    * @return boolean
    */
    public boolean isFile(FTPFile file) {
        return file.isFile();
    }

   
    /**
     * This method calls completePendigCommand, mandatory for FTPClient
     * @see <a href="http://commons.apache.org/proper/commons-net/apidocs/org/apache/commons/net/ftp/FTPClient.html#completePendingCommand()">completePendigCommmand</a>
     * @return boolean
     */
     @Override
    public boolean particularCommand() {
        boolean success = true;
        try {
            success = getFtpsClient().completePendingCommand();
        } catch (IOException e) {
            log.error("Error on command completePendingCommand of FTPClient", e);
        }
        return success;
    }

    @Override
    /**
     * @return long size
     * @param object file
     */
    public long getObjectSize(FTPFile file) {
        return file.getSize();
    }

    @Override
    /**
     * @return boolean is a link
     * @param object as file
     */
    public boolean isLink(FTPFile file) {
        return file.isSymbolicLink();
    }

    @Override
    /**
     * @return String name of the link
     * @param object as file
     */
    public String getLink(FTPFile file) {
        return file.getLink();
    }

    @Override
    /**
     *
     * @return String directory retrieved for server on connect
     */
    public String getDirectoryserver() {
        String printWorkingDirectory = "";
        try {
            printWorkingDirectory = getFtpsClient().printWorkingDirectory();
        } catch (IOException e) {
            log.error("Error getting printworkingdirectory for server -ftpsource");
        }
        return printWorkingDirectory;
    }

    /**
     * @return the ftpClient
     */
    public FTPSClient getFtpsClient() {
        return ftpsClient;
    }

    /**
     * @param ftpClient the ftpClient to set
     */
    public void setFtpsClient(FTPSClient ftpClient) {
        this.ftpsClient = ftpClient;
    }

    /**
     *
     * @return object as cliente of ftpsource
     */
    @Override
    public Object getClientSource() {
        return ftpsClient;
    }

    @Override
    public void setFileType(int fileType) throws IOException {
        ftpsClient.setFileType(fileType);
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

   

}
