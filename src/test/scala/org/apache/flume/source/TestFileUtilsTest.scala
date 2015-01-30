package org.apache.flume.source

import org.junit.Test
import org.junit.Assert._


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

    garbage.foreach(logger.info(_))


    // there are 100 lines, 80 characteres per line + 1 carriage return
    assertEquals(100*81, tmpFile.toFile.length)

    // append more garbage
    appendASCIIGarbageToFile(tmpFile,20,10)

    assertEquals(100*81 + 20*11, tmpFile.toFile.length)
  }
}
