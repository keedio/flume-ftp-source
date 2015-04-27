/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.flume.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author luislazaro
 */
public class KeedioSourceTest extends TestCase {

    private static final Logger log = LoggerFactory.getLogger(KeedioSourceTest.class);

    public KeedioSourceTest(String testName) {
        super(testName);
    }

    /**
     * Test of saveMap method, of class KeedioSource. Check a file is created
     * int the specified path. Populate a map and use it as sizeFileList where
     * to keep files names and their size. Retrieve from the file in the file
     * system, and object that will be cast to map. Iterate over the map and
     * check that contains what we expect.
     */
    public void testSaveMap() {
        System.out.println("saveMap");
        KeedioSource instance = new KeedioSourceImpl();
        //check for the file is created 
        instance.setAbsolutePath(Paths.get(System.getProperty("java.io.tmpdir")+"file.ser"));
        Map<String, Long> map = new HashMap<>();
        map.put("file1.log", 156L);
        instance.setFileList(map);
        instance.saveMap();
        assertTrue("no existe", Files.exists(Paths.get(System.getProperty("java.io.tmpdir")+"file.ser")));

        //now check that the map saved into file contains what we expect
        ObjectInputStream ois = null;
        Map<String, Long> mapExp = new HashMap<>();
        String result1 = "";
        String result2 = "";
        try {
            ois = new ObjectInputStream(new FileInputStream(System.getProperty("java.io.tmpdir")+"file.ser"));
        } catch (IOException e) {
            log.error("", e);
        }
        try {
            mapExp = (HashMap) ois.readObject();
        } catch (ClassNotFoundException | IOException e) {
            log.error("", e);
        }

        Iterator it = mapExp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry e = (Map.Entry) it.next();
            result1 = e.getKey().toString();
            result2 = e.getValue().toString();
            // System.out.println(e.getKey() + " " + e.getValue());
        }

        String expResult1 = "file1.log";
        assertEquals(expResult1, result1);
        String expResult2 = "156";
        assertEquals(expResult2, result2);

    }

    /**
     * Test of loadMap method, of class KeedioSource
     *
     */
    public void testLoadMap() {
        System.out.println("loadMap");
        KeedioSource instance = new KeedioSourceImpl();
        Map<String, Long> mapExp = new HashMap<>();

        try {
            mapExp = instance.loadMap(System.getProperty("java.io.tmpdir")+"file.ser");
        } catch (ClassNotFoundException | IOException e) {
            log.error("", e);
        }

        String result1 = "";
        String result2 = "";
        Iterator it = mapExp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry e = (Map.Entry) it.next();
            result1 = e.getKey().toString();
            result2 = e.getValue().toString();
            //System.out.println(e.getKey() + " " + e.getValue());
        }

        String expResult1 = "file1.log";
        assertEquals(expResult1, result1);
        String expResult2 = "156";
        assertEquals(expResult2, result2);
    }

    /**
     * Test of cleanList method. File of map no included in set, will be deleted
     * from the map.
     */
    public void testCleanList() {
        System.out.println("cleanList");
        KeedioSource instance = new KeedioSourceImpl();

        Map<String, Long> mapExp = new HashMap<>();
        mapExp.put("file1.log", 2282L);
        mapExp.put("file234.log", 132L);
        mapExp.put("file5.log", 133283474L);
        instance.setFileList(mapExp);

        Set<String> setExp = new HashSet<>();
        setExp.add("file5.log");
        setExp.add("file1.log");
        instance.setExistFileList(setExp);

        instance.cleanList();

        assertEquals(2, mapExp.size());

        Iterator it = mapExp.entrySet().iterator();
        Map.Entry e = (Map.Entry) it.next();
        assertEquals(e.getKey() + "," + e.getValue(), "file5.log,133283474");
        Map.Entry f = (Map.Entry) it.next();
        assertEquals(f.getKey() + "," + f.getValue(), "file1.log,2282");

    }

    /**
     * Test of makeLocationFile method.
     */
    public void testMakeLocationFile() {
        System.out.println("makeLocationFile");
        KeedioSource instance = new KeedioSourceImpl();
        instance.setFileName("file.ser");
        instance.setFolder(System.getProperty("java.io.tmpdir"));
        assertEquals(instance.makeLocationFile().toString(), System.getProperty("java.io.tmpdir")+"file.ser");
    }

    /**
     * Test of existFolder method.
     */
    public void testExistFolder() {
        System.out.println("existFolder");
        KeedioSource instance = new KeedioSourceImpl();
        instance.setFolder(System.getProperty("java.io.tmpdir"));
        assertTrue(instance.existFolder());
    }

    /**
     * Test of checkPreviousMap method.
     */
    public void testCheckPreviousMap() {
        System.out.println("checkPreviousMap");
        KeedioSource instance = new KeedioSourceImpl();
        instance.setFileList(null);
        instance.setFileName("file.ser");
        instance.setFolder(System.getProperty("java.io.tmpdir"));
        instance.makeLocationFile();
        instance.checkPreviousMap();
        assertNotNull(instance.getFileList());

        String result1 = "";
        String result2 = "";
        Iterator it = instance.getFileList().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry e = (Map.Entry) it.next();
            result1 = e.getKey().toString();
            result2 = e.getValue().toString();
        }

        String expResult1 = "file1.log";
        assertEquals(expResult1, result1);
        String expResult2 = "156";
        assertEquals(expResult2, result2);

    }

    public class KeedioSourceImpl extends KeedioSource {

        public boolean connect() {
            return false;
        }

        public void disconnect() {
        }

        public List<Object> listFiles(String dirToList) {
            return null;
        }

        public void changeToDirectory(String directory) {
        }

        public InputStream getInputStream(Object file) throws IOException {
            return null;
        }

        public String getObjectName(Object file) {
            return "";
        }

        public boolean isDirectory(Object file) {
            return false;
        }

        public boolean isFile(Object file) {
            return false;
        }

        public boolean particularCommand() {
            return false;
        }

        public long getObjectSize(Object file) {
            return 0L;
        }

        public boolean isLink(Object file) {
            return false;
        }

        public String getLink(Object file) {
            return "";
        }

        public String getDirectoryserver() {
            return "";
        }

        public Object getClientSource() {
            return null;
        }

        public void setFileType(int fileType) throws IOException {
        }
    }

}
