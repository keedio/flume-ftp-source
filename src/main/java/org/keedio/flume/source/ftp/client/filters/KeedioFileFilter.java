package org.keedio.flume.source.ftp.client.filters;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;

/**
 * Created by luislazaro on 21/8/17.
 * lalazaro@keedio.com
 * Keedio
 */
public class KeedioFileFilter implements FTPFileFilter {
  private String strMatch = "";
  public KeedioFileFilter(String strMatch){
    this.strMatch = strMatch;
  }

  @Override
  public boolean accept(FTPFile ftpFile) {
    if (("").equals(strMatch)) return true;
    if (strMatch == null) return true;
    return (
      ftpFile.isDirectory() ||
        (ftpFile.isFile() && ftpFile.getName().matches(strMatch)));
  }
}
