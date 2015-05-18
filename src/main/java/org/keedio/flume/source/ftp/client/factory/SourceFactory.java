/*
 * KEEDIO
 */
package org.keedio.flume.source.ftp.client.factory;

import org.apache.flume.Context;
import org.keedio.flume.source.ftp.client.KeedioSource;
import org.keedio.flume.source.ftp.client.sources.FTPSSource;
import org.keedio.flume.source.ftp.client.sources.FTPSource;
import org.keedio.flume.source.ftp.client.sources.SFTPSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luis Lázaro lalazaro@keedio.com Keedio
 */
public class SourceFactory {

    private KeedioSource keedioSource;
    private static final Logger logger = LoggerFactory.getLogger(KeedioSource.class);
    private final Integer discoverDelay = 10000;
    private final boolean flushlinesDefault = true;
    private final String folderDefault = System.getProperty("java.io.tmpdir");
    private final Integer chunksizeDefault = 1024;

    /**
     * Create KeedioSource
     *
     * @param context
     * @return KeedioSource
     */
    public KeedioSource createKeedioSource(Context context) {
        keedioSource = null;
        initSource(context);
        return keedioSource;
    }

    /**
     * Initiate attributes of KeediosSource according to context
     *
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
                SFTPSource sftpSource = new SFTPSource(context.getString("knownHosts"));
                keedioSource = sftpSource;
                initCommonParam(context);
                break;
            case "ftps":
                keedioSource = new FTPSSource();
                FTPSSource ftpsSource = new FTPSSource(
                        context.getBoolean("security.enabled"),
                        context.getString("security.cipher"),
                        context.getBoolean("security.certificate.enabled")
                );
                keedioSource = ftpsSource;
                initCommonParam(context);
                break;
            default:
                logger.error("Source not found in context");
        }
        return keedioSource;
    }

    /**
     * initialize common parameters for all sources.
     * @param context of source
     */
    public void initCommonParam(Context context) {
        keedioSource.setBufferSize(context.getInteger("buffer.size"));
        keedioSource.setServer(context.getString("name.server"));
        keedioSource.setUser(context.getString("user"));
        keedioSource.setPassword(context.getString("password"));
        keedioSource.setRunDiscoverDelay(context.getInteger("run.discover.delay", discoverDelay));
        keedioSource.setWorkingDirectory(context.getString("working.directory"));
        keedioSource.setPort(context.getInteger("port"));
        keedioSource.setFolder(context.getString("folder", folderDefault));
        keedioSource.setFileName(context.getString("file.name"));
        keedioSource.setFlushLines(context.getBoolean("flushlines", flushlinesDefault));
        keedioSource.setChunkSize(context.getInteger("chunk.size", chunksizeDefault));
    }

    /**
     * Generate a default file name for file track status.
     *
     * @param context
     * @return String
     */
    public String generateDefaultFileName(Context context) {
        return context.getString("client.source") + "status.ser";
    }
}
