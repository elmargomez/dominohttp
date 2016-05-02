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

package com.elmargomez.dominohttp.request;

import com.elmargomez.dominohttp.Request;

public class JSONRequest extends Request<String, String> {

    public JSONRequest(SuccessListener<String> successListener, FailedListeners failedListenersListener) {
        super(successListener, failedListenersListener);
        setContentType(APPLICATION_JSON);
    }

    @Override
    public byte[] getByteData() {
        if (data == null) {
            data = getBody().getBytes();
        }
        return data;
    }

    @Override
    public String generateResponse(byte[] b) {
        return new String(b);
    }

    @Override
    public int compareTo(Object another) {
        return 0;
    }
}
