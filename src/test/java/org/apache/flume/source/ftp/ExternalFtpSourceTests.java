//package org.apache.flume.source.ftp;
//
//import org.apache.flume.EventDeliveryException;
//import org.apache.flume.PollableSource;
//import org.testng.Assert;
//import org.testng.annotations.Test;
//
///**
// * Integration tests that will connect to an external FTP server.
// *
// */
//public class ExternalFtpSourceTests extends AbstractFtpSourceTest {
//    int getPort = 21;
//    String getPassword = "anonymous";
//    String getUser = "anonymous";
//
//    @Test(enabled = false)
//    public void symLinkTest() {
//        PollableSource.Status proc0 = null;
//        try {
//            proc0 = ftpSource.process();
//            Assert.assertEquals(PollableSource.Status.READY, proc0);
//            Assert.assertEquals(ftpSourceCounter.getFilesCount(), 1);
//            Assert.assertEquals(ftpSourceCounter.getFilesProcCount(), 1);
//            Assert.assertEquals(ftpSourceCounter.getFilesProcCountError(), 0);
//        } catch (EventDeliveryException e) {
//            Assert.fail();
//        }
//    }
//
//}
