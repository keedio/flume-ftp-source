/*
 * KEEDIO
 */
package org.keedio.flume.source.ftp.client.sources;

import org.keedio.flume.source.ftp.client.KeedioSource;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.net.ftp.FTPReply;

import java.io.IOException;
import org.apache.commons.net.ftp.FTP;

/**
 *
 * @author Luis Lázaro lalazaro@keedio.com Keedio
 */
public class FTPSource extends KeedioSource {

    private static final Logger log = LoggerFactory.getLogger(FTPSource.class);
    private FTPClient ftpClient = new FTPClient();
    //private FTPFile afile = (FTPFile) file;

    /**
     * @return boolean Opens a Socket connected to a server and login to return
     * True if successfully completed, false if not.
     */
    @Override
    public boolean connect() {
        setConnected(true);
        try {
            getFtpClient().connect(getServer(), getPort());
            int replyCode = getFtpClient().getReplyCode();

            if (!FTPReply.isPositiveCompletion(replyCode)) {
                getFtpClient().disconnect();
                log.error("Connect Failed due to FTP server refused connection.");
                this.setConnected(false);
            }

            if (!(ftpClient.login(user, password))) {
                log.error("Could not login to the server");
                this.setConnected(false);
            }
            
            ftpClient.enterLocalPassiveMode();
            ftpClient.setControlKeepAliveTimeout(300);
            if (getWorkingDirectory() != null) {
                getFtpClient().changeWorkingDirectory(getWorkingDirectory());
            }

            if (getBufferSize() != null) {
                getFtpClient().setBufferSize(getBufferSize());
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
            getFtpClient().logout();
            getFtpClient().disconnect();
            setConnected(false);

        } catch (IOException e) {
            log.error("Source " + this.getClass().getName() + " failed disconnect", e);
        }
    }

    @Override
    /**
     * @return void
     * @param String destination
     */
    public void changeToDirectory(String dir) {
        try {
            ftpClient.changeWorkingDirectory(dir);
        } catch (IOException e) {
            log.error("Could not change to directory " + dir);
        }
    }

    @Override
    /**
     * @return list with objects in directory
     * @param current directory
     */
    public List<Object> listFiles(String dir) {
        List<Object> list = new ArrayList<>();
        try {
            FTPFile[] subFiles = getFtpClient().listFiles(dir);
            for (FTPFile file : subFiles) {
                list.add((Object) file);
            }
        } catch (IOException e) {
            log.error("Could not list list files from  " + dir);
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
            if (isFlushLines()) {
                this.setFileType(FTP.ASCII_FILE_TYPE);
            } else {
                this.setFileType(FTP.BINARY_FILE_TYPE);
            }
            inputStream = getFtpClient().retrieveFileStream(afile.getName());
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

   
    /**
     * This method calls completePendigCommand, mandatory for FTPClient
     * @see <a href="http://commons.apache.org/proper/commons-net/apidocs/org/apache/commons/net/ftp/FTPClient.html#completePendingCommand()">completePendigCommmand</a>
     * @return boolean
     */
     @Override
    public boolean particularCommand() {
        boolean success = true;
        try {
            success = getFtpClient().completePendingCommand();
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

    @Override
    /**
     *
     * @return String directory retrieved for server on connect
     */
    public String getDirectoryserver() {
        String printWorkingDirectory = "";
        try {
            printWorkingDirectory = getFtpClient().printWorkingDirectory();
        } catch (IOException e) {
            log.error("Error getting printworkingdirectory for server -ftpsource");
        }
        return printWorkingDirectory;
    }

    /**
     * @return the ftpClient
     */
    public FTPClient getFtpClient() {
        return ftpClient;
    }

    /**
     * @param ftpClient the ftpClient to set
     */
    public void setFtpClient(FTPClient ftpClient) {
        this.ftpClient = ftpClient;
    }

    /**
     *
     * @return object as cliente of ftpsource
     */
    public Object getClientSource() {
        return ftpClient;
    }

    @Override
    public void setFileType(int fileType) throws IOException {
        ftpClient.setFileType(fileType);
    }
} //endclass
