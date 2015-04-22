package org.apache.flume.source.ftp;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.flume.Event;
import org.apache.flume.Context;
import org.apache.flume.channel.ChannelProcessor;
import static org.mockito.Mockito.*;

import org.apache.flume.source.FTPSource;
import org.apache.flume.source.FtpSourceCounter;
import org.apache.flume.source.TestFileUtils;
import org.apache.flume.source.ftp.server.EmbeddedSecureFtpServer;
import org.apache.log4j.Logger;
import org.mockito.MockitoAnnotations;
import org.mockito.Mock;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public abstract class AbstractSecureFtpSourceTest extends EmbeddedSecureFtpServer{
    private Logger logger = Logger.getLogger(getClass());

    @Mock
    Context mockContext = new Context();

    FTPSource ftpSource;
    FtpSourceCounter ftpSourceCounter;

    int getPort = 2422;

    String getUser = "flumetest";
    String getPassword = "flumetest";
    String getHost = "localhost";
    String getWorkingDirectory = null;
    //secure
    boolean getSecurityMode = false;
    boolean getSecurityCert = false;
    String getProtocolSec = "TLS";

    @Test(enabled=false) @BeforeMethod
    public void beforeMethod() {
        MockitoAnnotations.initMocks(this);

        when(mockContext.getInteger("buffer.size")).thenReturn(0);
        when(mockContext.getString("name.server")).thenReturn(getHost);
        when(mockContext.getString("user")).thenReturn(getUser);
        when(mockContext.getString("password")).thenReturn(getPassword);
        when(mockContext.getInteger("run.discover.delay")).thenReturn(100);
        when(mockContext.getInteger("port")).thenReturn(getPort);
        when(mockContext.getString("working.directory")).thenReturn(getWorkingDirectory);
        //secure
        when(mockContext.getBoolean("security.mode")).thenReturn(getSecurityMode);
        when(mockContext.getBoolean("security.certificate")).thenReturn(getSecurityCert);
        when(mockContext.getString("security.cipher")).thenReturn(getProtocolSec);

        logger.info("Creating FTP source");

        ftpSource = new FTPSource();
        ftpSource.configure(mockContext);
        ftpSourceCounter = new FtpSourceCounter("SOURCE.");
        ftpSource.setFtpSourceCounter(ftpSourceCounter);

        class DummyChannelProcessor extends ChannelProcessor {
            public DummyChannelProcessor() {
                super(null);
            }
            @Override
            public void processEvent(Event event) {}
        }

        ftpSource.setChannelProcessor(new DummyChannelProcessor());
    }

    @AfterMethod
    @Test(enabled=false)
    public void afterMethod() {
        try {
            logger.info("Stopping FTP source");
            ftpSource.stop();

            Paths.get("hasmap.ser").toFile().delete();
            Paths.get("eventCount.ser").toFile().delete();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
    @Test(enabled=false)
    public void cleanup(Path file) {
        try {
            TestFileUtils.forceDelete(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Test(enabled=false)
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
