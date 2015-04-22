/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.flume.source;

import java.io.InputStream;
import junit.framework.TestCase;
import org.apache.flume.Context;
import org.apache.flume.PollableSource;
import org.apache.flume.client.KeedioSource;
import org.apache.flume.metrics.FtpSourceCounter;
import org.apache.flume.source.utils.FTPSourceEventListener;

/**
 *
 * @author luislazaro
 */
public class SourceTest extends TestCase {
    
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

//    /**
//     * Test of orderKeedioSource method, of class Source.
//     */
//    public void testOrderKeedioSource() {
//        System.out.println("orderKeedioSource");
//        Context context = new Context();
//        //Source instance = new Source();
//        System.out.println(context.getInteger("port"));
//        
//    }

//    /**
//     * Test of configure method, of class Source.
//     */
//    public void testConfigure() {
//        System.out.println("configure");
//        Context context = null;
//        Source instance = new Source();
//        instance.configure(context);
//       
//    }
//
//    /**
//     * Test of process method, of class Source.
//     */
//    public void testProcess() throws Exception {
//        System.out.println("process");
//        Source instance = new Source();
//        PollableSource.Status expResult = null;
//        PollableSource.Status result = instance.process();
//        assertEquals(expResult, result);
//       
//    }
//
//    /**
//     * Test of start method, of class Source.
//     */
//    public void testStart() {
//        System.out.println("start");
//        Source instance = new Source();
//        instance.start();
//       
//    }
//
//    /**
//     * Test of stop method, of class Source.
//     */
//    public void testStop() {
//        System.out.println("stop");
//        Source instance = new Source();
//        instance.stop();
//        
//    }
//
//    /**
//     * Test of discoverElements method, of class Source.
//     */
//    public void testDiscoverElements() throws Exception {
//        System.out.println("discoverElements");
//        KeedioSource keedioSource = null;
//        String parentDir = "";
//        String currentDir = "";
//        int level = 0;
//        Source instance = new Source();
//        instance.discoverElements(keedioSource, parentDir, currentDir, level);
//        
//    }
//
    /**
     * Test of readStream method, of class Source.
     */
    public void testReadStream() {
        System.out.println("readStream");
        InputStream inputStream = null;
        long position = 0L;
        Source instance = new Source();
        instance.readStream(inputStream, position);
        
    }
//
//    /**
//     * Test of processMessage method, of class Source.
//     */
//    public void testProcessMessage() {
//        System.out.println("processMessage");
//        byte[] lastInfo = null;
//        Source instance = new Source();
//        instance.processMessage(lastInfo);
//        
//    }
//
//    /**
//     * Test of setListener method, of class Source.
//     */
//    public void testSetListener() {
//        System.out.println("setListener");
//        FTPSourceEventListener listener = null;
//        Source instance = new Source();
//        instance.setListener(listener);
//        
//    }
//
//    /**
//     * Test of handleProcessError method, of class Source.
//     */
//    public void testHandleProcessError() {
//        System.out.println("handleProcessError");
//        String fileName = "";
//        Source instance = new Source();
//        instance.handleProcessError(fileName);
//        
//    }
//
//    /**
//     * Test of setFtpSourceCounter method, of class Source.
//     */
//    public void testSetFtpSourceCounter() {
//        System.out.println("setFtpSourceCounter");
//        FtpSourceCounter ftpSourceCounter = null;
//        Source instance = new Source();
//        instance.setFtpSourceCounter(ftpSourceCounter);
//        
//    }
//
//    /**
//     * Test of getKeedioSource method, of class Source.
//     */
//    public void testGetKeedioSource() {
//        System.out.println("getKeedioSource");
//        Source instance = new Source();
//        KeedioSource expResult = null;
//        KeedioSource result = instance.getKeedioSource();
//        assertEquals(expResult, result);
//        
//    }
//    
}
