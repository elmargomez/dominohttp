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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Set;

public class UploadJPEGRequest extends Request {

    protected RequestSuccess<String> successListener;
    private String imagePath;

    public UploadJPEGRequest() {
        setContentType(ContentType.IMAGE_JPEG);
    }

    public void setImagePath(String s) {
        imagePath = s;
    }

    public void setSuccessListener(RequestSuccess<String> success) {
        this.successListener = success;
    }

    public void execute() {
        try {
            URL url = new URL(stringURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            connection.setRequestProperty("Content-Type", contentType);
            Set<Map.Entry<String, String>> entries = header.entrySet();
            for (Map.Entry<String, String> entry : entries) {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }

            if (imagePath == null)
                throw new NullPointerException("Image Path is null!");

            File file = new File(imagePath);
            FileInputStream fileInputStream = new FileInputStream(file);
            OutputStream outputStream = connection.getOutputStream();
            byte[] buff = new byte[1024];
            int count;
            while (-1 != (count = fileInputStream.read(buff))) {

            }

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
                if (failedListener != null) {
                    failedListener.response("Response code " + respondCode + "!");
                }
            }

        } catch (MalformedURLException e) {
            if (failedListener != null) {
                failedListener.response("MalformedURLException :" + e.getMessage());
            }
        } catch (IOException e) {
            if (failedListener != null) {
                failedListener.response("IOException :" + e.getMessage());
            }
        }
    }


}
