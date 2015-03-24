/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.flume.source;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import org.apache.flume.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import org.apache.commons.net.ftp.FTPReply;



/**
 * @author luis lazaro
 */


public class SFTPSourceUtils {
    private JSch jsch;
    private Session sessionSftp;
    private Channel channel;
    private ChannelSftp sftpClient;
    private String server, user, password,knownHosts;
    private Integer port;
    private int runDiscoverDelay;
    private String workingDirectory;
    private static final Logger log = LoggerFactory.getLogger(SFTPSourceUtils.class);
    private Integer bufferSize;

    public SFTPSourceUtils(Context context) {
        bufferSize = context.getInteger("buffer.size");
        server = context.getString("name.server");
        user = context.getString("user");
        password = context.getString("password");
        runDiscoverDelay = context.getInteger("run.discover.delay");
        workingDirectory = context.getString("working.directory");
        port = context.getInteger("port");
        knownHosts = context.getString("knownHosts");
        jsch = new JSch();
        try {
            jsch.setKnownHosts(knownHosts);
            sessionSftp = jsch.getSession(user,server);
            sessionSftp.setPassword(password);
            sessionSftp.connect();
            if (sessionSftp.isConnected()){
                channel = sessionSftp.openChannel("sftp");
                channel.connect();
                if (channel.isConnected()){
                    sftpClient = (ChannelSftp)channel;
                }
            }
        } catch(JSchException e){
            if (!(sessionSftp.isConnected())) {
            log.info("JSchException ", e);
            }
        }
        
    }

   

    /*
    @return SFTPClient
    */
    public ChannelSftp getSFtpClient() {
        return sftpClient;
    }

    /*
    @return String, name of host to ftp
    */
    public String getServer() {
        return server;
    }


    /*
    @return int delay for thread
    */
    public int getRunDiscoverDelay() {
        return runDiscoverDelay;
    }

    
}

