package org.apache.flume.source.ftp

import java.nio.file.{FileSystems, Path, Files}
import java.nio.file.attribute.PosixFilePermission
import javax.annotation.concurrent.NotThreadSafe

import com.typesafe.scalalogging.slf4j.LazyLogging
import org.apache.flume.channel.ChannelProcessor
import org.apache.flume.source.utils.FTPSourceEventListener
import org.apache.flume.{Event, PollableSource, Context}
import org.apache.flume.source.{FtpSourceCounter, FTPSource, TestFileUtils}
import org.apache.flume.source.ftp.server.EmbeddedFTPServer
import org.mockito.{MockitoAnnotations, Mock}
import org.mockito.Mockito._
import org.testng.Assert._
import org.testng.annotations.{Test, AfterMethod, BeforeMethod}
import scala.collection.JavaConversions._

/**
 * Basic integration tests for the Keedios' Flume FTP Source,
 *
 * Created by luca on 30/1/15.
 */
@NotThreadSafe
class EmbeddedFtpSourceTest extends EmbeddedFTPServer with TestFileUtils with LazyLogging with AbstractFtpSourceTest {

  logger.info("homeDir: "+EmbeddedFTPServer.homeDirectory.toFile.getAbsolutePath)

  @Test
  def testProcessNoFile() = {
    val proc = ftpSource.process
    assertEquals(PollableSource.Status.READY, proc)
    assertEquals(ftpSourceCounter.getFilesCount, 0)
    assertEquals(ftpSourceCounter.getFilesProcCount,0)
    assertEquals(ftpSourceCounter.getFilesProcCountError,0)


  }

  @Test
  def testProcessNewFile(): Unit ={
    val tmpFile = createTmpFile(EmbeddedFTPServer.homeDirectory)

    val proc = ftpSource.process
    assertEquals(PollableSource.Status.READY, proc)
    assertEquals(ftpSourceCounter.getFilesCount,1)
    assertEquals(ftpSourceCounter.getFilesProcCount,1)
    assertEquals(ftpSourceCounter.getFilesProcCountError,0)
    cleanup(tmpFile)
  }

  @Test
  def testProcessNewFileInNewFolder(): Unit = {
    val tmpDir = createTmpDir(EmbeddedFTPServer.homeDirectory)

    val tmpFile = createTmpFile(tmpDir)

    val proc = ftpSource.process
    assertEquals(PollableSource.Status.READY, proc)
    assertEquals(ftpSourceCounter.getFilesCount,1)
    assertEquals(ftpSourceCounter.getFilesProcCount,1)
    assertEquals(ftpSourceCounter.getFilesProcCountError,0)

    cleanup(tmpDir)
  }

  /**
   * Creates a new empty file in the ftp root,
   * creates a new directory in the ftp root and an empty file inside of it.
   */
  @Test
  def testProcessMultipleFiles0(): Unit = {
    val tmpDir = createTmpDir(EmbeddedFTPServer.homeDirectory)

    val tmpFile0 = createTmpFile(EmbeddedFTPServer.homeDirectory)
    val tmpFile1 = createTmpFile(tmpDir)

    val proc = ftpSource.process
    assertEquals(PollableSource.Status.READY, proc)
    assertEquals(ftpSourceCounter.getFilesCount,2)
    assertEquals(ftpSourceCounter.getFilesProcCount,2)
    assertEquals(ftpSourceCounter.getFilesProcCountError,0)

    cleanup(tmpFile0)
    cleanup(tmpDir)
  }

  /**
   * Tries to access a file without permissions
   */
  @Test
  def testProcessNoPermission(): Unit = {
    val tmpFile0 = createTmpFile(EmbeddedFTPServer.homeDirectory)

    Files.setPosixFilePermissions(tmpFile0,
      Set[PosixFilePermission](PosixFilePermission.OWNER_WRITE))

    val proc = ftpSource.process
    assertEquals(PollableSource.Status.READY, proc)
    assertEquals(ftpSourceCounter.getFilesCount,1)
    assertEquals(ftpSourceCounter.getFilesProcCount,0)
    assertEquals(ftpSourceCounter.getFilesProcCountError,1)

    cleanup(tmpFile0)
  }

  @Test
  def testProcessNotEmptyFile(): Unit = {
    val tmpFile0 = createTmpFile(EmbeddedFTPServer.homeDirectory)
    appendASCIIGarbageToFile(tmpFile0)
    val proc = ftpSource.process
    assertEquals(PollableSource.Status.READY, proc)
    assertEquals(ftpSourceCounter.getFilesCount,1)
    assertEquals(ftpSourceCounter.getFilesProcCount,1)
    assertEquals(ftpSourceCounter.getFilesProcCountError,0)

    val map = ftpSource.loadMap("hasmap.ser")

    logger.info(tmpFile0.toFile.getName)

    val filename = "//"+tmpFile0.toFile.getName

    assertEquals(map.get(filename), 81L*100L)

    cleanup(tmpFile0)
  }

  @Test
  def testProcessModifiedFile(): Unit = {
    val tmpFile0 = createTmpFile(EmbeddedFTPServer.homeDirectory)
    appendASCIIGarbageToFile(tmpFile0)
    val proc0 = ftpSource.process
    assertEquals(PollableSource.Status.READY, proc0)
    assertEquals(ftpSourceCounter.getFilesCount,1)
    assertEquals(ftpSourceCounter.getFilesProcCount,1)
    assertEquals(ftpSourceCounter.getFilesProcCountError,0)

    appendASCIIGarbageToFile(tmpFile0, 1000, 100)

    val proc1 = ftpSource.process
    assertEquals(PollableSource.Status.READY, proc0)
    assertEquals(ftpSourceCounter.getFilesCount,1)
    assertEquals(ftpSourceCounter.getFilesProcCount,1)
    assertEquals(ftpSourceCounter.getFilesProcCountError,0)

    val map = ftpSource.loadMap("hasmap.ser")

    logger.info(tmpFile0.toFile.getName)

    val filename = "//"+tmpFile0.toFile.getName

    assertEquals(map.get(filename), 81L*100L + 1000L*101L)

    cleanup(tmpFile0)
  }

  /**
   * Creates N temporary non-empty files in the
   * FTP root dir and process it using the FTP source.
   */
  @Test
  def testProcessMultipleFiles1(): Unit ={
    val totFiles = 100

    val files = for (i <- 1 to totFiles) yield {
      val tmpFile0 = createTmpFile(EmbeddedFTPServer.homeDirectory)
      appendASCIIGarbageToFile(tmpFile0)

      if (i == 8){
        Files.setPosixFilePermissions(tmpFile0,
          Set[PosixFilePermission](PosixFilePermission.OWNER_WRITE))
      }

      tmpFile0
    }

    val proc0 = ftpSource.process
    assertEquals(PollableSource.Status.READY, proc0)
    assertEquals(ftpSourceCounter.getFilesCount,totFiles)
    assertEquals(ftpSourceCounter.getFilesProcCount,totFiles-1)
    assertEquals(ftpSourceCounter.getFilesProcCountError,1)

    cleanup(files:_*)
  }


}
