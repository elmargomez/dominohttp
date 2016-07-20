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

import com.elmargomez.dominohttp.data.NetworkHeader;
import com.elmargomez.dominohttp.data.WebRequest;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class Network {

    public Response getNetworkResponse(WebRequest request) throws IOException {
        NetworkHeader webHeader = request.getHeader();

        InputStream stream = null;
        OutputStream outputStream = null;
        BufferedReader errorStreamWriter = null;
        try {

            HttpURLConnection con = openConnection(webHeader.url);
            con.setRequestMethod(webHeader.method);
            // All header information combined together.
            Map<String, String> allHeaders = new HashMap<>();
            allHeaders.put("Content-Type", webHeader.contentType);
            allHeaders.putAll(webHeader.header);

            for (String i : allHeaders.keySet()) {
                con.setRequestProperty(i, allHeaders.get(i));
            }

            if (webHeader.method == NetworkHeader.PUT || webHeader.method == NetworkHeader.POST) {
                con.setDoInput(true);
            }

            // getByte data must be executed in other thread.
            if (webHeader.method != NetworkHeader.GET) {
                byte[] data = request.getBody();
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
                if (headerKey == null && header == null) {
                    break;
                }
                allHeaders.put(headerKey, header);
            }

            if (con.getResponseCode() != 200) {
                errorStreamWriter = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                StringBuilder builder = new StringBuilder();
                String dummy;
                while ((dummy = errorStreamWriter.readLine()) != null) {
                    builder.append(dummy);
                }

                throw new IOException(builder.toString());
            }

            Response response = new Response();
            response.setHeader(allHeaders);
            stream = con.getInputStream();
            response.serverData = getBytes(stream);
            response.responseCode = con.getResponseCode();
            return response;

        } catch (IOException e) {
            throw e;
        } finally {
            if (stream != null) {
                stream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
            if (errorStreamWriter != null) {
                errorStreamWriter.close();
            }
        }
    }

    public HttpURLConnection openConnection(String url) throws IOException {
        return (HttpURLConnection) new URL(url).openConnection();
    }

    public static byte[] getBytes(InputStream stream) throws IOException {
        ByteArrayOutputStream byteArray = null;
        byte[] buffer = new byte[1024];
        int read;
        try {
            byteArray = new ByteArrayOutputStream();
            while (-1 != (read = stream.read(buffer))) {
                byteArray.write(buffer, 0, read);
            }
            byteArray.flush();
            buffer = byteArray.toByteArray();
        } catch (IOException e) {
            throw e;
        } finally {
            if (byteArray != null) {
                byteArray.close();
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

        // parse the network responseType
        public void setHeader(Map<String, String> headers) {
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
