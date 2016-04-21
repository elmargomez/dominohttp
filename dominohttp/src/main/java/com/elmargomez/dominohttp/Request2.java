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

public abstract class Request2<I> {
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

    public String url;
    public String method;
    public Map<String, String> header = new HashMap<>();
    String requestKey;
    protected byte[] data;
    private I input;

    public Request2(Success successListener, Failed failedListener) {
        this.successListener = successListener;
        this.failedListener = failedListener;
        this.requestKey = KeyGenerator.getGenerator().getKey();
    }

    public void setURL(@NonNull String url) {
        this.url = url;
    }

    public void setMethod(@NonNull @Method String method) {
        this.method = method;
    }

    public void addContentType(@NonNull @ContentType String ct) {
        header.put("Content-Type", ct);
    }

    public void setHeader(Map<String, String> s) {
        header.putAll(s);
    }

    public abstract byte[] getByteData();

    public I getBody() {
        return input;
    }

    public void setBody(I i) {
        this.input = i;
    }

    public interface Success<T> {

    }

    public interface Failed {

    }

}
