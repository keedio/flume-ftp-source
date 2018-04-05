package org.keedio.flume.source.ftp.source.ssh;

import org.keedio.flume.source.ftp.client.sources.SFTPSource;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.flume.Event;
import org.apache.flume.Context;
import org.apache.flume.channel.ChannelProcessor;
import static org.mockito.Mockito.*;

import org.keedio.flume.source.ftp.source.Source;
import org.keedio.flume.source.ftp.metrics.SourceCounter;

import org.keedio.flume.source.ftp.source.TestFileUtils;

import org.keedio.flume.source.ftp.source.sshd.server.EmbeddedSSHDServer;
import org.apache.log4j.Logger;
import org.mockito.MockitoAnnotations;
import org.mockito.Mock;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public abstract class AbstractSshSourceTest extends EmbeddedSSHDServer {

    private Logger logger = Logger.getLogger(getClass());

    @Mock
    Context mockContext = new Context();

    Source sftpSource;
    SourceCounter sourceCounter;

    int getPort = 2223;
    String getUser = "flumetest";
    String getPassword = "flumetest";
    String getServer = "127.0.0.1";
   // String getWorkingDirectory = "/";
    String getFileName = "hasmapSFTP.ser";
    String getFolder = System.getProperty("java.io.tmpdir");
    String getAbsoutePath = System.getProperty("java.io.tmpdir") + "/" + getFileName;
    String getSource = "sftp";
    String getKnownHosts = "src/test/resources/known_hosts";

    @BeforeMethod
    public void beforeMethod() {
        MockitoAnnotations.initMocks(this);

        when(mockContext.getBoolean("search.processInUse", true)).thenReturn(true);
        when(mockContext.getBoolean("search.recursive", true)).thenReturn(true);
        when(mockContext.getString("client.source")).thenReturn(getSource);
        when(mockContext.getInteger("buffer.size")).thenReturn(1024);
        when(mockContext.getString("name.server")).thenReturn(getServer);
        when(mockContext.getString("user")).thenReturn(getUser);
        when(mockContext.getString("password")).thenReturn(getPassword);
        when(mockContext.getInteger("run.discover.delay")).thenReturn(100);
        when(mockContext.getInteger("port")).thenReturn(getPort);
       // when(mockContext.getString("working.directory")).thenReturn(getWorkingDirectory);
        when(mockContext.getString("file.name", "default_file_track_status.ser")).thenReturn(getFileName);
        when(mockContext.getString("folder", System.getProperty("java.io.tmpdir"))).thenReturn(System.getProperty("java.io.tmpdir"));
        when(mockContext.getInteger("chunk.size", 1024)).thenReturn(1024);

        when(mockContext.getString("knownHosts")).thenReturn(getKnownHosts);

        logger.info("Creating SFTP source");

        sftpSource = new Source();
        sftpSource.configure(mockContext);
        sourceCounter = new SourceCounter("SOURCE.");
        sftpSource.setFtpSourceCounter(sourceCounter);

        class DummyChannelProcessor extends ChannelProcessor {

            public DummyChannelProcessor() {
                super(null);
            }

            @Override
            public void processEvent(Event event) {
            }
        }

        sftpSource.setChannelProcessor(new DummyChannelProcessor());
    }

    @AfterMethod
    public void afterMethod() {
        try {
            logger.info("Stopping SFTP source");
            sftpSource.stop();

            Paths.get("hasmapSFTP.ser").toFile().delete();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void cleanup(Path file) {
        try {
            TestFileUtils.forceDelete(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void cleanup(List<Path> files) {
        for (Path f : files) {
            try {
                TestFileUtils.forceDelete(f);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
