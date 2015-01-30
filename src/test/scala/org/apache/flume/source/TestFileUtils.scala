package org.apache.flume.source

import java.nio.charset.Charset
import java.nio.file.{OpenOption, FileSystems, Files, Path}

import com.typesafe.scalalogging.slf4j.LazyLogging
import org.apache.commons.lang.RandomStringUtils
import scala.collection.JavaConversions._
import java.nio.file.StandardOpenOption._
/**
 *
 * Created by luca on 29/1/15.
 */
trait TestFileUtils extends LazyLogging{

  val ROOT_TMP_DIR = FileSystems.getDefault.getPath(System.getProperty("java.io.tmpdir"))

  def createTmpDir: Path = Files.createTempDirectory(ROOT_TMP_DIR,"flume-ftp-source-tmpdir-")

  def createTmpFile(parentDir: Path) : Path = Files.createTempFile(parentDir, "flume-ftp-source-tmpfile-", null)

  def createTmpFile : Path = Files.createTempFile(createTmpDir, "flume-ftp-source-tmpfile-", null)

  def appendASCIIGarbageToFile(file:Path, lines: Int = 100, lineLength: Int = 80): Seq[String] = {

    val randomStrings = for(i <- 1 to lines) yield { RandomStringUtils.randomAscii(lineLength) }

    Files.write(file,randomStrings,APPEND)

    randomStrings
  }
}
