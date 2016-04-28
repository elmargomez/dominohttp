/*
 * Copyright 2016 Elmar Rhex Gomez.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.elmargomez.dominohttp;

import com.elmargomez.dominohttp.request.Cache;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class FileCache implements Cache {

    private static final int DEFAULT_DISK_SIZE = 5 * (int) Math.pow(1024.0, 2.0);
    private Map<String, CacheHeader> headers = new HashMap<>();

    private long size;
    private final int limitSize;
    private boolean alreadyInitialize;
    private File generalFile;

    public FileCache(File file, int maxCacheSizeInBytes) {
        generalFile = file;
        limitSize = maxCacheSizeInBytes;
    }

    public FileCache(File file) {
        this(file, DEFAULT_DISK_SIZE);
    }

    @Override
    public synchronized void initialize() {
        if (alreadyInitialize) {
            DominoLog.debug("File Cached already Initialized!");
            return;
        } else {
            alreadyInitialize = true;
        }

        if (generalFile == null)
            throw new NullPointerException();

        if (!generalFile.exists()) {
            generalFile.mkdir();
            return;
        }

        File[] files = generalFile.listFiles();
        if (files == null)
            return;

        for (File file : files) {

            BufferedInputStream stream = null;
            try {
                stream = new BufferedInputStream(new FileInputStream(file));
                CacheHeader header = CacheHeader.readHeader(stream);
                header.size = file.length();
                putData(header.cacheKey, header);
            } catch (IOException e) {
                if (file != null) {
                    file.delete();
                }
            } finally {
                if (stream == null) {
                    try {
                        stream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        DominoLog.debug("File cached initialized!");
    }

    @Override
    public synchronized void put(String key, Data data) {
        hammerFile(data.data.length);
        File file = getFileForKey(key);
        try {
            BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(file));
            CacheHeader e = new CacheHeader(key, data);
            boolean success = e.writeHeader(fos);
            if (!success) {
                fos.close();
                throw new IOException();
            }
            fos.write(data.data);
            fos.close();
            putData(key, e);
            return;
        } catch (IOException e) {
        }

        boolean deleted = file.delete();
    }

    @Override
    public synchronized Data get(String cacheKey) {
        CacheHeader entry = headers.get(cacheKey);
        // if the entry does not exist, return.
        if (entry == null) {
            return null;
        }

        File file = getFileForKey(cacheKey);
        SizeInputStream cis = null;
        try {
            cis = new SizeInputStream(new BufferedInputStream(new FileInputStream(file)));
            CacheHeader.readHeader(cis); // eat header
            byte[] data = writeBytesToStream(cis, (int) (file.length() - cis.bytesRead));
            return entry.toCacheEntry(data);
        } catch (IOException e) {
            remove(cacheKey);
            return null;
        } finally {
            if (cis != null) {
                try {
                    cis.close();
                } catch (IOException ioe) {
                    return null;
                }
            }
        }
    }

    @Override
    public synchronized void remove(String key) {
        boolean deleted = getFileForKey(key).delete();
        removeEntry(key);
    }

    private void removeEntry(String key) {
        CacheHeader entry = headers.get(key);
        if (entry != null) {
            size -= entry.size;
            headers.remove(key);
        }
    }

    private static class SizeInputStream extends FilterInputStream {
        private int bytesRead = 0;

        private SizeInputStream(InputStream in) {
            super(in);
        }

        @Override
        public int read() throws IOException {
            int result = super.read();
            if (result != -1) {
                bytesRead++;
            }
            return result;
        }

        @Override
        public int read(byte[] buffer, int offset, int count) throws IOException {
            int result = super.read(buffer, offset, count);
            if (result != -1) {
                bytesRead += result;
            }
            return result;
        }
    }

    private void hammerFile(int neededSpace) {
        if ((size + neededSpace) < limitSize) {
            return;
        }

        Iterator<Map.Entry<String, CacheHeader>> iterator = headers.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, CacheHeader> entry = iterator.next();
            CacheHeader e = entry.getValue();
            boolean deleted = getFileForKey(e.cacheKey).delete();
            if (deleted) {
                size -= e.size;
            }

            iterator.remove();

            if ((size + neededSpace) < limitSize * 0.9f) {
                break;
            }
        }

    }

    public File getFileForKey(String key) {
        return new File(generalFile, getFilenameForKey(key));
    }

    private String getFilenameForKey(String key) {
        int firstHalfLength = key.length() / 2;
        String localFilename = String.valueOf(key.substring(0, firstHalfLength).hashCode());
        localFilename += String.valueOf(key.substring(firstHalfLength).hashCode());
        return localFilename;
    }

    private void putData(String key, CacheHeader entry) {
        if (!headers.containsKey(key)) {
            size += entry.size;
        } else {
            CacheHeader oldEntry = headers.get(key);
            size += (entry.size - oldEntry.size);
        }
        headers.put(key, entry);
    }

    static class CacheHeader {
        public long size;
        public String cacheKey;
        public Map<String, String> header;
        public long ttl;
        public long softTTL;

        public CacheHeader() {
        }

        public CacheHeader(String cacheKey, Data data) {
            size = data.data.length;
            this.cacheKey = cacheKey;
            header = data.header;
            ttl = data.ttl;
            softTTL = data.softTTL;
        }

        public static CacheHeader readHeader(InputStream is) throws IOException {
            CacheHeader entry = new CacheHeader();
            entry.cacheKey = readString(is);
            entry.header = readStringStringMap(is);
            entry.ttl = readLong(is);
            entry.softTTL = readLong(is);
            return entry;
        }

        public boolean writeHeader(OutputStream os) {
            try {
                writeString(os, cacheKey);
                writeStringStringMap(header, os);
                writeLong(os, ttl);
                writeLong(os, softTTL);
                os.flush();
                return true;
            } catch (IOException e) {
                return false;
            }
        }

        public Data toCacheEntry(byte[] data) {
            Data e = new Data();
            e.data = data;
            e.ttl = ttl;
            e.softTTL = softTTL;
            e.header = header;
            return e;
        }

    }

    private static byte[] writeBytesToStream(InputStream in, int length) throws IOException {
        byte[] bytes = new byte[length];
        int count;
        int pos = 0;
        while (pos < length && ((count = in.read(bytes, pos, length - pos)) != -1)) {
            pos += count;
        }

        if (pos != length) {
            throw new IOException("Expected " + length + " bytes, read " + pos + " bytes");
        }
        return bytes;
    }

    private static int read(InputStream is) throws IOException {
        int b = is.read();
        if (b == -1) {
            throw new EOFException();
        }
        return b;
    }

    static void writeInt(OutputStream os, int n) throws IOException {
        os.write((n >> 0) & 0xff);
        os.write((n >> 8) & 0xff);
        os.write((n >> 16) & 0xff);
        os.write((n >> 24) & 0xff);
    }

    static int readInt(InputStream is) throws IOException {
        int n = (read(is) << 0)
                | (read(is) << 8)
                | (read(is) << 16)
                | (read(is) << 24);
        return n;
    }

    static void writeLong(OutputStream os, long n) throws IOException {
        os.write((byte) (n >>> 0));
        os.write((byte) (n >>> 8));
        os.write((byte) (n >>> 16));
        os.write((byte) (n >>> 24));
        os.write((byte) (n >>> 32));
        os.write((byte) (n >>> 40));
        os.write((byte) (n >>> 48));
        os.write((byte) (n >>> 56));
    }

    static long readLong(InputStream is) throws IOException {
        long n = ((read(is) & 0xFFL) << 0)
                | ((read(is) & 0xFFL) << 8)
                | ((read(is) & 0xFFL) << 16)
                | ((read(is) & 0xFFL) << 24)
                | ((read(is) & 0xFFL) << 32)
                | ((read(is) & 0xFFL) << 40)
                | ((read(is) & 0xFFL) << 48)
                | ((read(is) & 0xFFL) << 56);
        return n;
    }

    static void writeString(OutputStream os, String s) throws IOException {
        byte[] b = s.getBytes("UTF-8");
        writeLong(os, b.length); // store the text size!
        os.write(b, 0, b.length);
    }

    static String readString(InputStream is) throws IOException {
        int n = (int) readLong(is);
        byte[] b = writeBytesToStream(is, n);
        return new String(b, "UTF-8");
    }

    static void writeStringStringMap(Map<String, String> map, OutputStream os) throws IOException {
        if (map != null) {
            writeInt(os, map.size());
            for (Map.Entry<String, String> entry : map.entrySet()) {
                writeString(os, entry.getKey());
                writeString(os, entry.getValue());
            }
        } else {
            writeInt(os, 0);
        }
    }

    static Map<String, String> readStringStringMap(InputStream is) throws IOException {
        int size = readInt(is);
        Map<String, String> result = (size == 0)
                ? Collections.<String, String>emptyMap()
                : new HashMap<String, String>(size);
        for (int i = 0; i < size; i++) {
            String key = readString(is).intern();
            String value = readString(is).intern();
            result.put(key, value);
        }
        return result;
    }

}
