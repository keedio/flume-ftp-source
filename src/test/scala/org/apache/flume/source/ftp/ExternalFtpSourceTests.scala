package org.apache.flume.source.ftp

import java.nio.file.{Files, FileSystems}

import org.apache.flume.PollableSource
import org.apache.flume.source.ftp.server.EmbeddedFTPServer
import org.apache.flume.source.{TestFileUtils, TestFileUtilsTest}
import org.testng.Assert._
import org.testng.annotations.Test

/**
 * Integration tests that will connect to an external FTP server.
 *
 * Created by luca on 4/2/15.
 */
class ExternalFtpSourceTests extends AbstractFtpSourceTest with TestFileUtils{
  override def getPort: Int = 21
  override def getPassword: String = "anonymous"
  override def getUser: String = "anonymous"

  @Test(enabled = false)
  def symLinkTest(): Unit ={
    val proc0 = ftpSource.process
    assertEquals(PollableSource.Status.READY, proc0)
    assertEquals(ftpSourceCounter.getFilesCount,1)
    assertEquals(ftpSourceCounter.getFilesProcCount,1)
    assertEquals(ftpSourceCounter.getFilesProcCountError,0)

  }

}
