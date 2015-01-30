package org.apache.flume.source.ftp

import org.apache.flume.{PollableSource, Context}
import org.apache.flume.source.{FtpSourceCounter, FTPSource, TestFileUtils}
import org.apache.flume.source.ftp.server.EmbeddedFTPServer
import org.junit.runner.RunWith
import org.junit.{Before, Test}
import org.junit.Assert._
import org.mockito.Mockito._
import org.mockito.runners.MockitoJUnitRunner
import scala.collection.JavaConversions._

/**
 * Created by luca on 30/1/15.
 */
@RunWith(classOf[MockitoJUnitRunner])
class FtpSourceTest extends EmbeddedFTPServer{

  val port = 2121
  var mockContext: Context = _



  @Before
  def beforeMethod() : Unit = {

    mockContext = mock(classOf[Context])

    when(mockContext.getString("name.server")).thenReturn("localhost")
    when(mockContext.getString("user")).thenReturn("flumetest")
    when(mockContext.getString("password")).thenReturn("flumetest")
    when(mockContext.getInteger("run.discover.delay")).thenReturn(1000)
    when(mockContext.getInteger("port")).thenReturn(port)


  }

  @Test
  def testProcessNoFile = {
    val ftpSource = new FTPSource
    ftpSource.configure(mockContext)

    val ftpSourceCounter = new FtpSourceCounter("SOURCE.")
    ftpSource.setFtpSourceCounter(ftpSourceCounter)

    val proc = ftpSource.process
    assertEquals(PollableSource.Status.READY, proc)
    assertEquals(0, ftpSourceCounter.getFilesCount)
    assertEquals(0, ftpSourceCounter.getFilesProcCount)
    assertEquals(0, ftpSourceCounter.getFilesProcCountError)
  }
}
