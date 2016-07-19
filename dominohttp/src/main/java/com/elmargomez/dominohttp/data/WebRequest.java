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

import com.elmargomez.dominohttp.RequestQueue;

public class WebRequest {

    private Header mHeader;
    private Body mBody;

    private String mRequestID;
    private RequestQueue mRequestQueue;
    private Object mBind;
    private int mSuccessID = -1;
    private int mErrorID = -1;

    public interface Header {
        void header(NetworkHeader header);
    }

    public interface Body {
        byte[] body();
    }

    protected WebRequest(RequestQueue requestQueue, Object object, Header header, Body body) {
        if (header == null) {
            throw new IllegalArgumentException("Header must not be null!");
        }
        mRequestQueue = requestQueue;
        mBind = object;
        mHeader = header;
        mBody = body;
    }

    public void execute() {

    }

}
