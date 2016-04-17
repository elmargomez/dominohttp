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

import android.content.Context;

import java.io.File;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

public class RequestQueue {
    private static final int THREAD_DEF_SIZE = 4;

    private final BlockingQueue<Request> requestQueue = new PriorityBlockingQueue<>();
    private RequestDispatcher[] dispatchers;

    private HttpClient httpClient = null;
    private CacheBank cacheBank = null;

    public RequestQueue(HttpClient client, CacheBank cacheBank) {
        this(client, cacheBank, THREAD_DEF_SIZE);
    }

    public RequestQueue(HttpClient client, CacheBank cacheBank, int threadCount) {
        this.httpClient = client;
        this.cacheBank = cacheBank;
        this.dispatchers = new RequestDispatcher[threadCount];
    }

    public static RequestQueue getInstance(Context context, HttpClient client) {
        if (client == null) {
            client = new HttpClient();
        }

        File file = new File(context.getCacheDir(), DiskCache.makeFolderName("Domi"));
        return new RequestQueue(client, new DiskCache(file));
    }

    public void request(Request request, String id) {
        request.responseID = id;
        requestQueue.add(request);
    }

    public void remove(Request request) {
        requestQueue.remove(request);
    }

    public void start() {

    }

    public void stop() {

    }

    public static void listen(Response response, String id) {

    }

    public static void unListen(Response response, String id) {

    }

}
