/*****************************************************************
* FptSourceCounter exposes custom metrics for specific source FTP
************/

package org.apache.flume.source;

import org.apache.flume.instrumentation.MonitoredCounterGroup;
/**
 * 
 * @author Luis Lázaro <lalazaro@keedio.com>
 */
public class FtpSourceCounter extends MonitoredCounterGroup implements FtpSourceCounterMBean {
    private static long files_count, filesProcCount, filesProcCountError, eventCount, sendThroughput,start_time,last_sent;
    private static  final String[] ATTRIBUTES = { "files_count" , "filesProcCount", "filesProcCountError", "eventCount","start_time","last_sent"};                 
        
    public FtpSourceCounter(String name){
       super(MonitoredCounterGroup.Type.SOURCE, name, ATTRIBUTES);
       files_count = 0;
       filesProcCount = 0;
       filesProcCountError = 0;
       eventCount = 0;
       last_sent = 0;
       setStartTime();
    }
            
    /*
    @return long, number of files discovered
    */
    @Override
    public long getFilesCount(){
        return files_count;
    }
    
    /*
    @void, increment count of files
    */
    @Override
    public void  incrementFilesCount(){
        files_count++;
    }
    
    /*
    @return long, files succesfully
    */
    @Override
    public long getFilesProcCount(){
        return filesProcCount;
    }
    
    /*
    @void, increment count of proc files    
    */
    @Override
    public void incrementFilesProcCount(){
        filesProcCount++;
    }
    
     /*
    @return long, files proc with error
    */
    @Override
    public long getFilesProcCountError(){
        return filesProcCountError;
    }
    
    /*
    @void, increment count of proc files with error    
    */
    @Override
    public void incrementFilesProcCountError(){
        filesProcCountError++;
    }
    
    @Override
    public void incrementEventCount(){
        last_sent = System.currentTimeMillis();
        eventCount++;
    }
    
    @Override
    public long getEventCount(){
        return eventCount;
    }
    
    @Override
    public long getSendThroughput() {
        return sendThroughput;
    }
    
    @Override
    public long setStartTime() {
        start_time = System.currentTimeMillis();
        return start_time;
    }
    
    @Override
    public void setSendThroughput(){
        if (last_sent > start_time) {
            sendThroughput = eventCount / ((last_sent - start_time) / 1000);
        }
    }
}
