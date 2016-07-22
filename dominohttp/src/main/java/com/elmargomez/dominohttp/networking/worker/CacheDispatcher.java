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

package com.elmargomez.dominohttp.networking.worker;

import com.elmargomez.dominohttp.data.FileCache;
import com.elmargomez.dominohttp.networking.Cache;
import com.elmargomez.dominohttp.networking.Callback;
import com.elmargomez.dominohttp.networking.Request;

import java.util.concurrent.BlockingQueue;

public class CacheDispatcher extends Thread {

    private BlockingQueue<Request> mNetworkRequest;
    private BlockingQueue<Request> mCachedRequest;
    private Cache mCache;
    private Callback mCallback;

    private boolean isInterrupted;

    public CacheDispatcher(BlockingQueue<Request> mNetworkRequest,
                           BlockingQueue<Request> mCachedRequest, Cache cache,
                           Callback callback) {
        this.mNetworkRequest = mNetworkRequest;
        this.mCachedRequest = mCachedRequest;
        this.mCache = cache;
        this.mCallback = callback;
    }

    @Override
    public void run() {
        mCache.initialize();
        while (true) {
            Request request = null;
            try {
                request = mCachedRequest.take();
            } catch (InterruptedException e) {
                if (isInterrupted) {
                    break;
                }
            }

            if (request.state == Request.CANCELED) {
                // We need to skip this request.
                continue;
            }

            FileCache.Data data = mCache.get(request.getRequestKey());
            if (data == null || data.isExpired()) {
                mNetworkRequest.add(request);
                continue;
            }

            mCallback.success(request, data.data);
        }
    }

    public void cancel() {
        isInterrupted = true;
        interrupt();
    }
}
