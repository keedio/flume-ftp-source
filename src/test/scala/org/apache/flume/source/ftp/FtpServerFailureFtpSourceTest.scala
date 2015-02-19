package org.apache.flume.source.ftp

import com.typesafe.scalalogging.slf4j.LazyLogging
import org.apache.flume.PollableSource
import org.apache.flume.source.TestFileUtils
import org.apache.flume.source.ftp.server.EmbeddedFTPServer
import org.apache.flume.source.utils.FTPSourceEventListener
import org.testng.Assert._
import org.testng.annotations.Test

/**
 * Integration tests simulating an FTP server failure during
 * data retrieval.
 *
 * Created by luca on 3/2/15.
 */
class FtpServerFailureFtpSourceTest extends EmbeddedFTPServer with TestFileUtils with LazyLogging with AbstractFtpSourceTest {


  /**
   * Test an FTP server failure after the successful retrieval
   * of a the FTP file stream but before starting reading from the stream itself.
   */
  @Test
  def testFtpFailure(): Unit ={
    class MyEventListener extends FTPSourceEventListener {
      override def fileStreamRetrieved(): Unit = {
        logger.info("Stopping server")
        EmbeddedFTPServer.ftpServer.suspend()
        Thread.sleep(2000)
      }
    }
    ftpSource.setListener(new MyEventListener)

    val tmpFile0 = createTmpFile(EmbeddedFTPServer.homeDirectory)
    appendASCIIGarbageToFile(tmpFile0,1000,100)
    assertEquals(ftpSourceCounter.getFilesCount,0)

    val proc0 = ftpSource.process
    assertEquals(PollableSource.Status.READY, proc0)
    assertEquals(ftpSourceCounter.getFilesCount,1)
    assertEquals(ftpSourceCounter.getFilesProcCount,0)
    assertEquals(ftpSourceCounter.getFilesProcCountError,1)

    cleanup(tmpFile0)
  }
}
