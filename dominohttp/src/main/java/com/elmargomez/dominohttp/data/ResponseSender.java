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

import android.os.Handler;
import android.os.Looper;

import com.elmargomez.dominohttp.networking.Callback;
import com.elmargomez.dominohttp.networking.Request;

public class ResponseSender implements Callback {
    private Handler handler = null;

    public ResponseSender(Handler handler) {
        this.handler = handler;
    }

    public ResponseSender() {
        this(new Handler(Looper.getMainLooper()));
    }

    @Override
    public void success(final Request request, final byte[] response) {
        if (request.mSuccess == null) {
            return;
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                request.mSuccess.response(request.mRetrievalID, request.responseConvert(response));
            }
        });
    }

    @Override
    public void failure(final Request request, final String error) {
        if (request.mError == null) {
            return;
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                request.mError.response(request.mRetrievalID, error);
            }
        });
    }
}
