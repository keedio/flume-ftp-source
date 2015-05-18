package org.keedio.flume.source.ftp.source.ftps;

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
import org.keedio.flume.source.ftp.source.ftps.server.EmbeddedFTPSserver;
import org.apache.log4j.Logger;
import org.mockito.MockitoAnnotations;
import org.mockito.Mock;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;



public abstract class AbstractFtpsSourceTest extends EmbeddedFTPSserver{
    private Logger logger = Logger.getLogger(getClass());

    @Mock
    Context mockContext = new Context();

  
    SourceCounter ftpSourceCounter;
    Source ftpsSource;

    int getPort = 2221;

    String getUser = "flumetest";
    String getPassword = "flumetest";
    String getHost = "localhost";
    String getWorkingDirectory = null;
    String getFileName = "hasmapFTPS.ser";
    String getAbsoutePath = System.getProperty("java.io.tmpdir") + "/hasmapFTPS.ser";
    String getSource = "ftps";
    boolean getSecurity = true;
    String getCipher = "TLS";
    boolean getCertificate = false;
    
     

    @BeforeMethod
    public void beforeMethod() {
        MockitoAnnotations.initMocks(this);

        when(mockContext.getString("client.source")).thenReturn(getSource);
        when(mockContext.getInteger("buffer.size")).thenReturn(0);
        when(mockContext.getString("name.server")).thenReturn(getHost);
        when(mockContext.getString("user")).thenReturn(getUser);
        when(mockContext.getString("password")).thenReturn(getPassword);
        when(mockContext.getInteger("run.discover.delay")).thenReturn(100);
        when(mockContext.getInteger("port")).thenReturn(getPort);
        when(mockContext.getString("working.directory")).thenReturn(getWorkingDirectory);
        when(mockContext.getString("file.name")).thenReturn(getFileName);
        when(mockContext.getString("folder", System.getProperty("java.io.tmpdir"))).thenReturn(System.getProperty("java.io.tmpdir"));
        when(mockContext.getInteger("chunk.size", 1024)).thenReturn(1024);
        
        when(mockContext.getBoolean("security.enabled")).thenReturn(getSecurity);
        when(mockContext.getString("security.cipher")).thenReturn(getCipher);
        when(mockContext.getBoolean("security.certificate.enabled")).thenReturn(getCertificate);        

        logger.info("Creating FTPS source");
        
        ftpsSource = new Source();
        
        ftpsSource.configure(mockContext);
        ftpSourceCounter = new SourceCounter("SOURCE.");
        ftpsSource.setFtpSourceCounter(ftpSourceCounter);

        class DummyChannelProcessor extends ChannelProcessor {
            public DummyChannelProcessor() {
                super(null);
            }
            @Override
            public void processEvent(Event event) {}
        }

        ftpsSource.setChannelProcessor(new DummyChannelProcessor());
    }

    @AfterMethod
    public void afterMethod() {
        try {
            logger.info("Stopping FTPS source");
            ftpsSource.stop();

            Paths.get("hasmapFTPS.ser").toFile().delete();
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
