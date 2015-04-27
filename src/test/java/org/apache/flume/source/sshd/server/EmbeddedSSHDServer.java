package org.apache.flume.source.sshd.server;


import org.apache.sshd.SshServer;
/**
 *
 * @author Luis Lázaro lalazaro@keedio.com
 * Keedio
 */
public class EmbeddedSSHDServer {
    private SshServer sshServer = SshServer.setUpDefaultServer();
    
    public EmbeddedSSHDServer(){
        //sshServer.setPort(22);
    }
}
