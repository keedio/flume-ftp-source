package org.apache.flume.source;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.commons.lang.RandomStringUtils;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;
import java.util.List;


/**
 * Abstract class providing helper methods used in integration tests.
 * Translated from scala trait
 *
 */
public class TestFileUtils {

    static final Path ROOT_TMP_DIR = FileSystems.getDefault().getPath(System.getProperty("java.io.tmpdir"));

    /**
     * Creates a temporary dir.
     * @return the path object representing the temp dir
     */
    public static Path createTmpDir() throws IOException {
        return Files.createTempDirectory(ROOT_TMP_DIR, "flume-ftp-source-tmpdir-");
    }


    /**
     * Creates a temporary dir inside the specified directory.
     *
     * @return the path object representing the temp dir
     */
    public static Path createTmpDir(Path parent) throws IOException {
        return Files.createTempDirectory(parent,"flume-ftp-source-tmp-subdir-");
    }

    /**
     * Creates a temporary file in the specified directory.
     * @param parentDir the directory in which the temp file will be created
     * @return the path object representing the temp dir.
     */
    public static Path createTmpFile(Path parentDir) throws IOException {
        return Files.createTempFile(parentDir, "flume-ftp-source-tmpfile-", null);
    }

    /**
     * Creates a temp file.
     */
    public static Path createTmpFile() throws IOException {
        return Files.createTempFile(createTmpDir(), "flume-ftp-source-tmpfile-", null);
    }

    /**
     * Appends a predefined number of random ASCII chars to a given file.
     *
     * @param file the path representing the file to which the ASCII chars will be appended.
     * @return a collection containing the random lines appended to the provided file.
     */
    public static List<String> appendASCIIGarbageToFile(Path file) throws IOException {

        int lines = 100;
        int lineLength = 80;

        List<String> randomStrings = new LinkedList<>();
        for (int i = 0; i < lines; i++) {
            randomStrings.add( RandomStringUtils.randomAscii(lineLength) );
        }

        Files.write(file, randomStrings, StandardCharsets.UTF_8, StandardOpenOption.APPEND);

        return randomStrings;
    }

    /**
     * Appends a predefined number of random ASCII chars to a given file.
     *
     * @param file the path representing the file to which the ASCII chars will be appended.
     * @param lines the number of lines that will be appended to the input file (defaults to 100).
     * @return a collection containing the random lines appended to the provided file.
     */
    public static List<String> appendASCIIGarbageToFile(Path file, int lines) throws IOException {

        int lineLength = 80;

        List<String> randomStrings = new LinkedList<>();
        for (int i = 0; i < lines; i++) {
            randomStrings.add( RandomStringUtils.randomAscii(lineLength) );
        }

        Files.write(file, randomStrings, StandardCharsets.UTF_8, StandardOpenOption.APPEND);

        return randomStrings;
    }

    /**
     * Appends a predefined number of random ASCII chars to a given file.
     *
     * @param file the path representing the file to which the ASCII chars will be appended.
     * @param lines the number of lines that will be appended to the input file (defaults to 100).
     * @param lineLength the length of each line that will be appended to the input file (defaults to 80).
     * @return a collection containing the random lines appended to the provided file.
     */
    public static List<String> appendASCIIGarbageToFile(Path file, int lines, int lineLength) throws IOException {

        List<String> randomStrings = new LinkedList<>();
        for (int i = 0; i < lines; i++) {
            randomStrings.add( RandomStringUtils.randomAscii(lineLength) );
        }

        Files.write(file, randomStrings, StandardCharsets.UTF_8, StandardOpenOption.APPEND);

        return randomStrings;
    }


    /**
     * Deletes the given path object from file system.
     * If <i>file</i> is a directory it's removed recursively
     * @param file the path to remove
     */
    public static void forceDelete(Path file) throws IOException {
        if (file.toFile().exists()) {
            if ( file.toFile().isFile() ) {
                Files.delete(file);
            } else if ( file.toFile().isDirectory() ){

                Files.walkFileTree(file, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path f, BasicFileAttributes attrs) throws IOException {
                            Files.delete(f);
                            return FileVisitResult.CONTINUE;
                    }
                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                            Files.delete(dir);
                            return FileVisitResult.CONTINUE;
                    }
                });
            }
        }
    }
}
