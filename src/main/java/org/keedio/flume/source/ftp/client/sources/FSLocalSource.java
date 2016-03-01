package org.keedio.flume.source.ftp.client.sources;

import org.keedio.flume.source.ftp.client.KeedioSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by luislazaro on 1/3/16.
 * lalazaro@keedio.com
 * Keedio
 */
public class FSLocalSource  extends KeedioSource<Path> {

   private static final Logger LOGGER = LoggerFactory.getLogger(FSLocalSource.class);

   private Path path;
   public FSLocalSource(String workdir){
      this.path = FileSystems.getDefault().getPath(workdir);
   }

   @Override
   public boolean connect() {
      if (Files.exists(path)){
         return true;
      }
      return false;
   }

   @Override
   public void disconnect() {
     //nothing to do, is local file system.
   }

   @Override
   public List<Path> listElements(String dirToList) throws IOException {
      List<Path> elements = new ArrayList<>();
      File f = new File(dirToList);
      File[] list = f.listFiles();
      for (File file : list ){
         elements.add(file.toPath());
      }
      return elements;
   }

   @Override
   public void changeToDirectory(String directory) throws IOException {
      path = FileSystems.getDefault().getPath(directory);
   }

   @Override
   public InputStream getInputStream(Path file) throws IOException {
      return Files.newInputStream(file);
   }

   @Override
   public String getObjectName(Path file) {
      return file.getFileName().toString();
   }

   @Override
   public boolean isDirectory(Path file) {
      return Files.isDirectory(file);
   }

   @Override
   public boolean isFile(Path file) {
      return Files.isRegularFile(file);
   }

   @Override
   public boolean particularCommand() {
      return true;
   }

   @Override
   public long getObjectSize(Path file) {
      long size = 0L;
       try {
          size = Files.size(file);
       } catch (IOException e){
          LOGGER.info("", e);
       }
      return size;
   }

   @Override
   public boolean isLink(Path file) {
      return Files.isSymbolicLink(file);
   }

   @Override
   public String getLink(Path file) {
      return null;
   }

   @Override
   public String getDirectoryserver() throws IOException {
      return path.toString();
   }

   @Override
   public Object getClientSource() {
      return null;
   }

   @Override
   public void setFileType(int fileType) throws IOException {

   }
}
