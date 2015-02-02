Flume-ftp-source
================
A network server on port 21 (FTP) will be source of events for Apache-flume. Files in main directory's server will be discovered and proccessed. The source is implemented a pollable source in terms of Flume, as the polling time is configurable in the main configuration of flume's file.

Requirements
------------
- Apache-flume mayor to 1.4.0.
- commons-net-3.3.jar as extedernal dependency.
