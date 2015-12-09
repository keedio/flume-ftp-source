/*
    KEEDIO
*/
package org.keedio.flume.source.ftp.metrics;
/**
 *
 * @author Luis Lázaro <lalazaro@keedio.com>
 */
public interface SourceCounterMBean {
    /**
     * 
     * @return 
     */
    public long getFilesCount();
    
    /**
     *
     */
    public void incrementFilesCount();

    /**
     *
     * @return
     */
    public long getFilesProcCount();

    /**
     *
     */
    public void incrementFilesProcCount();

    /**
     *
     * @return
     */
    public long getFilesProcCountError();

    /**
     *
     */
    public void incrementFilesProcCountError();

    /**
     *
     * @return
     */
    public long getEventCount();

    /**
     *
     */
    public void incrementEventCount();

    /**
     *
     * @return
     */
    public long getSendThroughput();

    /**
     *
     */
    public void incrementCountModProc();

    /**
     *
     * @return
     */
    public long getCountModProc();

    /**
     *
     * @return
     */
    public long getLastSent();

    /**
     *
     * @param size
     */
    public void incrementCountSizeProc(long size);

    /**
     *
     * @return
     */
    public long getCountSizeProc();

    /**
     *
     * @return
     */
    public long getCountSizeProcKb();

    /**
     *
     * @return
     */
    public long getCountSizeProcMb();
}