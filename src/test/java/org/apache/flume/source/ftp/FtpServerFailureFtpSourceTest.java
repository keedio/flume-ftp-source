package org.apache.flume.source.ftp;

import org.apache.flume.EventDeliveryException;
import org.apache.flume.PollableSource;
import org.apache.flume.source.TestFileUtils;
import org.apache.flume.source.ftp.server.EmbeddedFTPServer;
import org.apache.flume.source.utils.FTPSourceEventListener;
import org.apache.ftpserver.ftplet.FtpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Integration tests simulating an FTP server failure during
 * data retrieval.
 *
 */
public class FtpServerFailureFtpSourceTest extends AbstractFtpSourceTest {
    /**
     * Test an FTP server failure after the successful retrieval
     * of a the FTP file stream but before starting reading from the stream itself.
     */

    @Test
    public void testFtpFailure() {

        final Logger logger = LoggerFactory.getLogger(FtpServerFailureFtpSourceTest.class);

        class MyEventListener extends FTPSourceEventListener {
            @Override
            public void fileStreamRetrieved()  {
                logger.info("Stopping server");
                EmbeddedFTPServer.ftpServer.suspend();
            }
        }
        ftpSource.setListener(new MyEventListener());

        Path tmpFile0 = null;
        try {
            tmpFile0 = TestFileUtils.createTmpFile(EmbeddedFTPServer.homeDirectory);
            TestFileUtils.appendASCIIGarbageToFile(tmpFile0, 1000, 100);
            Assert.assertEquals(ftpSourceCounter.getFilesCount(), 0);

            PollableSource.Status proc0 = ftpSource.process();
            Assert.assertEquals(PollableSource.Status.READY, proc0);
            Assert.assertEquals(ftpSourceCounter.getFilesCount(), 1);
            Assert.assertEquals(ftpSourceCounter.getFilesProcCount(), 0);
            Assert.assertEquals(ftpSourceCounter.getFilesProcCountError(), 1);
        } catch (IOException|EventDeliveryException e) {
            Assert.fail();
        } finally {
            cleanup(tmpFile0);
        }
    }
}
