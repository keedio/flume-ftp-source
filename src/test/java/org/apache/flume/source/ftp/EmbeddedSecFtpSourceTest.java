package org.apache.flume.source.ftp;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.*;
import javax.annotation.concurrent.NotThreadSafe;

import org.apache.flume.EventDeliveryException;
import org.apache.flume.PollableSource;
import org.apache.flume.source.TestFileUtils;
import org.apache.flume.source.ftp.server.EmbeddedSecureFtpServer;

import org.apache.flume.source.utils.FTPSourceEventListener;
import org.apache.ftpserver.ftplet.FtpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

/**
 * Basic integration tests for the Keedios' Flume FTP Source,
 *
 */
@NotThreadSafe
public class EmbeddedSecFtpSourceTest extends AbstractFtpSourceTest {

    private static Logger logger = LoggerFactory.getLogger(EmbeddedFtpSourceTest.class);

    static {
        logger.info("homeDir: " + EmbeddedSecureFtpServer.homeDirectory.toFile().getAbsolutePath());
    }

    @Test
    public void testProcessNoFileSec() {
        try {
            PollableSource.Status proc = ftpSource.process();
            Assert.assertEquals(PollableSource.Status.READY, proc);
            Assert.assertEquals(ftpSourceCounter.getFilesCount(), 0);
            Assert.assertEquals(ftpSourceCounter.getFilesProcCount(), 0);
            Assert.assertEquals(ftpSourceCounter.getFilesProcCountError(), 0);
        } catch (EventDeliveryException e) {
            Assert.fail();
        }
    }

    @Test(dependsOnMethods = "testProcessNoFileSec")
    public void testProcessNewFileSec() {
        Path tmpFile = null;
        try {
            tmpFile = TestFileUtils.createTmpFile(EmbeddedSecureFtpServer.homeDirectory);
            PollableSource.Status proc = ftpSource.process();
            Assert.assertEquals(PollableSource.Status.READY, proc);
            Assert.assertEquals(ftpSourceCounter.getFilesCount(), 1);
            Assert.assertEquals(ftpSourceCounter.getFilesProcCount(), 1);
            Assert.assertEquals(ftpSourceCounter.getFilesProcCountError(), 0);
        } catch (IOException|EventDeliveryException e) {
            Assert.fail();
        } finally {
            cleanup( tmpFile );
        }
    }

    @Test(dependsOnMethods = "testProcessNewFileSec")
    public void testProcessNewFileInNewFoldeSec() {
        Path tmpDir = null;
        Path tmpFile = null;
        try {
            tmpDir = TestFileUtils.createTmpDir(EmbeddedSecureFtpServer.homeDirectory);
            tmpFile = TestFileUtils.createTmpFile(tmpDir);

            PollableSource.Status proc = ftpSource.process();
            Assert.assertEquals(PollableSource.Status.READY, proc);
            Assert.assertEquals(ftpSourceCounter.getFilesCount(), 1);
            Assert.assertEquals(ftpSourceCounter.getFilesProcCount(), 1);
            Assert.assertEquals(ftpSourceCounter.getFilesProcCountError(), 0);
        } catch (IOException|EventDeliveryException e) {
            Assert.fail();
        } finally {
            cleanup(Arrays.asList(tmpFile, tmpDir));
        }

    }

    /**
     * Creates a new empty file in the ftp root,
     * creates a new directory in the ftp root and an empty file inside of it.
     */
    @Test(dependsOnMethods = "testProcessNewFileInNewFolderSec")
    public void testProcessMultipleFilesSec() {
        Path tmpDir = null;
        Path tmpFile0 = null;
        Path tmpFile1 = null;
        try {
            tmpDir = TestFileUtils.createTmpDir(EmbeddedSecureFtpServer.homeDirectory);
            tmpFile0 = TestFileUtils.createTmpFile(EmbeddedSecureFtpServer.homeDirectory);
            tmpFile1 = TestFileUtils.createTmpFile(tmpDir);

            PollableSource.Status proc = ftpSource.process();
            Assert.assertEquals(PollableSource.Status.READY, proc);
            Assert.assertEquals(ftpSourceCounter.getFilesCount(), 2);
            Assert.assertEquals(ftpSourceCounter.getFilesProcCount(), 2);
            Assert.assertEquals(ftpSourceCounter.getFilesProcCountError(), 0);
        } catch (IOException|EventDeliveryException e) {
            Assert.fail();
        } finally {
            cleanup(Arrays.asList(tmpFile0, tmpFile1, tmpDir));
        }
    }

    /**
     * Tries to access a file without permissions
     */
    @Test(dependsOnMethods = "testProcessMultipleFiles0Sec")
    public void testProcessNoPermissionSec() {
        Path tmpFile0 = null;
        try {
            tmpFile0 = TestFileUtils.createTmpFile(EmbeddedSecureFtpServer.homeDirectory);
            Files.setPosixFilePermissions(tmpFile0,
                    new HashSet<PosixFilePermission>(Arrays.asList(PosixFilePermission.OWNER_WRITE)));

            PollableSource.Status proc = ftpSource.process();
            Assert.assertEquals(PollableSource.Status.READY, proc);
            Assert.assertEquals(ftpSourceCounter.getFilesCount(), 1);
            Assert.assertEquals(ftpSourceCounter.getFilesProcCount(), 0);
            Assert.assertEquals(ftpSourceCounter.getFilesProcCountError(), 1);
        } catch (IOException|EventDeliveryException e) {
            Assert.fail();
        } finally {
            cleanup(tmpFile0);
        }

    }

    @Test(dependsOnMethods = "testProcessNoPermission")
    public void testProcessNotEmptyFileSec() {
        Path tmpFile0 = null;
        try {
            tmpFile0 = TestFileUtils.createTmpFile(EmbeddedSecureFtpServer.homeDirectory);
            TestFileUtils.appendASCIIGarbageToFile(tmpFile0);
            PollableSource.Status proc = ftpSource.process();
            Assert.assertEquals(PollableSource.Status.READY, proc);
            Assert.assertEquals(ftpSourceCounter.getFilesCount(), 1);
            Assert.assertEquals(ftpSourceCounter.getFilesProcCount(), 1);
            Assert.assertEquals(ftpSourceCounter.getFilesProcCountError(), 0);

            HashMap<String, Long> map = ftpSource.loadMap("hasmap.ser");

            String filename = "//"+tmpFile0.toFile().getName();

            Assert.assertEquals( Long.valueOf(map.get(filename)), Long.valueOf(81L * 100L));
        } catch (IOException|ClassNotFoundException|EventDeliveryException e) {
            Assert.fail();
        } finally {
            cleanup(tmpFile0);
        }
    }

    @Test(dependsOnMethods = "testProcessNotEmptyFileSec")
    public void testProcessModifiedFileSec() {
        Path tmpFile0 = null;
        try {
            tmpFile0 = TestFileUtils.createTmpFile(EmbeddedSecureFtpServer.homeDirectory);
            TestFileUtils.appendASCIIGarbageToFile(tmpFile0);
            PollableSource.Status proc0 = ftpSource.process();
            Assert.assertEquals(PollableSource.Status.READY, proc0);
            Assert.assertEquals(ftpSourceCounter.getFilesCount(), 1);
            Assert.assertEquals(ftpSourceCounter.getFilesProcCount(), 1);
            Assert.assertEquals(ftpSourceCounter.getFilesProcCountError(), 0);

            TestFileUtils.appendASCIIGarbageToFile(tmpFile0, 1000, 100);

            PollableSource.Status proc1 = ftpSource.process();
            Assert.assertEquals(PollableSource.Status.READY, proc0);
            Assert.assertEquals(ftpSourceCounter.getFilesCount(), 1);
            Assert.assertEquals(ftpSourceCounter.getFilesProcCount(), 1);
            Assert.assertEquals(ftpSourceCounter.getFilesProcCountError(), 0);

            Map<String,Long> map = ftpSource.loadMap("hasmap.ser");

            String filename = "//"+tmpFile0.toFile().getName();

            Assert.assertEquals(Long.valueOf(map.get(filename)), Long.valueOf(81L * 100L + 1000L * 101L));

        } catch (IOException|EventDeliveryException|ClassNotFoundException e) {
            Assert.fail();
        } finally {
            cleanup(tmpFile0);
        }
    }

    /**
     * Creates N temporary non-empty files in the
     * FTP root dir and process it using the FTP source.
     */
    @Test(dependsOnMethods = "testProcessModifiedFileSec")
    public void testProcessMultipleFiles1Sec() {
        int totFiles = 100;
        List<Path> files = new LinkedList<>();

        try {
            for (int i = 1; i <= totFiles; i++) {
                Path tmpFile0 = TestFileUtils.createTmpFile(EmbeddedSecureFtpServer.homeDirectory);
                TestFileUtils.appendASCIIGarbageToFile(tmpFile0);

                if (i == 8) {
                    Files.setPosixFilePermissions(tmpFile0,
                            new HashSet<>(Arrays.asList(PosixFilePermission.OWNER_WRITE)));
                }

                files.add(tmpFile0);
            }

            PollableSource.Status proc0 = ftpSource.process();
            Assert.assertEquals(PollableSource.Status.READY, proc0);
            Assert.assertEquals(ftpSourceCounter.getFilesCount(), totFiles);
            Assert.assertEquals(ftpSourceCounter.getFilesProcCount(), totFiles - 1);
            Assert.assertEquals(ftpSourceCounter.getFilesProcCountError(), 1);
        } catch (IOException|EventDeliveryException e) {
            Assert.fail();
        } finally {
            cleanup(files);
        }
    }
    
   @Test(dependsOnMethods = "testProcessMultipleFiles1Sec")
    public void testFtpFailure() {
        class MyEventListener extends FTPSourceEventListener {
            @Override
            public void fileStreamRetrieved()  {
                logger.info("Stopping server");
                EmbeddedSecureFtpServer.ftpServer.stop();
            }
        }
        ftpSource.setListener(new MyEventListener());

        String[] directories = EmbeddedSecureFtpServer.homeDirectory.toFile().list();

        logger.info("Found files: ");

        for (String directory : directories) {
            logger.info(directory);
        }

        Path tmpFile0 = null;
        try {
            tmpFile0 = TestFileUtils.createTmpFile(EmbeddedSecureFtpServer.homeDirectory);
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
