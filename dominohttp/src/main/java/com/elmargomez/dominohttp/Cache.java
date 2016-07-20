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

import com.elmargomez.dominohttp.data.Network;

import java.util.Map;

public interface Cache {

    void put(String key, Data data);

    Data get(String cacheKey);

    void remove(String k);

    void initialize();

    class Data {
        public Map<String, String> header;
        public long ttl;
        public long softTTL;
        public byte[] data;

        public Data(){

        }

        public Data(Network.Response networkResponse) {
            header = networkResponse.header;
            data = networkResponse.serverData;
            ttl = networkResponse.ttl;
            softTTL = networkResponse.softTTL;
        }

        public boolean isExpired() {
            return ttl < System.currentTimeMillis();
        }

        public boolean needsRefresh() {
            return softTTL < System.currentTimeMillis();
        }
    }

}
