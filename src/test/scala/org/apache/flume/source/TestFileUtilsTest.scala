package org.apache.flume.source

import java.nio.file.{Path, Files}
import java.nio.file.attribute.PosixFilePermission

import org.apache.flume.source.ftp.server.EmbeddedFTPServer
import org.testng.Assert._
import org.testng.annotations.Test


/**
 * Created by luca on 30/1/15.
 */
class TestFileUtilsTest extends TestFileUtils{

  @Test
  def testCreateTmpDir: Unit ={

    val path = createTmpDir

    assertNotNull(path)

    assertTrue(path.isAbsolute)
    assertTrue(path.toFile.exists)
    assertTrue(path.toFile.isDirectory)
    assertTrue(path.getParent.equals(ROOT_TMP_DIR))
    assertTrue(path.toFile.getName.startsWith("flume-ftp-source-tmpdir-"))
  }

  @Test
  def testCreateTmpFileWithParentDir: Unit ={
    val dir = createTmpDir

    val tmpFile = createTmpFile(dir)

    assertNotNull(tmpFile)
    assertTrue(tmpFile.toFile.exists)
    assertTrue(tmpFile.toFile.isFile)
    assertEquals(0,tmpFile.toFile.length)
    assertTrue(tmpFile.toFile.getName.startsWith("flume-ftp-source-tmpfile-"))
    assertTrue(tmpFile.getParent.equals(dir))
  }

  @Test
  def testAppendASCIIGarbageToFile: Unit ={
    val tmpFile = createTmpFile

    logger.info(tmpFile.toFile.getAbsolutePath)

    assertEquals(0,tmpFile.toFile.length)

    val garbage = appendASCIIGarbageToFile(tmpFile)
    assertNotNull(garbage)
    assertEquals(100,garbage.size)
    assertEquals(80,garbage(0).size)

    // there are 100 lines, 80 characteres per line + 1 carriage return
    assertEquals(100*81, tmpFile.toFile.length)

    // append more garbage
    appendASCIIGarbageToFile(tmpFile,20,10)

    assertEquals(100*81 + 20*11, tmpFile.toFile.length)
  }

  @Test
  def testForceDelete(): Unit ={
    val totFiles = 1000

    val tmpDir = createTmpDir

    val files = for (i <- 1 to totFiles) yield {
      val tmpFile0 = createTmpFile(tmpDir)
      appendASCIIGarbageToFile(tmpFile0)

      tmpFile0
    }

    assertTrue(tmpDir.toFile.exists())
    assertTrue(tmpDir.toFile.isDirectory)

    files.foreach{ e:Path =>
      assertTrue(e.toFile.exists)
      assertTrue(e.toFile.isFile)
    }

    forceDelete(tmpDir)

    assertFalse(tmpDir.toFile.exists())

    files.foreach{ e:Path =>
      assertFalse(e.toFile.exists)

    }
  }
}
