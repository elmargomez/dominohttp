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

package com.elmargomez.dominohttp.data;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.Map;

public class NetworkHeader {

    public static final String GET = "GET";
    public static final String PUT = "PUT";
    public static final String POST = "POST";

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({GET, PUT, POST})
    public @interface Method {

    }

    public static final String APPLICATION_JSON = "application/json";
    public static final String TEXT_PLAIN = "text/plain";
    public static final String IMAGE_JPEG = "image/jpeg";

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({APPLICATION_JSON, TEXT_PLAIN, IMAGE_JPEG})
    public @interface ContentType {

    }

    public final Map<String, String> header = new HashMap<>();
    public String url;
    public String method;
    public String contentType;

    public void setContentType(@ContentType String contentType) {
        this.contentType = contentType;
    }

    public void setMethod(@Method String method) {
        this.method = method;
    }

}
