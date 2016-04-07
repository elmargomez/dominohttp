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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.elmargomez.dominohttp.ContentType;
import com.elmargomez.dominohttp.listener.FailedListener;
import com.elmargomez.dominohttp.listener.SuccessListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;

public class DownloadJPEGRequest extends Request {

    private SuccessListener<Bitmap> successListener;
    private String fileName;
    private String location;

    public DownloadJPEGRequest() {
        setContentType(ContentType.IMAGE_JPEG);
    }

    public DownloadJPEGRequest setSuccessListener(SuccessListener<Bitmap> success) {
        this.successListener = success;
        return this;
    }

    /**
     * Sets the name of the image without the file extension.
     *
     * @param fileName the desired file name of the image without file extension.
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setStorageLocation(String location) {
        this.location = location;
    }

    public int executed() {
        try {
            HttpURLConnection connection = getConnection();

            int respondCode = connection.getResponseCode();
            if (200 == respondCode) {
                if (successListener != null) {
                    InputStream inputStream = connection.getInputStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    inputStream.close();
                    successListener.response(this, bitmap);
                }
                return EXECUTION_REQUEST_SUCCESS;
            } else {
                FailedListener listener = getRequestFailedListener();
                if (listener != null) {
                    InputStream inputStream = connection.getErrorStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder builder = new StringBuilder();
                    String temp;
                    while ((temp = reader.readLine()) != null) {
                        builder.append(temp);
                    }
                    reader.close();
                    setErrorMessage(builder.toString());
                    listener.response(this, respondCode);
                }
                return EXECUTION_REQUEST_ERROR;
            }

        } catch (MalformedURLException e) {
            setErrorMessage("MalformedURLException :" + e.getMessage());
        } catch (IOException e) {
            setErrorMessage("IOException :" + e.getMessage());
        }

        return EXECUTION_FAILURE_ON_DEPLOY;
    }


    @Override
    public int compareTo(Request another) {
        return 0;
    }


    public static class Builder extends Request.Builder<DownloadJPEGRequest, Builder> {

        private SuccessListener<Bitmap> successListener;
        private String fileName;
        private String location;

        public Builder() {
            super(DownloadJPEGRequest.class);
        }

        public Builder setFileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public Builder setStorageLocation(String location) {
            this.location = location;
            return this;
        }

        public Builder setSuccessListener(SuccessListener<Bitmap> success) {
            this.successListener = success;
            return this;
        }

        @Override
        public DownloadJPEGRequest build() {

            if (fileName == null)
                throw new NullPointerException("Filename must not be null!");

            if (location == null)
                throw new NullPointerException("The target path must not be null!");

            DownloadJPEGRequest request = getBuildClass();
            request.setFileName(fileName);
            request.setStorageLocation(location);
            request.setSuccessListener(successListener);

            return request;
        }

    }

}
