/*****************************************************************
* FptSourceCounter exposes custom metrics for specific source FTP
************/

package org.keedio.flume.source.ftp.metrics;

import org.apache.flume.instrumentation.MonitoredCounterGroup;
/**
 * 
 * @author Luis Lázaro <lalazaro@keedio.com>
 */
public class SourceCounter extends MonitoredCounterGroup implements SourceCounterMBean {
    
    private static long filesCount;
    private static long filesProcCount ;  
    private static long filesProcCountError;
    private static long eventCount;
    private static long sendThroughput;
    private static long startTime;
    private static long lastSent;
    private static long countModProc;
    private static long bytesProcessed;
    private static long kbProcessed;
    private static long mbProcessed;
            
   
    
    private static  final String[] ATTRIBUTES = { "files_count" , "filesProcCount", "filesProcCountError", 
        "eventCount","start_time","last_sent", "sendThroughput", "countModProc", "bytesProcessed", "KbProcessed", "MbProcessed"
    };                 
        
    /**
     *
     * @param name
     */
    public SourceCounter(String name){
       super(MonitoredCounterGroup.Type.SOURCE, name, ATTRIBUTES);
       filesCount = 0;
       filesProcCount = 0;
       filesProcCountError = 0;
       eventCount = 0;
       lastSent = 0;
       startTime = System.currentTimeMillis();
       sendThroughput = 0;
       countModProc = 0;
       bytesProcessed = 0;
       kbProcessed = 0;
       mbProcessed = 0;
    }
            
    /**
    @return long, number of files discovered
    */
    @Override
    public long getFilesCount(){
        return filesCount;
    }
    
    /**
    @void, increment count of files
    */
    @Override
    public void  incrementFilesCount(){
        filesCount++;
    }
    
    /**
    @return long, files succesfully
    */
    @Override
    public long getFilesProcCount(){
        return filesProcCount;
    }
    
    /**
    @void, increment count of proc files    
    */
    @Override
    public void incrementFilesProcCount(){
        filesProcCount++;
    }
    
     /**
    @return long, files proc with error
    */
    @Override
    public long getFilesProcCountError(){
        return filesProcCountError;
    }
    
    /**
    @void, increment count of proc files with error    
    */
    @Override
    public void incrementFilesProcCountError(){
        filesProcCountError++;
    }
    
    /**
     *@void
     */
    @Override
    public void incrementEventCount(){
        lastSent = System.currentTimeMillis();
        eventCount++;
       if (lastSent - startTime >= 1000) {
           long secondsElapsed = (lastSent - startTime) / 1000;
           sendThroughput = eventCount / secondsElapsed;
        }
    }
    
    /**
     * 
     * @return  long
     */
    
    @Override
    public long getEventCount(){
        return eventCount;
    }
    
    /**
     * 
     * @return 
     */
    @Override
    public long getSendThroughput() {
        return sendThroughput;
    }
    
    /**
     */
    @Override
    public void incrementCountModProc(){
        countModProc++;
    }
    
    /**
     * 
     * @return 
     */
    @Override
    public long getCountModProc(){
        return countModProc;
    }
    
    /**
     * 
     * @return 
     */
    @Override
    public long getLastSent(){
        return lastSent;
    }
    
    /**
     * 
     * @param size 
     */
    @Override
    public void incrementCountSizeProc(long size){
        bytesProcessed+= size;
    }

    /**
     * 
     * @return 
     */
    @Override
    public long getCountSizeProc(){
        return bytesProcessed;
    }
    
    /**
     * 
     * @return 
     */
    @Override
    public long getCountSizeProcKb(){
        kbProcessed = getCountSizeProc() / 1024;
        return kbProcessed;
    }
    
    /**
     * 
     * @return 
     */
    @Override
    public long getCountSizeProcMb(){
        mbProcessed = getCountSizeProc() / (1024 * 1024);
        return mbProcessed;
    }
       
    
    
   
}
