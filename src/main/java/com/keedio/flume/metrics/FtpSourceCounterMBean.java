/*
 * KEEDIO
 */
package com.keedio.flume.metrics;
/**
 *
 * @author Luis Lázaro <lalazaro@keedio.com>
 */
public interface FtpSourceCounterMBean {
    public long getFilesCount();
    public void incrementFilesCount();
    public long getFilesProcCount();
    public void incrementFilesProcCount();
    public long getFilesProcCountError();
    public void incrementFilesProcCountError();
    public long getEventCount();
    public void incrementEventCount();
    public long getSendThroughput();
    public void incrementCountModProc();
    public long getCountModProc();
    public long getMbProcessed();
    public long getKbProcessed();
    public long getLastSent();
}