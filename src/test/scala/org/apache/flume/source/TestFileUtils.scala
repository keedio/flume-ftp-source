package org.apache.flume.source

import java.io.IOException
import java.nio.charset.{StandardCharsets, Charset}
import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes

import com.typesafe.scalalogging.slf4j.LazyLogging
import org.apache.commons.lang.RandomStringUtils
import scala.collection.JavaConversions._
import java.nio.file.StandardOpenOption._
/**
 * Trait providing helper methods used is integration tests.
 *
 * Created by luca on 29/1/15.
 */
trait TestFileUtils extends LazyLogging{

  val ROOT_TMP_DIR = FileSystems.getDefault.getPath(System.getProperty("java.io.tmpdir"))

  /**
   * Creates a temporary dir.
   * @return the path object representing the temp dir
   */
  def createTmpDir: Path = Files.createTempDirectory(ROOT_TMP_DIR,"flume-ftp-source-tmpdir-")

  /**
   * Creates a temporary dir inside the specified directory.
   *
   * @return the path object representing the temp dir
   */
  def createTmpDir(parent: Path): Path = Files.createTempDirectory(parent,"flume-ftp-source-tmp-subdir-")

  /**
   * Creates a temporary file in the specified directory.
   * @param parentDir the directory in which the temp file will be created
   * @return the path object representing the temp dir.
   */
  def createTmpFile(parentDir: Path) : Path = Files.createTempFile(parentDir, "flume-ftp-source-tmpfile-", null)

  /**
   * Creates a temp file.
   */
  def createTmpFile : Path = Files.createTempFile(createTmpDir, "flume-ftp-source-tmpfile-", null)

  /**
   * Appends a predefined number of random ASCII chars to a given file.
   *
   * @param file the path representing the file to which the ASCII chars will be appended.
   * @param lines the number of lines that will be appended to the input file (defaults to 100).
   * @param lineLength the length of each line that will be appended to the input file (defaults to 80).
   * @return a collection containing the random lines appended to the provided file.
   */
  def appendASCIIGarbageToFile(file:Path, lines: Int = 100, lineLength: Int = 80): Seq[String] = {

    val randomStrings = for(i <- 1 to lines) yield { RandomStringUtils.randomAscii(lineLength) }

    Files.write(file,randomStrings,StandardCharsets.UTF_8, APPEND)

    randomStrings
  }

  /**
   * Deletes the given path object from file system.
   * If <i>file</i> is a directory it's removed recursively
   * @param file the path to remove
   */
  def forceDelete(file:Path): Unit = {
    if (file.toFile.exists()) {
      if (file.toFile.isFile) {
        Files.delete(file)
      } else if (file.toFile.isDirectory){

        Files.walkFileTree(file,new SimpleFileVisitor[Path]{
          override def visitFile(f: Path, attrs: BasicFileAttributes): FileVisitResult = {
            Files.delete(f)
            FileVisitResult.CONTINUE
          }

          override def postVisitDirectory(dir: Path, exc: IOException): FileVisitResult = {
            Files.delete(dir)
            FileVisitResult.CONTINUE
          }
        })
      }
    }
  }
}
