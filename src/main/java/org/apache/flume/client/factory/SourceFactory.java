/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.apache.flume.client.factory;

import org.apache.flume.Context;
import org.apache.flume.client.KeedioSource;
import org.apache.flume.client.sources.FTPSSource;
import org.apache.flume.client.sources.FTPSource;
import org.apache.flume.client.sources.SFTPSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Luis Lázaro lalazaro@keedio.com
 * Keedio
 */
public class SourceFactory {
    private KeedioSource keedioSource;
    private static final Logger log = LoggerFactory.getLogger(KeedioSource.class);
    private final Integer DISCOVERDELAY_DEFAULT = 10000;
    
    /**
     * Create KeedioSource
     * @param context
     * @return KeedioSource
     */
    public KeedioSource createKeedioSource(Context context){
        keedioSource = null;        
        initSource(context);        
        return keedioSource;        
    }
    
     /**
     * Initiate attributes of KeediosSource according to context
     * @param context of the source
     * @return KeedioSource
     */
    public KeedioSource initSource(Context context) {
        switch (context.getString("client.source")) {
            case "ftp":
                keedioSource = new FTPSource();
                initCommonParam(context);
                break;
            case "sftp":
                keedioSource = new SFTPSource();
                SFTPSource sftpSource = new SFTPSource();
                sftpSource.setKnownHosts(context.getString("knownHosts"));
                keedioSource = sftpSource;
                initCommonParam(context);
                break;
            case "ftps":
                keedioSource = new FTPSSource();
                FTPSSource ftpsSource = new FTPSSource();
                ftpsSource.setProtocolSec(context.getString("security.cipher"));
                ftpsSource.setSecurityMode(context.getBoolean("security.enabled"));
                ftpsSource.setSecurityCert(context.getBoolean("security.certificate.enabled"));
                keedioSource = ftpsSource;
                initCommonParam(context);
                break;
            default:
                log.error("Source not found in context");
                System.exit(1);
        }
        return keedioSource;
    }

    /**
     * @void initialize common parameters for all sources
     * @param context of source
     */
    public void initCommonParam(Context context) {
        keedioSource.setBufferSize(context.getInteger("buffer.size"));
        keedioSource.setServer(context.getString("name.server"));
        keedioSource.setUser(context.getString("user"));
        keedioSource.setPassword(context.getString("password"));
        keedioSource.setRunDiscoverDelay(context.getInteger("run.discover.delay",DISCOVERDELAY_DEFAULT));
        keedioSource.setWorkingDirectory(context.getString("working.directory"));
        keedioSource.setPort(context.getInteger("port"));
        keedioSource.setFolder(context.getString("folder"));
        keedioSource.setFileName(context.getString("file.name"));
        keedioSource.setFlushLines(context.getBoolean("flushlines"));
    }

}
