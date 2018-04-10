/*
 * KEEDIO
 */
package org.keedio.flume.source.ftp.source;

import java.io.*;

import junit.framework.TestCase;

import org.apache.flume.Event;
import org.apache.flume.channel.ChannelProcessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.mockito.Mockito.mock;

import org.apache.flume.event.SimpleEvent;

import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Calendar;
import java.util.Date;
import java.util.zip.GZIPInputStream;

/**
 * @author luislazaro
 */
public class SourceTest extends TestCase {

  private static final Logger log = LoggerFactory.getLogger(SourceTest.class);

  public SourceTest(String testName) {
    super(testName);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public void testReadStream6() {
    System.out.println("readStream6-gzip decompression");
    InputStream inputStream = null;
    long position = 0L;
    try {
      inputStream = new GZIPInputStream(new FileInputStream("src/test/resources/gzippedcsvfile.gz"));
      assertNotNull(inputStream);
    } catch (FileNotFoundException e) {
      log.error("", e);
      fail();
    } catch (IOException e) {
      e.printStackTrace();
      fail();
    }

    class DummyChannelProcessor extends ChannelProcessor {

      public DummyChannelProcessor() {
        super(null);
      }

      @Override
      public void processEvent(Event event) {
      }
    }

    ChannelProcessor chanel = new DummyChannelProcessor();

    Event event = new SimpleEvent();
    try {
      inputStream.skip(0);
      byte[] bytesArray = new byte[1024];
      int bytesRead = -1;

      while ((bytesRead = inputStream.read(bytesArray)) != -1) {
        try (ByteArrayOutputStream baostream = new ByteArrayOutputStream(1024)) {
          baostream.write(bytesArray, 0, bytesRead);
          byte[] data = baostream.toByteArray();
          ByteBuffer byteBuffer = ByteBuffer.wrap(data);
          byteBuffer.compact();
          event.setBody(byteBuffer.array());
          event.setBody(data);
        }

      }

      inputStream.close();
    } catch (IOException e) {
      log.error("", e);
    }
    try {
      assertEquals(new String(event.getBody(), "UTF-8"), "field1,field2,field3");
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
      fail();
    }

  }

  public void testReadStream4() {
    System.out.println("readStream4");
    InputStream inputStream = null;
    long position = 0L;
    try {
      inputStream = new FileInputStream("src/test/resources/file.txt");
      assertNotNull(inputStream);
    } catch (FileNotFoundException e) {
      log.error("", e);
      fail();
    }

    class DummyChannelProcessor extends ChannelProcessor {

      public DummyChannelProcessor() {
        super(null);
      }

      @Override
      public void processEvent(Event event) {
      }
    }

    ChannelProcessor chanel = new DummyChannelProcessor();

    Event event = new SimpleEvent();
    try {
      inputStream.skip(0);
      byte[] bytesArray = new byte[1024];
      int bytesRead = -1;

      while ((bytesRead = inputStream.read(bytesArray)) != -1) {
        try (ByteArrayOutputStream baostream = new ByteArrayOutputStream(1024)) {
          baostream.write(bytesArray, 0, bytesRead);
          byte[] data = baostream.toByteArray();
          ByteBuffer byteBuffer = ByteBuffer.wrap(data);
          byteBuffer.compact();
          event.setBody(byteBuffer.array());
          event.setBody(data);
        }

      }

      inputStream.close();
    } catch (IOException e) {
      log.error("", e);
    }

  }

  public void testReadStream5() {
    System.out.println("readStream5");
    InputStream inputStream = null;
    long position = 0L;
    try {
      inputStream = new FileInputStream("src/test/resources/file.txt");
      assertNotNull(inputStream);
    } catch (FileNotFoundException e) {
      log.error("", e);
      fail();
    }

    class DummyChannelProcessor extends ChannelProcessor {

      public DummyChannelProcessor() {
        super(null);
      }

      @Override
      public void processEvent(Event event) {
      }
    }

    ChannelProcessor chanel = new DummyChannelProcessor();

    Event event = new SimpleEvent();
    try {
      inputStream.skip(position);
      BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
      String line = null;

      if (in.ready()) {
        while ((line = in.readLine()) != null) {
          //processMessage(line.getBytes());
          event.setBody(line.getBytes());
        }
      } else {
        System.out.println("buffereader no ready");
      }

      in.close();
      inputStream.close();
    } catch (IOException e) {
      log.error(e.getMessage(), e);
      //successRead = false;
    }
  }

  public void testCounterFiles() {
    int filesCount = 0;
    int i = 0;

    while (i < 25) {
      filesCount++;
      i++;
      discover(filesCount);

    }

    log.info("reached " + filesCount);
  }

  public void discover(int filesCount) {
    if (filesCount > 10) {
      log.info("discovered " + filesCount);
      return;
    } else {
      log.info("less files < 10");
    }
  }

  public void testLastModifiedTimeExceededTimeout() {
    Source source = new Source();
    int timeout = 30;
    try {
      File file = TestFileUtils.createTmpFile(TestFileUtils.ROOT_TMP_DIR).toFile();
      assertTrue(source.lastModifiedTimeExceededTimeout(file.lastModified(), timeout));

      Calendar cal = Calendar.getInstance();
      cal.setTime(new Date());
      cal.add(Calendar.SECOND, -timeout - 1);
      Long timeoutAgo = cal.getTime().getTime();

      assertTrue(file.setLastModified(timeoutAgo)); // make sure new lastmodified was successfully applied
      assertFalse(source.lastModifiedTimeExceededTimeout(file.lastModified(), timeout));
    } catch (IOException e) {
      log.error(e.getMessage(), e);
    }
  }

} //endclasstest
