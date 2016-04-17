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

package testonly;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class DiskCache implements CacheBank {

    private final Map<String, CacheHeader> cachedHeaders = new HashMap<>();
    private File cacheFile;
    private int size;

    public DiskCache(File cacheFile) {
        this.cacheFile = cacheFile;
    }


    @Override
    public void init() {
        File[] files = cacheFile.listFiles();
        if (files == null)
            return;

        for(File f : files){
            try {
                InputStream stream = new FileInputStream(f);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public synchronized void put(String key, CacheContent cacheContent) {

    }

    @Override
    public synchronized CacheContent get(String key) {
        return null;
    }

    public static String makeFolderName(String name) {
        return name;
    }

    private class CacheHeader {
        public Map<String, String> header;
        public int ttl;
        public int extendedTTL;

    }
}
