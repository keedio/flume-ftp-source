Flume-ftp-source
================
A network server on port 21 (FTP) will be source of events for Apache-flume. Files in main directory's server will be discovered and proccessed. The source is implemented as pollable source in terms of Flume, as the polling time is configurable in the main configuration of flume's file.
In main flume's agent configuration file must be specified if security for FTP is required. There are two kind of protocol security supported by the plugin:

- FTP: File Transfer Protocol, normal plain text (insecure) but available for common use. Port 21.
- FTPS: File Transfer Protocol that uses AUTH SSL o TLS cryptographic protocols. Port 21.
- SFTP: File Transfer Protocol that uses SSH V3, via a single channel (layer transport) and sending/receiving in binary. Port 22.(Recommended).

Files can be proccesed int two ways:
- by lines, as one event is a full line.(flushlines = true)
- by chunk of bytes, exactly 1 KB is the size of one event. (flushlines = false) 

Proccesed files's name and size will be tracked into a Map, this one will be "saved" into an external file (file.name),
located in parameter .folder of the config.

### Requirements ######

- Apache-flume mayor to 1.4.0.
- commons-net-3.3.jar (ftpClient and ftpsClient)
- jsch-0.1.52.jar (channelSftp)


### Parameters for flume ######

###### Example configuration for FTP source

>       agent.sources.ftp1.type = org.keedio.flume.source.ftp.source.Source

>       agent.sources.ftp1.client.sources = ftp

>       agent.sources.ftp1.name.server = 127.0.0.1
>       agent.sources.ftp1.user = username
>       agent.sources.ftp1.password = password
>       agent.sources.ftp1.port = 21
>       agent.sources.ftp1.folder = /var/flume
>       agent.sources.ftp1.file.name = status-ftp1-file.ser
<<<<<<< HEAD
>       agent.sources.ftp1.security.enabled = false
>       agent.sources.ftp1.security.cipher = TLS
>       agent.sources.ftp1.security.certificate.enabled = false 


###### Example configuration for FTPS source
>       agent.sources.ftp1.type = org.apache.flume.source.FTPSource (same source as FTP) 
>       agent.sources.ftp1.name.server = 127.0.0.1
>       agent.sources.ftp1.user = username
>       agent.sources.ftp1.password = password
>       agent.sources.ftp1.port = 21
>       agent.sources.ftp1.folder = /var/flume
>       agent.sources.ftp1.file.name = status-ftp1-file.ser
>       agent.sources.ftp1.security.enabled = true 
>       agent.sources.ftp1.security.cipher = TLS
>       agent.sources.ftp1.security.certificate.enabled = (false | true)  (if false the plugin will accept any 
=======


###### Example configuration for FTPS source

>       agent.sources.ftps1.type = org.keedio.flume.source.ftp.source.Source

>       agent.sources.ftps1.client.sources = ftps

>       agent.sources.ftps1.name.server = 127.0.0.1
>       agent.sources.ftps1.user = username
>       agent.sources.ftps1.password = password
>       agent.sources.ftps1.port = 21
>       agent.sources.ftps1.folder = /var/flume
>       agent.sources.ftps1.file.name = status-ftp1-file.ser
>       agent.sources.ftps1.security.enabled = true 
>       agent.sources.ftps1.security.cipher = TLS
>       agent.sources.ftps1.security.certificate.enabled = (false | true)  (if false the plugin will accept any 
>>>>>>> feature/source-integration
>       certificate sent by the server, validated or not).

###### Example configuration for SFTP source

>       agent.sources.sftp1.type = org.keedio.flume.source.ftp.source.Source

>       agent.sources.sftp1.client.sources = sftp
 
>       agent.sources.sftp1.name.server = 127.0.0.1
>       agent.sources.sftp1.user = username
>       agent.sources.sftp1.password = password
>       agent.sources.sftp1.port = 22
>       agent.sources.sftp1.folder = /var/flume
>       agent.sources.sftp1.file.name = status-sftp1-file.ser
>       agent.sources.sftp1.knownHosts = /home/<user launching flume>/.ssh/known_hosts


###### Discover delay, each configured milisecond directory will be explored
>     agent.sources.<fpt1 | ftps1 | sftp1>.run.discover.delay=10000

###### Force flume-ftp to proccess lines instead of chunk of bytes
###### (Thanks to Erik Schmiegelow : https://github.com/schmiegelow/flume-ftp-source)
>     agent.sources.<fpt1 | ftps1 | sftp1>.flushlines = (true | false)      

###### Force the size of events in bytes. 
The default size is 1024 bytes.Customizing this option is intended for particular cases.
>     agent.sources.ftp1.chunk.size = 1024


###### For example config files check:
 https://github.com/keedio/flume-ftp-source/tree/feature/source-integration/src/main/resources/example-configs

### Version history #####
- 2.0.4 new package name convention, check for above examples. 
- 2.0.1 new configurable parameters in flume's context.
- 2.0.0: sources integration.
- 1.1.5: flush lines from SFTPSource.
- 1.1.4-rev4: added support to proccess lines instead of chunk of bytes, (standard tailing).
- 1.1.4-rev1, 1.1.4-rev2, 1.1.4-rev3: solved problem with SSL connections on servers behind fire-walls.

###### Force flume-ftp to proccess lines instead of chunk of bytes
###### (thanks to schmiegelow : https://github.com/schmiegelow/flume-ftp-source)
>     agent.sources.ftp1.flushlines = true
      

### Version history #####
1.1.5 flush lines from SFTPSource
1.1.4-rev4: added support to proccess lines instead of chunk of bytes, (standard tailing):
1.1.4-rev1, 1.1.4-rev2, 1.1.4-rev3: solved problem with SSL connections on servers behind fire-walls

### License ######

Apache License, Version 2.0
http://www.apache.org/licenses/LICENSE-2.0


### Wiki ######

https://github.com/keedio/flume-ftp-source/wiki/flume-ftp-source,-especificaciones-generales-y-pruebas-iniciales



--
Luis Lázaro <lalazaro@keedio.com>
www.keedio.com

