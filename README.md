Flume-ftp-source
================
A network server on port 21 (FTP) will be source of events for Apache-flume. Files in main directory's server will be discovered and proccessed. The source is implemented a pollable source in terms of Flume, as the polling time is configurable in the main configuration of flume's file.

Requirements
------------
- Apache-flume mayor to 1.4.0.
- commons-net-3.3.jar as external dependency.

Compile
-------
mvn clean package

Configuration of Flume-ftp-source.conf
--------------------------------------
Active list \n
agent.sources = ftp1\n
agent.sinks = k1
agent.channels = c1 

Type of source for ftp sources
agent.sources.ftp1.type = org.apache.flume.source.FTPSource

Connection properties for ftp server
agent.sources.ftp1.name.server = 127.0.0.1
agent.sources.ftp1.user = username
agent.sources.ftp1.password = password

Discover delay, each configured milisecond directory will be explored
agent.sources.ftp1.run.discover.delay=10000

agent.channels.c1.type = memory
agent.channels.c1.capacity = 1000
agent.channels.c1.transactionCapacity = 100
