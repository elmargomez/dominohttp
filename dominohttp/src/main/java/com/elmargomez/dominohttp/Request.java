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

import android.support.annotation.NonNull;
import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.Map;

public abstract class Request<I> {
    public static final String GET = "GET";
    public static final String PUT = "PUT";
    public static final String POST = "POST";

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({GET, PUT, POST})
    @interface Method {

    }

    public static final String APPLICATION_JSON = "application/json";
    public static final String TEXT_PLAIN = "text/plain";
    public static final String IMAGE_JPEG = "image/jpeg";

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({APPLICATION_JSON, TEXT_PLAIN, IMAGE_JPEG})
    @interface ContentType {

    }

    private Success successListener;
    private Failed failedListener;

    private String requestKey;
    private String url;
    private String method;
    private String contentType;
    private final HashMap<String, String> header = new HashMap<>();
    protected byte[] data;
    private I input;

    private boolean hasCached;

    public Request(Success successListener, Failed failedListener) {
        this.successListener = successListener;
        this.failedListener = failedListener;
    }

    public void setURL(@NonNull String url) {
        this.url = url;
    }

    public String getURL() {
        return url;
    }

    public void setMethod(@NonNull @Method String method) {
        this.method = method;
    }

    public String getMethod() {
        return method;
    }

    public void setContentType(@NonNull @ContentType String ct) {
        this.contentType = ct;
    }

    public String getContentType() {
        return this.contentType;
    }

    public String getRequestKey() {
        if (requestKey == null) {
            StringBuilder builder = new StringBuilder();
            builder.append(url);
            for (String key : header.keySet()) {
                builder.append(key);
                builder.append(header.get(key));
            }
            if (data != null) {
                builder.append(data.length);
            }
            requestKey = builder.toString();
        }
        return requestKey;
    }

    public void addHeaders(Map<String, String> s) {
        header.putAll(s);
    }

    public void addHeader(String key, String val) {
        header.put(key, val);
    }

    public Map<String, String> getHeaders() {
        return header;
    }

    public void isCached(boolean v) {
        hasCached = v;
    }

    public boolean hasCached() {
        return hasCached;
    }

    protected I getBody() {
        return input;
    }

    public void setBody(I i) {
        this.input = i;
    }

    public abstract byte[] getByteData();

    public interface Success<T> {

    }

    public interface Failed {

    }

}
