package org.apache.flume.source.ftps.server;

import java.io.IOException;
import java.nio.file.Path;
import java.io.File;

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

/**
 * Created by luca on 30/1/15.
 */
public class EmbeddedFTPSserver {

    public static Path homeDirectory;
    private static FtpServerFactory serverFactory;
    private static ListenerFactory listenerFactory;
    private static PropertiesUserManagerFactory userManagerFactory;
    private static UserManager userManager;
    private static BaseUser user;
    public static FtpServer ftpsServer;

    static {
        try {
            homeDirectory = TestFileUtils.createTmpDir();            
            serverFactory = new FtpServerFactory();
            listenerFactory = new ListenerFactory();
            userManagerFactory = new PropertiesUserManagerFactory();
            userManager = userManagerFactory.createUserManager();
            user = new BaseUser();

            listenerFactory.setPort(2221);
            listenerFactory.setServerAddress("localhost");
            serverFactory.addListener("default", listenerFactory.createListener());

            SslConfigurationFactory ssl = new SslConfigurationFactory();
            ssl.setKeystoreFile(new File("/var/tmp/ftpserver.ser"));
            ssl.setKeystorePassword("flumetest");

            listenerFactory.setSslConfiguration(ssl.createSslConfiguration());
            listenerFactory.setImplicitSsl(true);

            serverFactory.addListener("default", listenerFactory.createListener());


            user.setName("flumetest");
            user.setPassword("flumetest");
            user.setHomeDirectory(homeDirectory.toFile().getAbsolutePath());
            userManager.save(user);
            serverFactory.setUserManager(userManager);

            ftpsServer = serverFactory.createServer();

        } catch (IOException | FtpException e) {
            e.printStackTrace();
        }
    }

    @BeforeSuite
    public void initServer() throws FtpException {
        ftpsServer.start();
    }

    @AfterSuite
    public void destroyServer() {
        if (ftpsServer != null && !ftpsServer.isStopped()) {
            ftpsServer.stop();
        }
    }

}
