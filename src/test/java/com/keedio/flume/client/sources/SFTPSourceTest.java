/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.keedio.flume.client.sources;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.io.InputStream;
import java.util.List;
import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.sshd.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;

import org.apache.sshd.common.*;
import org.apache.sshd.server.UserAuth;
import org.apache.sshd.server.auth.UserAuthNone;
import org.apache.sshd.server.auth.UserAuthPassword;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.session.*;

import org.apache.sshd.server.command.ScpCommandFactory;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.sftp.SftpSubsystem;

import java.io.IOException;
import java.nio.file.Path;
import org.apache.flume.source.TestFileUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import java.util.List;
import java.util.ArrayList;

/**
 *
 * @author luislazaro
 */
public class SFTPSourceTest extends TestCase {

    private static final Logger log = LoggerFactory.getLogger(SFTPSourceTest.class);

    public SFTPSourceTest(String testName) {
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

    /**
     * Test of connect method, of class SFTPSource.
     */
//    public void testConnect() {
//        String knownHosts = "/Users/luislazaro/.ssh/known_hosts";
//        JSch jsch = new JSch();
//        Session sessionSftp = null;
//        Channel channel = null;
//        ChannelSftp sftpClient = null;
//        System.out.println("connect");
//        SFTPSource instance = new SFTPSource();
//        try {
//            jsch.setKnownHosts(knownHosts);
//            sessionSftp = jsch.getSession("flumetest", "localhost");
//            sessionSftp.setPassword("flumetest");
//            sessionSftp.setPort(22);
//            sessionSftp.connect();
//            if (sessionSftp.isConnected()) {
//                channel = sessionSftp.openChannel("sftp");
//                channel.connect();
//                if (channel.isConnected()) {
//                    sftpClient = (ChannelSftp) channel;
//                    instance.setConnected(true);
//                }
//            }
//        } catch (JSchException e) {
//
//        }
//        assertTrue(instance.isConnected());
//
//    }

    public void testsetupSftpServer() {
        System.out.println("testSetupSftpServer");
        SshServer sshd = SshServer.setUpDefaultServer();
        sshd.setPort(22);
        sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider("hostkey.ser"));

        List<NamedFactory<UserAuth>> userAuthFactories = new ArrayList<NamedFactory<UserAuth>>();
        userAuthFactories.add(new UserAuthPassword.Factory());
        sshd.setUserAuthFactories(userAuthFactories);
        sshd.setPasswordAuthenticator(new PasswordAuthenticator() {
            public boolean authenticate(String username, String password, ServerSession session) {
                return "tomek".equals(username) && "123".equals(password);
            }
        });

        sshd.setCommandFactory(new ScpCommandFactory());

        List<NamedFactory<Command>> namedFactoryList = new ArrayList<NamedFactory<Command>>();
        namedFactoryList.add(new SftpSubsystem.Factory());
        sshd.setSubsystemFactories(namedFactoryList);

        try {
            sshd.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//
//    /**
//     * Test of getKnownHosts method, of class SFTPSource.
//     */
//    public void testGetKnownHosts() {
//        System.out.println("getKnownHosts");
//        SFTPSource instance = new SFTPSource();
//        String expResult = "";
//        String result = instance.getKnownHosts();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setKnownHosts method, of class SFTPSource.
//     */
//    public void testSetKnownHosts() {
//        System.out.println("setKnownHosts");
//        String knownHosts = "";
//        SFTPSource instance = new SFTPSource();
//        instance.setKnownHosts(knownHosts);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getJsch method, of class SFTPSource.
//     */
//    public void testGetJsch() {
//        System.out.println("getJsch");
//        SFTPSource instance = new SFTPSource();
//        JSch expResult = null;
//        JSch result = instance.getJsch();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setJsch method, of class SFTPSource.
//     */
//    public void testSetJsch() {
//        System.out.println("setJsch");
//        JSch jsch = null;
//        SFTPSource instance = new SFTPSource();
//        instance.setJsch(jsch);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getSessionSftp method, of class SFTPSource.
//     */
//    public void testGetSessionSftp() {
//        System.out.println("getSessionSftp");
//        SFTPSource instance = new SFTPSource();
//        Session expResult = null;
//        Session result = instance.getSessionSftp();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setSessionSftp method, of class SFTPSource.
//     */
//    public void testSetSessionSftp() {
//        System.out.println("setSessionSftp");
//        Session sessionSftp = null;
//        SFTPSource instance = new SFTPSource();
//        instance.setSessionSftp(sessionSftp);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getChannel method, of class SFTPSource.
//     */
//    public void testGetChannel() {
//        System.out.println("getChannel");
//        SFTPSource instance = new SFTPSource();
//        Channel expResult = null;
//        Channel result = instance.getChannel();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setChannel method, of class SFTPSource.
//     */
//    public void testSetChannel() {
//        System.out.println("setChannel");
//        Channel channel = null;
//        SFTPSource instance = new SFTPSource();
//        instance.setChannel(channel);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getSftpClient method, of class SFTPSource.
//     */
//    public void testGetSftpClient() {
//        System.out.println("getSftpClient");
//        SFTPSource instance = new SFTPSource();
//        ChannelSftp expResult = null;
//        ChannelSftp result = instance.getSftpClient();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setSftpClient method, of class SFTPSource.
//     */
//    public void testSetSftpClient() {
//        System.out.println("setSftpClient");
//        ChannelSftp sftpClient = null;
//        SFTPSource instance = new SFTPSource();
//        instance.setSftpClient(sftpClient);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of disconnect method, of class SFTPSource.
//     */
//    public void testDisconnect() {
//        System.out.println("disconnect");
//        SFTPSource instance = new SFTPSource();
//        instance.disconnect();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of changeToDirectory method, of class SFTPSource.
//     */
//    public void testChangeToDirectory() {
//        System.out.println("changeToDirectory");
//        String directory = "";
//        SFTPSource instance = new SFTPSource();
//        instance.changeToDirectory(directory);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of listFiles method, of class SFTPSource.
//     */
//    public void testListFiles() {
//        System.out.println("listFiles");
//        String directory = "";
//        SFTPSource instance = new SFTPSource();
//        List<Object> expResult = null;
//        List<Object> result = instance.listFiles(directory);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getInputStream method, of class SFTPSource.
//     */
//    public void testGetInputStream() throws Exception {
//        System.out.println("getInputStream");
//        Object file = null;
//        SFTPSource instance = new SFTPSource();
//        InputStream expResult = null;
//        InputStream result = instance.getInputStream(file);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getObjectName method, of class SFTPSource.
//     */
//    public void testGetObjectName() {
//        System.out.println("getObjectName");
//        Object file = null;
//        SFTPSource instance = new SFTPSource();
//        String expResult = "";
//        String result = instance.getObjectName(file);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of isDirectory method, of class SFTPSource.
//     */
//    public void testIsDirectory() {
//        System.out.println("isDirectory");
//        Object file = null;
//        SFTPSource instance = new SFTPSource();
//        boolean expResult = false;
//        boolean result = instance.isDirectory(file);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of isFile method, of class SFTPSource.
//     */
//    public void testIsFile() {
//        System.out.println("isFile");
//        Object file = null;
//        SFTPSource instance = new SFTPSource();
//        boolean expResult = false;
//        boolean result = instance.isFile(file);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of particularCommand method, of class SFTPSource.
//     */
//    public void testParticularCommand() {
//        System.out.println("particularCommand");
//        SFTPSource instance = new SFTPSource();
//        boolean expResult = false;
//        boolean result = instance.particularCommand();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getObjectSize method, of class SFTPSource.
//     */
//    public void testGetObjectSize() {
//        System.out.println("getObjectSize");
//        Object file = null;
//        SFTPSource instance = new SFTPSource();
//        long expResult = 0L;
//        long result = instance.getObjectSize(file);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of isLink method, of class SFTPSource.
//     */
//    public void testIsLink() {
//        System.out.println("isLink");
//        Object file = null;
//        SFTPSource instance = new SFTPSource();
//        boolean expResult = false;
//        boolean result = instance.isLink(file);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getLink method, of class SFTPSource.
//     */
//    public void testGetLink() {
//        System.out.println("getLink");
//        Object file = null;
//        SFTPSource instance = new SFTPSource();
//        String expResult = "";
//        String result = instance.getLink(file);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getDirectoryserver method, of class SFTPSource.
//     */
//    public void testGetDirectoryserver() {
//        System.out.println("getDirectoryserver");
//        SFTPSource instance = new SFTPSource();
//        String expResult = "";
//        String result = instance.getDirectoryserver();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getClientSource method, of class SFTPSource.
//     */
//    public void testGetClientSource() {
//        System.out.println("getClientSource");
//        SFTPSource instance = new SFTPSource();
//        Object expResult = null;
//        Object result = instance.getClientSource();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setFileType method, of class SFTPSource.
//     */
//    public void testSetFileType() throws Exception {
//        System.out.println("setFileType");
//        int fileType = 0;
//        SFTPSource instance = new SFTPSource();
//        instance.setFileType(fileType);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
// 
}
