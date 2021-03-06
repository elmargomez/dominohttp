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

import android.os.Handler;
import android.os.Looper;

import com.elmargomez.dominohttp.request.Request;

public class ResponseSender {
    private Handler handler = null;

    public ResponseSender(Handler handler) {
        this.handler = handler;
    }

    public ResponseSender() {
        this(new Handler(Looper.getMainLooper()));
    }

    public void success(final Request request, byte[] response) {
        final Object p = request.generateResponse(response);
        handler.post(new Runnable() {
            @Override
            public void run() {
                Request.SuccessListener successListener = request.getSuccessListener();
                if (successListener != null) {
                    successListener.response(request, p);
                }
            }
        });
    }

    public void failure(final Request request, final String error) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Request.FailedListeners errorListener = request.getErrorListener();
                if (errorListener != null) {
                    errorListener.error(request, error);
                }
            }
        });
    }
}
