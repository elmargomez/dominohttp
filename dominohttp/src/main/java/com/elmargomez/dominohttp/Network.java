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

    public Response getNetworkResponse(Request request) throws IOException {
        Response response = new Response();

        OutputStream outputStream = null;
        try {
            HttpURLConnection con = openConnection(request.getURL());
            con.setRequestMethod(request.getMethod());

            // All header information combined together.
            HashMap<String, String> allHeaders = new HashMap<>();
            allHeaders.put("Content-Type", request.getContentType());
            allHeaders.putAll(request.getHeaders());
            for (String i : allHeaders.keySet()) {
                con.setRequestProperty(i, allHeaders.get(i));
            }

            // getByte data must be executed in other thread.
            if (request.getMethod() != "GET") {
                byte[] data = request.getByteData();
                outputStream = con.getOutputStream();
                outputStream.write(data, 0, data.length);
                outputStream.flush();
            }

            // We are going to reuse the HashMap to avoid Object creation.
            // The response header will be loaded in this HashMap.
            allHeaders.clear();
            for (int i = 0; ; i++) {
                String headerKey = con.getHeaderFieldKey(i);
                String header = con.getHeaderField(i);

                // We have reached the last header, we need to end this loop.
                if (headerKey == null && header == null)
                    break;

                allHeaders.put(headerKey, header);
            }

            // parse the data
            response.setHeader(allHeaders);
            response.serverData = getBytes(con.getInputStream());
            response.responseCode = con.getResponseCode();

        } catch (IOException e) {
            throw new IOException();
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
        public Map<String, String> header = new HashMap<>();
        public byte[] serverData;
        public int responseCode;
        public long ttl;
        public long softTTL;

        // parse the network response
        public void setHeader(HashMap<String, String> headers) {
            this.header.putAll(headers);
            long now = System.currentTimeMillis();

            long serverDate = 0;
            long serverExpires = 0;
            long softExpire = 0;
            long finalExpire = 0;
            long maxAge = 0;
            long staleWhileRevalidate = 0;
            boolean hasCacheControl = false;
            boolean mustRevalidate = false;

            String headerValue;

            headerValue = headers.get("Date");
            if (headerValue != null) {
                serverDate = DateGenerator.getGenerator().getEpoch(headerValue);
            }

            headerValue = headers.get("Cache-Control");
            if (headerValue != null) {
                hasCacheControl = true;
                String[] tokens = headerValue.split(",");
                for (int i = 0; i < tokens.length; i++) {
                    String token = tokens[i].trim();
                    if (token.equals("no-cache") || token.equals("no-store")) {
                        return;
                    } else if (token.startsWith("max-age=")) {
                        try {
                            maxAge = Long.parseLong(token.substring(8));
                        } catch (Exception e) {
                        }
                    } else if (token.startsWith("stale-while-revalidate=")) {
                        try {
                            staleWhileRevalidate = Long.parseLong(token.substring(23));
                        } catch (Exception e) {
                        }
                    } else if (token.equals("must-revalidate") || token.equals("proxy-revalidate")) {
                        mustRevalidate = true;
                    }

                    // stale-while-revalidate and must-ravl,proxy-reval will not come together.
                }
            }

            headerValue = headers.get("Expires");
            if (headerValue != null) {
                serverExpires = DateGenerator.getGenerator().getEpoch(headerValue);
            }

            // Cache-Control takes precedence over an Expires header, even if both exist and Expires
            // is more restrictive.
            if (hasCacheControl) {
                softExpire = now + maxAge * 1000; // get time that our cache expires in millisecond.
                finalExpire = mustRevalidate
                        ? softExpire : softExpire + staleWhileRevalidate * 1000;
            } else if (serverDate > 0 && serverExpires >= serverDate) {
                // Default semantic for Expire header in HTTP specification is softExpire.
                softExpire = now + (serverExpires - serverDate);
                finalExpire = softExpire;
            }

            softTTL = softExpire;
            ttl = finalExpire;
        }
    }
}
