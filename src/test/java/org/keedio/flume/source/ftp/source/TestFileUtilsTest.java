package org.keedio.flume.source.ftp.source;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.testng.Assert;
import org.testng.annotations.Test;

public class TestFileUtilsTest {

    private static final Logger logger = LoggerFactory.getLogger(TestFileUtilsTest.class);

    @Test
    public void testCreateTmpDir() {

        Path path = null;
        try {
            path = TestFileUtils.createTmpDir();
            Assert.assertNotNull(path);

            Assert.assertTrue(path.isAbsolute());
            Assert.assertTrue(path.toFile().exists());
            Assert.assertTrue(path.toFile().isDirectory());
            Assert.assertTrue(path.getParent().equals(TestFileUtils.ROOT_TMP_DIR));
            Assert.assertTrue(path.toFile().getName().startsWith("flume-ftp-source-tmpdir-"));
        } catch (IOException e) {
            Assert.fail();
        }
    }

    @Test
    public void testCreateTmpFileWithParentDir() {
        try {
            Path dir = TestFileUtils.createTmpDir();
            Path tmpFile = TestFileUtils.createTmpFile(dir);

            Assert.assertNotNull(tmpFile);
            Assert.assertTrue(tmpFile.toFile().exists());
            Assert.assertTrue(tmpFile.toFile().isFile());
            Assert.assertEquals(0, tmpFile.toFile().length());
            Assert.assertTrue(tmpFile.toFile().getName().startsWith("flume-ftp-source-tmpfile-"));
            Assert.assertTrue(tmpFile.getParent().equals(dir));
        } catch (IOException e) {
            Assert.fail();
        }

    }

    @Test
    public void testAppendASCIIGarbagetoFile() {
        try {
            Path tmpFile = TestFileUtils.createTmpFile();

            logger.info(tmpFile.toFile().getAbsolutePath());

            Assert.assertEquals(0, tmpFile.toFile().length());

            List<String> garbage = TestFileUtils.appendASCIIGarbageToFile(tmpFile);
            Assert.assertNotNull(garbage);
            Assert.assertEquals(100, garbage.size());
            Assert.assertEquals(80, garbage.get(0).length());

            // there are 100 lines, 80 characteres per line + 1 carriage return
            Assert.assertEquals(100 * 81, tmpFile.toFile().length());

            // append more garbage
            TestFileUtils.appendASCIIGarbageToFile(tmpFile, 20, 10);

            Assert.assertEquals(100 * 81 + 20 * 11, tmpFile.toFile().length());
        } catch (IOException e) {
            Assert.fail();
        }
    }

    @Test
    public void testForceDelete() {
        int totFiles = 1000;

        try {
            Path tmpDir = TestFileUtils.createTmpDir();

            List<Path> files = new LinkedList<Path>();
            for (int i = 0; i < totFiles; i++) {
                Path tmpFile0 = TestFileUtils.createTmpFile(tmpDir);
                TestFileUtils.appendASCIIGarbageToFile(tmpFile0);
                files.add(tmpFile0);
            }

            Assert.assertTrue(tmpDir.toFile().exists());
            Assert.assertTrue(tmpDir.toFile().isDirectory());

            for (Path e : files) {
                Assert.assertTrue(e.toFile().exists());
                Assert.assertTrue(e.toFile().isFile());
            }

            TestFileUtils.forceDelete(tmpDir);

            Assert.assertFalse(tmpDir.toFile().exists());

            for (Path e : files) {
                Assert.assertFalse(e.toFile().exists());

            }
        } catch (IOException e) {
            Assert.fail();
        }
    }

    @Test
    public void testForceDeleteSymLink() {

        try {
            Path tmpDir = TestFileUtils.createTmpDir();
            Path tmpFile0 = TestFileUtils.createTmpFile(tmpDir);
            TestFileUtils.appendASCIIGarbageToFile(tmpFile0);

            Assert.assertTrue(tmpDir.toFile().exists());
            Assert.assertTrue(tmpDir.toFile().isDirectory());

            Assert.assertTrue(tmpFile0.toFile().exists());
            Assert.assertTrue(tmpFile0.toFile().isFile());

            String linkPath = tmpDir.toFile().getAbsolutePath();
            String linkName = "myTestLink";
            Path link = FileSystems.getDefault().getPath(linkPath, linkName);
            link = Files.createSymbolicLink(link, tmpFile0);
            Assert.assertTrue(link.toFile().exists());
            Assert.assertTrue(link.toFile().isFile());

            TestFileUtils.forceDelete(link);
            Assert.assertFalse(link.toFile().exists());

            TestFileUtils.forceDelete(tmpDir);
            Assert.assertFalse(tmpDir.toFile().exists());
        } catch ( IOException e ) {
            Assert.fail();
        }
    }

}