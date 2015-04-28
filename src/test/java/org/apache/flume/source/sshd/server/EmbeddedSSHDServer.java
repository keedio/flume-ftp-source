package org.apache.flume.source.sshd.server;



import org.apache.sshd.SshServer;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 *
 * @author Luis Lázaro lalazaro@keedio.com
 * Keedio
 */
public class EmbeddedSSHDServer {
    private SshServer sshServer = SshServer.setUpDefaultServer();
    private static final Logger log = LoggerFactory.getLogger(EmbeddedSSHDServer.class);
    
    public EmbeddedSSHDServer(){
        sshServer.setPort(2200);
        try {
        sshServer.start();
        } catch(IOException e){
            log.error("", e);
        }
    }
}
