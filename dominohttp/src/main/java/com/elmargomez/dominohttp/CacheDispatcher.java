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

import com.elmargomez.dominohttp.data.WebRequest;

import java.util.concurrent.BlockingQueue;

public class CacheDispatcher extends Thread {

    private BlockingQueue<WebRequest> networkRequest;
    private BlockingQueue<WebRequest> cachedRequest;
    private Cache cache;
    private WebRequest.ResponseSender responseSender;

    private boolean isInterrupted;

    public CacheDispatcher(BlockingQueue<WebRequest> networkRequest,
                           BlockingQueue<WebRequest> cachedRequest, Cache cache,
                           WebRequest.ResponseSender responseSender) {
        this.networkRequest = networkRequest;
        this.cachedRequest = cachedRequest;
        this.cache = cache;
        this.responseSender = responseSender;
    }

    @Override
    public void run() {
        cache.initialize();
        while (true) {
            WebRequest request = null;
            try {
                request = cachedRequest.take();
                DominoLog.debug("New Cache Request [id: " + request.getRequestKey() + "]");
            } catch (InterruptedException e) {
                if (isInterrupted) {
                    break;
                }
            }

            if (request.isCanceled()) {
                // We need to skip this request.
                continue;
            }

            Cache.Data data = cache.get(request.getRequestKey());
            if (data == null || data.isExpired()) {
                networkRequest.add(request);
                continue;
            }

            responseSender.success(request, data.data);
        }
    }

    public void cancel() {
        isInterrupted = true;
        interrupt();
    }
}
