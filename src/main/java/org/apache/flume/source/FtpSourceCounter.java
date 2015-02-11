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
    private static long files_count, filesProcCount, filesProcCountError;
    private static  final String[] ATTRIBUTES = { "files_count" , "filesProcCount", "filesProcCountError"};                 
        
    public FtpSourceCounter(String name){
       super(MonitoredCounterGroup.Type.SOURCE, name, ATTRIBUTES);
       files_count = 0;
       filesProcCount = 0;
       filesProcCountError = 0;
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
    
}
