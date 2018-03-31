Flume-ftp-source
================
A network server on port 21 (FTP) will be source of events for Apache-flume. Files in main directory's server will be discovered and proccessed. The source is implemented as pollable source in terms of Flume, as the polling time is configurable in the main configuration of flume's file.
In main flume's agent configuration file must be specified if security for FTP is required. There are two kind of protocol security supported by the plugin:

- FTP: File Transfer Protocol, normal plain text (insecure) but available for common use. Port 21.
- FTPS: File Transfer Protocol that uses AUTH SSL o TLS cryptographic protocols. Port 21.
- SFTP: File Transfer Protocol that uses SSH V3, via a single channel (layer transport) and sending/receiving in binary. Port 22.(Recommended).

Files can be processed in two ways:
- by lines, as one event is a full line.(flushlines = true)
- by chunk of bytes, exactly 1 KB is the size of one event by default. (flushlines = false)

Proccesed files's name and size will be tracked into a Map, this one will be "saved" into an external file (file.name),
located in parameter .folder of the config.

### Requirements ######

- [Apache-flume mayor to 1.4.0.](http://archive.apache.org/dist/flume/)
- [commons-net-3.3.jar](http://central.maven.org/maven2/commons-net/commons-net/3.3/commons-net-3.3.jar) (ftpClient and ftpsClient)
- [jsch-0.1.54.jar](http://central.maven.org/maven2/com/jcraft/jsch/0.1.54/jsch-0.1.54.jar) (channelSftp)

### Compilation and packaging###
1.**Clone the project:**
```
git clone https://github.com/keedio/flume-ftp-source.git
```

2.**Build with Maven:**
```
mvn clean package
```

### Deployment and launching ###
1. **[flume-ftp-source-X.Y.Z.jar](https://github.com/keedio/flume-ftp-source/tree/master)**
2. **[Download Apache Flume](http://archive.apache.org/dist/flume/).**
3. **[Create plugins.d directory](https://flume.apache.org/FlumeUserGuide.html#the-plugins-d-directory).**
4. **[Directory layout for plugins](https://flume.apache.org/FlumeUserGuide.html#directory-layout-for-plugins):**

    ```
    $ cd plugins.d
    $ mkdir flume-ftp
    $ cd flume-ftp
    $ mkdir lib libext
    $ cp jsch-0.1.54.jar libext/
    $ cp commons-net-3.3.jar libext/
    $ cp flume-ftp-source-X.Y.Z.jar lib/
     ```

 5. **[Create a config file, examples](https://github.com/keedio/flume-ftp-source/tree/master/src/main/resources/example-configs).**

     ```
     $ cp flume-ng-ftp-source-FTP.conf  apache-flume-1.4.0-bin/conf/
     ```

 6. **Which files will be processed?**

    Files in Ftp's user directory will be processed (Remote Directory).
    For example, if sever and user :
    `agent.sources.ftp1.name.server = 192.168.0.2`
    `agent.sources.ftp1.user = mortadelo`

    ```
    host:~ root# ftp 192.168.0.2
    Connected to 192.168.0.2.
    220 (vsFTPd 3.0.2)
    Name (192.168.0.2:root): mortadelo
    331 Please specify the password.
    Password:
    230 Login successful.
    Remote system type is UNIX.
    Using binary mode to transfer files.
    ftp> dir
    229 Entering Extended Passive Mode (|||29730|).
    150 Here comes the directory listing.
    -rw-r--r--    1 0        0              60 Aug 18 06:48 file1.txt
    -rw-r--r--    1 0        0              60 Aug 18 06:48 file2.txt
    226 Directory send OK.
    ftp> pwd
    Remote directory: /
    ftp>
    ```
    we want to process file1.txt and file2.txt

 7. **Launch flume binary:**
     ```
    $ ./bin/flume-ng agent -c conf -conf-file conf/flume-ng-ftp-source-FTP.conf --name agent -Dflume.root.logger=INFO,console
     ```

     ```
      [...]
      2017-08-18 09:07:33,471 (PollableSourceRunner-Source-ftp1) [INFO - org.keedio.flume.source.ftp.source.Source.process(Source.java:89)] Actual dir:  / files: 0
      2017-08-18 09:07:33,503 (PollableSourceRunner-Source-ftp1) [INFO - org.keedio.flume.source.ftp.source.Source.discoverElements(Source.java:193)] Discovered: file1.txt ,size: 60
      2017-08-18 09:07:33,516 (PollableSourceRunner-Source-ftp1) [INFO - org.keedio.flume.source.ftp.source.Source.discoverElements(Source.java:232)] Processed:  file1.txt ,total files: 1

      2017-08-18 09:07:33,518 (PollableSourceRunner-Source-ftp1) [INFO - org.keedio.flume.source.ftp.source.Source.discoverElements(Source.java:193)] Discovered: file2.txt ,size: 60
      2017-08-18 09:07:33,521 (PollableSourceRunner-Source-ftp1) [INFO - org.keedio.flume.source.ftp.source.Source.discoverElements(Source.java:232)] Processed:  file2.txt ,total files: 2

      2017-08-18 09:07:38,526 (PollableSourceRunner-Source-ftp1) [INFO - org.keedio.flume.source.ftp.source.Source.process(Source.java:89)] Actual dir:  / files: 2
      2017-08-18 09:07:43,535 (PollableSourceRunner-Source-ftp1) [INFO - org.keedio.flume.source.ftp.source.Source.process(Source.java:89)] Actual dir:  / files: 2
      2017-08-18 09:07:48,547 (PollableSourceRunner-Source-ftp1) [INFO - org.keedio.flume.source.ftp.source.Source.process(Source.java:89)] Actual dir:  / files: 2
      [...]
      ```
 8.  **Data processed.**

     For testing porposes set:
     ```
     agent.sinks.k1.type = file_roll
     agent.sinks.k1.sink.directory = /var/log/flume-ftp
     ```
     in /var/log/flume-ftp, flume will create a file ( [file_roll](https://flume.apache.org/FlumeUserGuide.html#file-roll-sink) ) as 123456789...-
     ```
     [host]# ls -ll
     -rw-r--r-- 1 root root 120 Aug 18 09:07 1503040052934-1
     tail -f 1503040052934-1
     line from file1.txt Fri_Aug_18_06:48:40.1503038920_UTC_2017
     line from file2.txt Fri_Aug_18_06:48:51.1503038931_UTC_2017
     ```
 9. **Stop and start processing files from the latest information unprocessed.**

    In config file, parameters
    `agent.sources.ftp1.folder = /var/log/flume-ftp`
    `agent.sources.ftp1.file.name = status-ftp1-file.ser`

    configure the path for the file that will keep a track status of files and
    information processed.
    For example, if stopping flume-ng and restarting,  file1.txt and file2.txt will not be
    discovered again. With flume stopped i appended a new line to file1.txt.

    ```
    2017-08-18 09:48:50,633 (lifecycleSupervisor-1-0) [INFO - org.apache.flume.instrumentation.MonitoredCounterGroup.start(MonitoredCounterGroup.java:95)] Component type: SOURCE, name: SOURCE.ftp1 started
    2017-08-18 09:48:50,638 (PollableSourceRunner-Source-ftp1) [INFO - org.keedio.flume.source.ftp.source.Source.process(Source.java:89)] Actual dir:  / files: 2
    2017-08-18 09:48:50,665 (PollableSourceRunner-Source-ftp1) [INFO - org.keedio.flume.source.ftp.source.Source.discoverElements(Source.java:200)] Modified: file1.txt ,size: 60
    2017-08-18 09:48:50,672 (PollableSourceRunner-Source-ftp1) [INFO - org.keedio.flume.source.ftp.source.Source.discoverElements(Source.java:232)] Processed:  file1.txt ,total files: 2
    ```
 10. **Whether a recursive listing should be performed**

    In config file, parameter `agent.sources.sftp1.search.recursive = false` (by default, this is `true`) specifies that a recursive search should not be performed in `agent.sources.sftp1.working.directory`.
 11. **Wait for files to be finalized before reading**

    This is useful when large files are being written to the source server, especially compressed files. To avoid reading them while they're still being written to, specify the parameter `agent.sources.sftp1.search.processInUse = false` in config file. This *must* be accompanied by another parameter - `agent.sources.sftp1.search.processInUseTimeout`, which is specified in seconds. To determine if a file is still being written to, the Flume agent will check the file's last modified timestamp. If the file was modified within `search.processInUseTimeout` seconds ago, it will be considered as still being written to. A value of 30 is usually sufficienly conservative.

    ```
    INFO	Source
    File testfile.csv.gz is still being written. Will skip for now and re-read when write is completed.
    INFO	Source
    Actual dir:  /home/mydir files: 24
    INFO	Source
    Discovered: testfile.csv.gz ,size: 5441264
    INFO	HDFSDataStream
    Serializer = TEXT, UseRawLocalFileSystem = false
    ```
 12. **Decompress source files on the fly**

    In many cases, source files may be compressed using a codec such as `GZIP`. Reading such files in chunks or lines may not be useful. To decompress such files on the fly, provide the parameter `agent.sources.sftp1.compressed` in the config file, with its value as the name of the compression codec used (e.g., `agent.sources.sftp1.compressed = gzip`). This will cause the Flume agent to read and decompress the source files on the fly and make the decompressed data available in a channel.

    ```
    Discovered: testfile.csv.gz ,size: 5441264
    INFO	Source
    File testfile.csv.gz is GZIP compressed, and decompression has been requested by user. Will attempt to decompress.
    INFO	HDFSDataStream
    Serializer = TEXT, UseRawLocalFileSystem = false
    ```

## Mandatory Parameters for flume ######

###### Example configuration for FTP source

>       agent.sources.ftp1.type = org.keedio.flume.source.ftp.source.Source

>       agent.sources.ftp1.client.source = ftp

>       agent.sources.ftp1.name.server = 127.0.0.1
>       agent.sources.ftp1.user = username
>       agent.sources.ftp1.password = password
>       agent.sources.ftp1.port = 21


###### Example configuration for FTPS source

>       agent.sources.ftps1.type = org.keedio.flume.source.ftp.source.Source

>       agent.sources.ftps1.client.source = ftps

>       agent.sources.ftps1.name.server = 127.0.0.1
>       agent.sources.ftps1.user = username
>       agent.sources.ftps1.password = password
>       agent.sources.ftps1.port = 21
>       
>       agent.sources.ftps1.security.enabled = true
>       agent.sources.ftps1.security.cipher = TLS
>       agent.sources.ftps1.security.certificate.enabled = (false | true)  (if false the plugin will accept any
>       certificate sent by the server, validated or not).
>       agent.sources.ftps1.path.keystore = /paht/to/keystore
>       agent.sources.ftps1.store.pass = the_keyStore_password

###### Example configuration for SFTP source

>       agent.sources.sftp1.type = org.keedio.flume.source.ftp.source.Source

>       agent.sources.sftp1.client.source = sftp

>       agent.sources.sftp1.name.server = 127.0.0.1
>       agent.sources.sftp1.user = username
>       agent.sources.sftp1.password = password
>       agent.sources.sftp1.port = 22
>
>       agent.sources.sftp1.strictHostKeyChecking = no // WARNING: for testing porposes only, default is yes
>       agent.sources.sftp1.knownHosts = /home/<user launching flume>/.ssh/known_hosts


### Optional Parameters for flume ######
###### Working directory for searching for files.
working.directory is under root directory server returned by FTP server:
>     agent.soures.<fpt1 | ftps1 | sftp1>.working.directory = [remote_directory]/directoryName

example 1:
>     agent.soures.fpt1.working.directory = /directory_flume_files

example 2:
>     agent.soures.sftp1.working.directory = /home/user/directory_flume_files

###### Discover delay, each configured milisecond directory will be explored.
If this parameter is omitted, default value will be set to 10000 ms.
>     agent.sources.<fpt1 | ftps1 | sftp1>.run.discover.delay=5000


###### Force flume-ftp to proccess lines instead of chunk of bytes.If omitted: true.
###### (Thanks to Erik Schmiegelow : https://github.com/schmiegelow/flume-ftp-source)
>     agent.sources.<fpt1 | ftps1 | sftp1>.flushlines = (true | false)      


###### Force the size of events in bytes. I omitted, default is 1KB.
Customizing this option is intended for particular cases.
>     agent.sources.ftp1.chunk.size = 1024

###### File's name that keeps track of files and sizes processed.
 If omitted, a default one will be created.
>      agent.sources.ftp1.file.name = status-ftp1-file.ser


###### Directory where to keep the file track status. If omitted, java.io.tmpdir will be used.
>      agent.sources.ftp1.folder = /var/flume


###### Match specific files's name according Java Regex:
 [Java Regular Expressions](https://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html)  for FTP, FTPS and SFTP protocols.

example 1:
>      agent.sources.ftp1.filter.pattern = .+\\.csv ----> only process files ends with

example 2:
>      agent.sources.sftp1.filter.pattern = flume_file.* ----> only process files starts with

###### For examples configs files, check:
 [example configurations](https://github.com/keedio/flume-ftp-source/tree/master/src/main/resources/example-configs)

### Mandatory parameters depending on the chosen source  ######
m : stands for parameter is mandatory for above source

o : optional

x : not available

|Parameter|Description|ftp|ftps|sftp|
|------|-----------|---|----|----|
|client.source|type of source from where get data|m|m|m|
|name.server| hostname or ipaddress|m|m|m|
|user|username allowed to connect|m|m|m|
|password|usenames's pass|m|m|m|
|port|server's port to connect|m|m|m|
|security.enabled|cryptographic protocols|x|m|x|
|security.cipher|Auth SSL or TLS|x|m|x|
|security.certificate.enabled|accept or not server's certificate|x|o|x|
|path.keystore|folder to keep keystory|x|o|x|
|knownHosts|keys|x|x|m|
|working.directory|custom directory to search for files|o|o|x|
|folder|directory where to keep track status files|o|o|o|
|discover.delay|polling time|o|o|o|
|chunk.size|for binary files size of event|o|o|o|
|file.name|file's name allocated in folder for track status|o|o|o|
|flushlines|true or false|m|m|m|
|search.recursive|true or false|o|o|o|
|search.processInUse|true or false|o|o|o|
|search.processInUseTimeout|time in seconds to determine busyness of files|o|o|o|
|sftp1.compressed|if source files are compressed, compression format|o|o|o|
|filter.pattern| [Java Regular Expression](https://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html) |o|o|o|
|strictHostKeyChecking| Disable verifying public key of the SSH protocol (for testing only)|x|x|o|


### Version history #####
- 2.1.2
    + Improvement: Added configurable property - search.recursive - to search recursively
    + Improvement: Added configurable properties - search.processInUse and search.processInUseTimeout -
    to allow user to specify whether files being written to should or should not be processed
    + Improvement: Added configurable property - compressed - to let user specify a compression format. This
    enables decompression on-the-fly.
- 2.1.1
    + Improvement: SFTP's filter.pattern parameter works now with Java Regex instead of Glob Pattern Wildcars.
    + Fix bug sftp source: if setting sftp.filter.pattern to some value, walking subdirectory recursiverly does no work properly.
  - 2.1.0
    + property filter.pattern for processing only the files which meet some criteria.
    + property working.directory for searching for files is now configurable.
- 2.0.10
    + Flume core: upgrade to Apache Flume 1.7.0
    + Source: add file's name and path to event header
    + Jsch: upgrade Jsch to 0.1.54 for new host key type (ecdsa-sha2-nistp256)
    + Jsch: add configurable parameter for testing: strictHostKeyChecking.
- 2.0.9 several fixes - check PRS
- 2.0.8 fix on readme file.
- 2.0.5 fixes minor bugs of 2.0.4.
- 2.0.4 new package name convention, check for above examples.
- 2.0.1 new configurable parameters in flume's context.
- 2.0.0: sources integration.
- 1.1.5: flush lines from SFTPSource.
- 1.1.4-rev4: added support to proccess lines instead of chunk of bytes, (standard tailing).
- 1.1.4-rev1, 1.1.4-rev2, 1.1.4-rev3: solved problem with SSL connections on servers behind fire-walls.


### Wiki ######

https://github.com/keedio/flume-ftp-source/wiki/flume-ftp-source,-especificaciones-generales-y-pruebas-iniciales



--
www.keedio.com
