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

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

public abstract class Request<R> implements Comparator<Request> {
    private String responseID = null;
    private String url = null;
    private String method = null;
    private String contentType = null;
    private Map<String, String> header = Collections.emptyMap();

    @Override
    public int compare(Request lhs, Request rhs) {
        return 0;
    }

    /**
     * The callback for receiving the data
     *
     * @param <R>
     */
    public interface SuccessListener<R> {
        void response(R r);
    }

    public interface ErrorListener {
        void response(String r);
    }

    public Request setID(String id) {
        this.responseID = id;
        return this;
    }

    public Request setURL(String url) {
        this.url = url;
        return this;
    }

    public Request setMethod(@Method String method) {
        this.method = method;
        return this;
    }

    public Request setContentType(@ContentType String contentType) {
        this.contentType = contentType;
        return this;
    }

    public Request addHeader(String key, String val) {
        header.put(key, val);
        return this;
    }

    public abstract Request addContent(R data);

    public abstract R getContent();

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({GET, PUT, POST})
    @interface Method {
    }

    public static final String GET = "GET";
    public static final String PUT = "PUT";
    public static final String POST = "POST";

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({APPLICATION_JSON, TEXT_PLAIN, IMAGE_JPEG})
    @interface ContentType {
    }

    public static final String APPLICATION_JSON = "application/json";
    public static final String TEXT_PLAIN = "text/plain";
    public static final String IMAGE_JPEG = "image/jpeg";
}
