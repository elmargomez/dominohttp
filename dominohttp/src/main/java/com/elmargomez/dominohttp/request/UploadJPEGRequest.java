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

import com.elmargomez.dominohttp.ContentType;
import com.elmargomez.dominohttp.listener.FailedListener;
import com.elmargomez.dominohttp.listener.SuccessListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;

public class UploadJPEGRequest extends Request {

    private SuccessListener<String> successListener;
    private String imagePath;

    public UploadJPEGRequest() {
        setContentType(ContentType.IMAGE_JPEG);
    }

    public void setImageURI(String s) {
        imagePath = s;
    }

    public void setSuccessListener(SuccessListener<String> success) {
        this.successListener = success;
    }

    public int executed() {
        try {
            HttpURLConnection connection = getConnection();

            File file = new File(imagePath);
            FileInputStream fileInputStream = new FileInputStream(file);
            OutputStream outputStream = connection.getOutputStream();
            byte[] buff = new byte[1024];
            int count;
            while (-1 != (count = fileInputStream.read(buff))) {
                outputStream.write(buff, 0, count);
            }
            outputStream.flush();
            outputStream.close();

            int respondCode = connection.getResponseCode();
            if (200 == respondCode) {
                if (successListener != null) {
                    InputStream inputStream = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder builder = new StringBuilder();
                    String temp;
                    while ((temp = reader.readLine()) != null) {
                        builder.append(temp);
                    }
                    reader.close();
                    successListener.response(this, builder.toString());
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

    public static class Builder extends Request.Builder<UploadJPEGRequest, Builder> {

        private SuccessListener<String> successListener;
        private String imagePath;

        public Builder() {
            super(UploadJPEGRequest.class);
        }

        public Builder setSuccessListener(SuccessListener<String> success) {
            this.successListener = success;
            return this;
        }

        public Builder setImageURI(String s) {
            imagePath = s;
            return this;
        }

        @Override
        public UploadJPEGRequest build() {

            if (imagePath == null)
                throw new NullPointerException("Image Path is null!");

            UploadJPEGRequest c = getBuildClass();
            c.setImageURI(imagePath);
            c.setSuccessListener(successListener);
            return null;
        }
    }
}
