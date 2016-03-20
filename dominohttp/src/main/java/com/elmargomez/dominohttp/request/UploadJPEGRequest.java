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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;

public class UploadJPEGRequest extends Request<UploadJPEGRequest> {

    protected OnSuccessListener<String> successListener;
    private String imagePath;

    public UploadJPEGRequest() {
        setContentType(ContentType.IMAGE_JPEG);
    }

    public UploadJPEGRequest setURI(String s) {
        imagePath = s;
        return this;
    }

    public UploadJPEGRequest setSuccessListener(OnSuccessListener<String> success) {
        this.successListener = success;
        return this;
    }

    public void execute() {
        try {
            HttpURLConnection connection = getConnection();

            if (imagePath == null)
                throw new NullPointerException("Image Path is null!");

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
                    successListener.response(builder.toString());
                }
            } else {
                OnRequestFailedListener listener = getRequestFailedListener();
                if (listener != null) {
                    InputStream inputStream = connection.getErrorStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder builder = new StringBuilder();
                    String temp;
                    while ((temp = reader.readLine()) != null) {
                        builder.append(temp);
                    }
                    reader.close();
                    listener.response(builder.toString(), connection.getResponseCode());
                }
            }

        } catch (MalformedURLException e) {
            OnInternalFailedListener listener = getInternalFailedListener();
            if (listener != null) {
                listener.response("MalformedURLException :" + e.getMessage());
            }
        } catch (IOException e) {
            OnInternalFailedListener listener = getInternalFailedListener();
            if (listener != null) {
                listener.response("IOException :" + e.getMessage());
            }
        }
    }


}
