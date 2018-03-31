/*
 * KEEDIO
 */

package org.keedio.flume.source.ftp.client;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.keedio.flume.source.ftp.client.filters.KeedioFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.*;


/**
 *
 * @author Luis Lázaro lalazaro@keedio.com
 * Keedio
 
 /**
 * Abstract class for sources that need a network connection before process.
 * @param <T>
 */
public abstract class KeedioSource<T> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(KeedioSource.class);
      
    private Map<String, Long> fileList = new HashMap<>();
    private Set<String> existFileList = new HashSet<>();
    private Path pathTohasmap = Paths.get("");
    private Path hasmap = Paths.get("");
    private Path absolutePath = Paths.get("");   
    
    /**
     *
     */
    protected String server;

    /**
     *
     */
    protected String user;

    /**
     *
     */
    protected String password;

    /**
     *
     */
    protected String folder;

    /**
     *
     */
    protected boolean recursive;

    /**
     *
     */
    protected String fileName;

    /**
     *
     */
    protected Integer port;

    /**
     *
     */
    protected Integer bufferSize;

    /**
     *
     */
    protected Integer runDiscoverDelay;

    /**
     *
     */
    protected String workingDirectory; //working directory specified in config.

    /**
     *
     */
    protected boolean flushLines;

    /**
     *
     */
    protected boolean connected;

    /**
     *
     */
    protected boolean processInUse;

    /**
     *
     */
    protected Integer processInUseTimeout;

    /**
     *
     */
    protected String dirToList;

    /**
     *
     */
    protected String compressionFormat;

  /**
     *
     */
    protected Integer chunkSize;

  /**
   *
   */
  private String keedioFilterRegex;
   
    /**
     *
     * @return
     */
    public abstract boolean connect();

    /**
     *
     */
    public abstract void disconnect();

    /**
     *
     * @param dirToList
     * @return
     * @throws IOException
     */
    public abstract List<T> listElements(String dirToList) throws IOException;

    /**
     *
     * @param directory
     * @throws IOException
     */
    public abstract void changeToDirectory(String directory)throws IOException;

    /**
     *
     * @param file
     * @return
     * @throws IOException
     */
    public abstract InputStream getInputStream(T file ) throws IOException;

    /**
     *
     * @param file
     * @return
     */
    public abstract String getObjectName(T file);

    /**
     *
     * @param file
     * @return
     */
    public abstract boolean isDirectory(T file);

    /**
     *
     * @param file
     * @return
     */
    public abstract boolean isFile(T file);

    /**
     *
     * @param file
     * @return
     */
    public abstract long getModifiedTime(T file);

    /**
     *
     * @return
     */
    public abstract boolean particularCommand();

    /**
     *
     * @param file
     * @return
     */
    public abstract long getObjectSize(T file);

    /**
     *
     * @param file
     * @return
     */
    public abstract boolean isLink(T file);

    /**
     *
     * @param file
     * @return
     */
    public abstract String getLink(T file);

    /**
     *
     * @return
     * @throws IOException
     */
    public abstract String getDirectoryserver() throws IOException; //the working directory retrieved by server

    /**
     *
     * @return
     */
    public abstract Object getClientSource();

    /**
     *
     * @param fileType
     * @throws IOException
     */
    public abstract void setFileType(int fileType) throws IOException;
   
    /**
     * @void save map of file's names proccesed
     */
    public void saveMap() {
        try {
            FileOutputStream fileOut = new FileOutputStream(getAbsolutePath().toString());
            try (ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
                out.writeObject((HashMap) getFileList());
            }
        } catch (FileNotFoundException e) {
            LOGGER.error("Error saving map File", e);
        } catch (IOException e) {
            LOGGER.error("Error saving map IO:", e);
        }
    }

    /**
     * @return HashMap<String,Long> 
     * @param name
     * @throws java.lang.ClassNotFoundException
     * @throws java.io.IOException
     */
    public Map<String, Long> loadMap(String name) throws ClassNotFoundException, IOException {
        FileInputStream fileIn = new FileInputStream(name);
        Map map = new HashMap<>();
        try (ObjectInputStream in = new ObjectInputStream(fileIn)) {
            map = (HashMap) in.readObject();
        }
        return map;
    }

    /**
     * @void, delete file from hashmaps if deleted from server
     */
    public void cleanList() {
        for (Iterator<String> iter = this.getFileList().keySet().iterator(); iter.hasNext();) {
            final String filename = iter.next();
            if (!(existFileList.contains(filename))) {
                iter.remove();
            }
        }
    }

    /**
     * @void, check if there are previous files to load of an old session
     */
    public void checkPreviousMap() {
        Path file1 = makeLocationFile();
        try {
            if (Files.exists(file1)) {
                setFileList(loadMap(file1.toString()));
                LOGGER.info("Found previous map of files flumed: " + file1.toString());
            } else {
                LOGGER.info("Not found preivous map of files flumed");

            }

        } catch (IOException | ClassNotFoundException e) {
            LOGGER.info("Exception thrown checking previous map ", e);
        }
    }

    /**
     * @return boolean, folder where to save data exists
     */
    public boolean existFolder() {
        pathTohasmap = Paths.get(getFolder()); //ruta a la carpeta especificada en fichero conf
        boolean folderExits = false;
        if (Files.exists(getPathTohasmap())) { //si realmente existe la carpeta especificada
            folderExits = true;
        }
        return folderExits;
    }

    /**
     * @return server
     */
    public String getServer() {
        return server;
    }

    /**
     * @void 
     * @param server the server to set
     */
    public void setServer(String server) {
        this.server = server;
    }

    /**
     * @return the user
     */
    public String getUser() {
        return user;
    }

    /**
     * @void 
     * @param user the user to set
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return the folder
     */
    public String getFolder() {
        return folder;
    }

    /**
     * @param folder the folder to set
     */
    public void setFolder(String folder) {
        this.folder = folder;
    }

    /**
     * @return the fileName
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @param fileName the fileName to set
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * @return the port
     */
    public Integer getPort() {
        return port;
    }

    /**
     * @param port the port to set
     */
    public void setPort(Integer port) {
        this.port = port;
    }

    /**
     * @return the bufferSize
     */
    public Integer getBufferSize() {
        return bufferSize;
    }

    /**
     * @param bufferSize the bufferSize to set
     */
    public void setBufferSize(Integer bufferSize) {
        this.bufferSize = bufferSize;
    }

    /**
     * @return the runDiscoverDelay
     */
    public int getRunDiscoverDelay() {
        return runDiscoverDelay;
    }

    /**
     * @param runDiscoverDelay the runDiscoverDelay to set
     */
    public void setRunDiscoverDelay(int runDiscoverDelay) {
        this.runDiscoverDelay = runDiscoverDelay;
    }

    /**
     * @return the workingDirectory
     */
    public String getWorkingDirectory() {
        return workingDirectory;
    }

    /**
     * @param workingDirectory the workingDirectory to set
     */
    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    /**
     * @return the fileList
     */
    public Map<String, Long> getFileList() {
        return fileList;
    }

    /**
     * @param fileList the fileList to set
     */
    public void setFileList(Map<String, Long> fileList) {
        this.fileList = fileList;
    }

    /**
     * @return the existFileList
     */
    public Set<String> getExistFileList() {
        return existFileList;
    }

    /**
     * @param existFileList the existFileList to set
     */
    public void setExistFileList(Set<String> existFileList) {
        this.existFileList = existFileList;
    }

    /**
     * @return the pathTohasmap
     */
    public Path getPathTohasmap() {
        return pathTohasmap;
    }

    /**
     * @param pathTohasmap the pathTohasmap to set
     */
    public void setPathTohasmap(Path pathTohasmap) {
        this.pathTohasmap = pathTohasmap;
    }

    /**
     * 
     * @return  recursive
     */
    public boolean isRecursive() {
        return recursive;
    }

    /**
     *
     * @return  processInUse
     */
    public boolean isProcessInUse() { return processInUse; }

    /**
     *
     * @return  processInUseTimeout
     */
    public Integer getProcessInUseTimeout() { return processInUseTimeout; }

    /**
     *
     * @return
     */
    public Path getHasmap() {
      return hasmap;
    }

    /**
     * @param hasmap the hasmap to set
     */
    public void setHasmap(Path hasmap) {
        this.hasmap = hasmap;
    }

    /**
     * @return the absolutePath
     */
    public Path getAbsolutePath() {
        return absolutePath;
    }

    /**
     * @param absolutePath the absolutePath to set
     */
    public void setAbsolutePath(Path absolutePath) {
        this.absolutePath = absolutePath;
    }

    /**
     * @return the connected
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * @param connected the connected to set
     */
    public void setConnected(boolean connected) {
        this.connected = connected;
    }
    
    /**
     * 
     * @return Path of file and folder  
     */
    public Path makeLocationFile(){
      hasmap = Paths.get(getFileName());
      pathTohasmap = Paths.get(getFolder());
      absolutePath = Paths.get(pathTohasmap.toString(), hasmap.toString());
      return absolutePath;
    }

    /**
     * @return if flush lines instead chunk of bytes
     */
    public boolean isFlushLines() {
        return flushLines;
    }

    /**
     * @param flushLines the flushLines to set
     */
    public void setFlushLines(boolean flushLines) {
        this.flushLines = flushLines;
    }

    /**
     * @return the chunkSize
     */
    public Integer getChunkSize() {
        return chunkSize;
    }

    /**
     * @param chunkSize the chunkSize to set
     */
    public void setChunkSize(Integer chunkSize) {
        this.chunkSize = chunkSize;
    }

    /**
     *
     * @param dirToList
     * @param filter
     * @return
     *
     * @throws IOException
     */
    public abstract List<T> listElements(String dirToList, KeedioFileFilter filter) throws IOException;

    public String getKeedioFilterRegex() {
        return keedioFilterRegex;
    }

    public void setKeedioFilterRegex(String keedioFilterRegex) {
        this.keedioFilterRegex = keedioFilterRegex;
    }

    public void setRecursive(boolean recursive) {
      this.recursive = recursive;
    }

    public void setProcessInUse(boolean processInUse) {
      this.processInUse = processInUse;
    }

    public void setProcessInUseTimeout(Integer processInUseTimeout) {
      this.processInUseTimeout = processInUseTimeout;
    }

    public void setFileCompressed(String compressionFormat) {
      this.compressionFormat = compressionFormat.toLowerCase().trim();
    }

    public String getCompressionFormat() { return compressionFormat; }
} //endclass
