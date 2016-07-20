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

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;

import com.elmargomez.dominohttp.RequestQueue;
import com.elmargomez.dominohttp.inter.SuccessResponse;
import com.elmargomez.dominohttp.parser.BitmapParser;
import com.elmargomez.dominohttp.parser.StringParser;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

public class WebRequest implements Comparable {

    protected Header mHeader;
    private Body mBody;

    private NetworkHeader mNetworkHeader = new NetworkHeader();
    private byte[] mRequestBody;

    private String mRequestID;
    private RequestQueue mRequestQueue;
    private Object mBind;
    private int mSuccessID = -1;
    private int mErrorID = -1;

    private boolean mShouldCached = true;
    private boolean mCanceled = false;
    private int mRetryCount = 3;

    /**
     * Check if the request is cancelled.
     *
     * @return true if canceled.
     */
    public boolean isCanceled() {
        return mCanceled;
    }

    public void setCanceled(boolean toCancel) {
        mCanceled = toCancel;
    }

    public boolean shouldCached() {
        return mShouldCached;
    }

    public void setCachable(boolean toCache) {
        mShouldCached = toCache;
    }

    public WebRequest successListener(int id) {
        mSuccessID = id;
        return this;
    }

    public WebRequest errorListener(int id) {
        mErrorID = id;
        return this;
    }

    /**
     * The Request ID of this Request.
     *
     * @return The Request-ID of this Request.
     */
    public String getRequestKey() {
        if (mRequestID == null) {
            mRequestID = UUID.randomUUID().toString();
        }
        return mRequestID;
    }

    public int getRetryCount() {
        return mRetryCount;
    }

    public void decRetryCount() {
        mRetryCount--;
    }

    @Override
    public int compareTo(Object another) {
        return 0;
    }

    public interface Header {
        void header(NetworkHeader header);
    }

    public interface Body {
        byte[] body();
    }

    protected WebRequest(RequestQueue requestQueue, Object object, Header header, Body body) {

        if (object == null) {
            throw new IllegalArgumentException("Object must not be null!");
        }

        if (header == null) {
            throw new IllegalArgumentException("Header must not be null!");
        }

        mRequestQueue = requestQueue;
        mBind = object;
        mHeader = header;
        mBody = body;
    }

//    public NetworkHeader getHeader() {
//        return mNetworkHeader;
//    }
//
//    public byte[] getBody() {
//        return mRequestBody;
//    }

    /**
     * Start executing the request.
     */
    public void execute() {
        // calls the listener
        mHeader.header(mNetworkHeader);
        //
        if (mBody != null) {
            mRequestBody = mBody.body();
        }
        mRequestQueue.add(this);
    }

    public static class ResponseSender {
        private static final BitmapParser BITMAP_PARSER = new BitmapParser();
        private static final StringParser STRING_PARSER = new StringParser();

        private Handler handler = null;

        public ResponseSender(Handler handler) {
            this.handler = handler;
        }

        public ResponseSender() {
            this(new Handler(Looper.getMainLooper()));
        }

        public void success(final WebRequest request, byte[] response) {

            if (request.mSuccessID < 0) {
                return;
            }

            Class<?> binder = request.mBind.getClass();
            for (Method method : binder.getDeclaredMethods()) {
                SuccessResponse annotation = method.getAnnotation(SuccessResponse.class);
                if (annotation != null && request.mSuccessID == annotation.id()) {
                    int i = annotation.responseType();
                    switch (i) {
                        case SuccessResponse.BITMAP_RESPONSE:
                            Bitmap bitmapOutput = BITMAP_PARSER.parse(response);
                            handler.post(new SuccessRunnable(request.mBind, bitmapOutput, method));
                            break;
                        case SuccessResponse.STRING_RESPONSE:
                            String stringOutput = STRING_PARSER.parse(response);
                            handler.post(new SuccessRunnable(request.mBind, stringOutput, method));
                            break;
                        default:
                            throw new UnsupportedOperationException("Unable to parse the request!");
                    }
                }
            }
        }

        public void failure(final WebRequest request, final String error) {
            //TODO complete this part
        }

        static class SuccessRunnable implements Runnable {

            private Object mObject;
            private Object mOutput;
            private Method mMethod;

            public SuccessRunnable(Object object, Object output, Method method) {
                mObject = object;
                mOutput = output;
                mMethod = method;
            }

            @Override
            public void run() {
                try {
                    mMethod.invoke(mObject, mOutput);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
