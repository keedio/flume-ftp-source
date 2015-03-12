package org.apache.flume.source.ftp.server;

import java.io.IOException;
import java.nio.file.Path;


import org.apache.flume.source.TestFileUtils;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import org.apache.ftpserver.ssl.SslConfigurationFactory;
import java.io.File;

/**
 * 
 * @author Luis Lázaro <lalazaro@keedio.com>
 */
public class EmbeddedSecureFtpServer {

    public static Path homeDirectory;
    private static  FtpServerFactory serverFactory;
    private static  ListenerFactory listenerFactory;
    private static  PropertiesUserManagerFactory userManagerFactory;
    private static  UserManager userManager;
    private static  BaseUser user;
    public static  FtpServer ftpServer;
    
    private static SslConfigurationFactory ssl;

    static {
        try {
            homeDirectory = TestFileUtils.createTmpDir();
            serverFactory = new FtpServerFactory();
            listenerFactory = new ListenerFactory();
            userManagerFactory = new PropertiesUserManagerFactory();
            userManager = userManagerFactory.createUserManager();
            user = new BaseUser();
            ssl = new SslConfigurationFactory();
            


            listenerFactory.setPort(2221);
            //ssl.setKeystoreFile(new File ("src/test/resources/ftpserver.jks"));
            //ssl.setKeystorePassword("password");
            listenerFactory.setSslConfiguration(ssl.createSslConfiguration());
            listenerFactory.setImplicitSsl(true);
            serverFactory.addListener("default", listenerFactory.createListener());

            user.setName("flumetest");
            user.setPassword("flumetest");
            user.setHomeDirectory(homeDirectory.toFile().getAbsolutePath());
            userManager.save(user);
            serverFactory.setUserManager(userManager);
            
            userManagerFactory.setFile(new File("myusers.properties"));
            serverFactory.setUserManager(userManagerFactory.createUserManager());

            ftpServer = serverFactory.createServer();

        } catch (IOException|FtpException e) {
            e.printStackTrace();
        }
    }

    @BeforeSuite
    public void initServer() throws FtpException {
        ftpServer.start();
    }

    @AfterSuite
    public void destroyServer() {
        if ( ftpServer != null && !ftpServer.isStopped() ){
            ftpServer.stop();
        }
    }
}
