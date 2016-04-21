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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Network {

    public Response getNetworkResponse(Request2 request) {
        Response response = new Response();

        OutputStream outputStream = null;
        try {
            HttpURLConnection con = openConnection(request.url);
            con.setRequestMethod(request.method);
            HashMap<String, String> allHeaders = new HashMap<>();
            allHeaders.putAll(request.header);
            for (String i : allHeaders.keySet()) {
                con.setRequestProperty(i, allHeaders.get(i));
            }
            byte[] data = request.getByteData();
            outputStream = con.getOutputStream();
            outputStream.write(data, 0, data.length);
            outputStream.flush();

            response.serverData = getBytes(con.getInputStream());
            response.responseCode = con.getResponseCode();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return response;
    }

    public HttpURLConnection openConnection(String url) throws IOException {
        return (HttpURLConnection) new URL(url).openConnection();
    }

    public static byte[] getBytes(InputStream stream) {
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int read;
        try {
            while (-1 != (read = stream.read(buffer))) {
                byteArray.write(buffer, 0, read);
            }
            byteArray.flush();
            buffer = byteArray.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                byteArray.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return buffer;
    }

    public class Response {
        public Map<String, String> header = Collections.emptyMap();
        public byte[] serverData;
        public int responseCode;
        public long ttl;
        public long softTTL;

        public boolean isExpired() {
            return ttl < System.currentTimeMillis();
        }

        public boolean mustRefresh() {
            return softTTL < System.currentTimeMillis();
        }

    }

}
