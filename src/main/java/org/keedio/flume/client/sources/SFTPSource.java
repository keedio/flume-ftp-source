/*
 * KEEDIO
 */
package org.keedio.flume.client.sources;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.keedio.flume.client.KeedioSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luis Lázaro lalazaro@keedio.com Keedio
 */
public class SFTPSource extends KeedioSource {

    private static final Logger log = LoggerFactory.getLogger(SFTPSource.class);

    private String knownHosts;
    private JSch jsch;
    private Session sessionSftp;
    private Channel channel;
    private ChannelSftp sftpClient;
    //private ChannelSftp.LsEntry afile = (ChannelSftp.LsEntry) file;

    public SFTPSource() {
    }

    public SFTPSource(String knownHosts) {
        this.knownHosts = knownHosts;
        jsch = new JSch();
    }

    /**
     * @return boolean Opens a Socket connected to a server and login to return
     * True if successfully completed, false if not.
     */
    @Override
    public boolean connect() {
        setConnected(true);
        try {
            jsch.setKnownHosts(knownHosts);
            sessionSftp = jsch.getSession(user, server);
            sessionSftp.setPassword(password);
            sessionSftp.connect();
            if (sessionSftp.isConnected()) {
                channel = sessionSftp.openChannel("sftp");
                channel.connect();
                if (channel.isConnected()) {
                    sftpClient = (ChannelSftp) channel;
                }
            }
        } catch (JSchException e) {
            if (!(sessionSftp.isConnected())) {
                log.info("JSchException ", e);
                this.setConnected(false);
            }
        }
        return isConnected();
    }

    /**
     * @return the knownHosts
     */
    public String getKnownHosts() {
        return knownHosts;
    }

    /**
     * @param knownHosts the knownHosts to set
     */
    public void setKnownHosts(String knownHosts) {
        this.knownHosts = knownHosts;
    }

    /**
     * @return the jsch
     */
    public JSch getJsch() {
        return jsch;
    }

    /**
     * @param jsch the jsch to set
     */
    public void setJsch(JSch jsch) {
        this.jsch = jsch;
    }

    /**
     * @return the sessionSftp
     */
    public Session getSessionSftp() {
        return sessionSftp;
    }

    /**
     * @param sessionSftp the sessionSftp to set
     */
    public void setSessionSftp(Session sessionSftp) {
        this.sessionSftp = sessionSftp;
    }

    /**
     * @return the channel
     */
    public Channel getChannel() {
        return channel;
    }

    /**
     * @param channel the channel to set
     */
    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    /**
     * @return the sftpClient
     */
    public ChannelSftp getSftpClient() {
        return sftpClient;
    }

    /**
     * @param sftpClient the sftpClient to set
     */
    public void setSftpClient(ChannelSftp sftpClient) {
        this.sftpClient = sftpClient;
    }

    /**
     * Disconnect and logout from current connection to server
     *
     * @return void
     */
    public void disconnect() {
        channel.disconnect();
        sessionSftp.disconnect();
        setConnected(false);
        if (isConnected()) {
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
            sftpClient.cd(directory);
        } catch (SftpException e) {
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
            List<ChannelSftp.LsEntry> subFiles = sftpClient.ls(directory);
            for (ChannelSftp.LsEntry file : subFiles) {
                list.add((Object) file);
            }
        } catch (SftpException e) {
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
        ChannelSftp.LsEntry afile = (ChannelSftp.LsEntry) file;
        try {
            inputStream = sftpClient.get(afile.getFilename());
        } catch (SftpException e) {
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
        ChannelSftp.LsEntry afile = (ChannelSftp.LsEntry) file;
        return afile.getFilename();
    }

    @Override
    /**
     * @return boolean
     * @param Object to check
     */
    public boolean isDirectory(Object file) {
        ChannelSftp.LsEntry afile = (ChannelSftp.LsEntry) file;
        return afile.getAttrs().isDir();
    }

    @Override
    /**
     * Theres no attribute to check isfile in SftpATTRS
     *
     * @return boolean
     * @param Object to check
     */
    public boolean isFile(Object file) {
        boolean isfile = false;
        if ((!isDirectory(file)) & (!isLink(file))) {
            isfile = true;
        } else {
            isfile = false;
        }
        return isfile;
    }

    @Override
    /**
     * @return boolean
     */
    public boolean particularCommand() {
        return true;
    }

    @Override
    /**
     * @return long size
     * @param object file
     */
    public long getObjectSize(Object file) {
        long filesize = 0L;
        ChannelSftp.LsEntry afile = (ChannelSftp.LsEntry) file;
        try {
            filesize = sftpClient.lstat(afile.getFilename()).getSize();
        } catch (SftpException e) {
            log.error("Could not lstat to get size of the file");
        }
        return filesize;
    }

    @Override
    /**
     * @return boolean is a link
     * @param object as file
     */
    public boolean isLink(Object file) {
        ChannelSftp.LsEntry afile = (ChannelSftp.LsEntry) file;
        return afile.getAttrs().isLink();
    }

    @Override
    /**
     * @return String name of the link
     * @param object as file
     */
    public String getLink(Object file) {
        String link = "";
        ChannelSftp.LsEntry afile = (ChannelSftp.LsEntry) file;
        try {
            link = sftpClient.readlink(afile.getFilename());
        } catch (SftpException e) {
            log.error("Could not readLink to get name");
        }
        return link;
    }

    @Override
    /**
     *
     * @return String directory retrieved for server on connect
     */
    public String getDirectoryserver() {
        String printWorkingDirectory = "";
        try {
            printWorkingDirectory = sftpClient.pwd();
        } catch (SftpException e) {
            log.error("Error getting printworkingdirectory for server -sftpsource");
        }
        return printWorkingDirectory;
    }

    /**
     *
     * @return object as cliente of ftpsource
     */
    public Object getClientSource() {
        return sftpClient;
    }

    @Override
    public void setFileType(int fileType) throws IOException {
        //do nothing        
    }
}
