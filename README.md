Flume-ftp-source
================
A network server on port 21 (FTP) will be source of events for Apache-flume. Files in main directory's server will be discovered and proccessed. The source is implemented as pollable source in terms of Flume, as the polling time is configurable in the main configuration of flume's file.
In main flume's agent configuration file must be specified if security for FTP is required. There are two kind of protocol security supported by the plugin:

- FTP: File Transfer Protocol, normal plain text (insecure) but available for common use. Port 21.
- FTPS: File Transfer Protocol that uses AUTH SSL o TLS cryptographic protocols. Port 21.
- SFTP: File Transfer Protocol that uses SSH V3, via a single channel (layer transport) and sending/receiving in binary. Port 22.(Recommended).

Files proccesed will be "saved" in an external file.

### Requirements ######

- Apache-flume mayor to 1.4.0.
- commons-net-3.3.jar (ftpClient and ftpsClient)
- jsch-0.1.52.jar (channelSftp)


### Parameters for flume ######

###### Type of source for FTP/FTPS sources
>       agent.sources.ftp1.type = org.apache.flume.source.FTPSource 

###### Type of source for SFTP sources
>       agent.sources.sftp1.type = org.apache.flume.source.SFTPSource 

###### Common connection properties for FTP/FTPS/SFTP servers
>       agent.sources.ftp1.name.server = 127.0.0.1
>       agent.sources.ftp1.user = username
>       agent.sources.ftp1.password = password
>       agent.sources.ftp1.port = 21
>       agent.sources.ftp1.folder = /var/flume
>       agent.sources.ftp1.file.name = status-ftp1-file.ser
>       agent.sources.ftp1.security.enabled = true (only ftps)
>       agent.sources.ftp1.security.cipher = TLS   (only ftps)
>       agent.sources.ftp1.security.certificate.enabled = true (only ftps) (if false the plugin will accept any 
>       certificate sent by the server, validated or not).


### Specific Connection properties for SFTP server ######

###### Type of source for SFTP sources
>     agent.sources.sftp1.knownHosts = /home/username/.ssh/known_hosts

###### Discover delay, each configured milisecond directory will be explored
>     agent.sources.fpt1.run.discover.delay=10000

### License ######

Apache License, Version 2.0
http://www.apache.org/licenses/LICENSE-2.0


### Wiki ######

https://github.com/keedio/flume-ftp-source/wiki/flume-ftp-source,-especificaciones-generales-y-pruebas-iniciales

--
Luis Lázaro <lalazaro@keedio.com>

