package org.apache.flume.source.ftp
import java.nio.file.Path

import org.apache.flume.{Event, Context}
import org.apache.flume.channel.ChannelProcessor
import org.apache.flume.source.{FtpSourceCounter, FTPSource, TestFileUtils}
import org.mockito.Mockito._
import org.mockito.{MockitoAnnotations, Mock}
import org.testng.annotations.{AfterMethod, BeforeMethod}

/**
 * Created by luca on 3/2/15.
 */
trait AbstractFtpSourceTest {
  this: TestFileUtils =>

  @Mock
  var mockContext: Context = _
  var ftpSource: FTPSource = _
  var ftpSourceCounter: FtpSourceCounter = _

  def getPort: Int = 2121

  def getUser: String = "flumetest"
  def getPassword: String = "flumetest"
  def getHost: String = "localhost"
  def getWorkingDirectory: String = null

  @BeforeMethod
  def beforeMethod(): Unit = {
    MockitoAnnotations.initMocks(this)

    when(mockContext.getString("name.server")).thenReturn(getHost)
    when(mockContext.getString("user")).thenReturn(getUser)
    when(mockContext.getString("password")).thenReturn(getPassword)
    when(mockContext.getInteger("run.discover.delay")).thenReturn(1000)
    when(mockContext.getInteger("port")).thenReturn(getPort)
    when(mockContext.getString("working.directory")).thenReturn(getWorkingDirectory)

    ftpSource = new FTPSource
    ftpSource.configure(mockContext)
    ftpSourceCounter = new FtpSourceCounter("SOURCE.")
    ftpSource.setFtpSourceCounter(ftpSourceCounter)

    class DummyChannelProcessor extends ChannelProcessor(null) {
      override def processEvent(event: Event): Unit = {
        None
      }
    }

    ftpSource.setChannelProcessor(new DummyChannelProcessor)
  }

  @AfterMethod
  def afterMethod(): Unit = {
    try {
      ftpSource.stop()
    }
    catch {
      case e: Throwable =>
    }
  }

  def cleanup(files: Path*): Unit = {
    logger.info(s"cleaning $files")
    files.foreach(forceDelete(_))
  }
}
