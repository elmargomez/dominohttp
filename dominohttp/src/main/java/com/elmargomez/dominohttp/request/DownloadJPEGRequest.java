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

import android.graphics.BitmapFactory;

import com.elmargomez.dominohttp.ContentType;
import com.elmargomez.dominohttp.listener.FailedListener;
import com.elmargomez.dominohttp.listener.SuccessListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;

public class DownloadJPEGRequest extends Request {

    private SuccessListener<String> successListener;
    private String fileName;
    private String location;
    private int width;
    private int height;

    public DownloadJPEGRequest() {
        setContentType(ContentType.IMAGE_JPEG);
    }

    public DownloadJPEGRequest setSuccessListener(SuccessListener<String> success) {
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

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int executed() {
        FileOutputStream outputFile = null;
        InputStream in = null;
        BufferedReader errorReader = null;

        try {
            HttpURLConnection conn = getConnection();
            File emptyFile = File.createTempFile(fileName, ".jpg", new File(location));

            outputFile = new FileOutputStream(emptyFile);
            in = conn.getInputStream();
            byte[] buffer = new byte[1024];
            int n;
            while (-1 != (n = in.read(buffer))) {
                outputFile.write(buffer, 0, n);
            }

            int respondCode = conn.getResponseCode();
            if (200 == respondCode) {
                if (successListener != null) {

                    // get the width and height og the image
                    BitmapFactory.Options option = new BitmapFactory.Options();
                    option.inJustDecodeBounds = true; // meaning decode only the size and no creation
                    BitmapFactory.decodeFile(emptyFile.getAbsolutePath(), option);
                    width = option.outWidth;
                    height = option.outHeight;

                    successListener.response(this, location + fileName + ".jpg");
                }
                return EXECUTION_REQUEST_SUCCESS;
            } else {
                FailedListener listener = getRequestFailedListener();
                if (listener != null) {
                    InputStream inputStream = conn.getErrorStream();
                    errorReader = new BufferedReader(new InputStreamReader(inputStream));

                    StringBuilder builder = new StringBuilder();
                    String temp;
                    while ((temp = errorReader.readLine()) != null) {
                        builder.append(temp);
                    }

                    setErrorMessage(builder.toString());
                    listener.response(this, respondCode);
                }
                return EXECUTION_REQUEST_ERROR;
            }

        } catch (MalformedURLException e) {
            setErrorMessage("MalformedURLException :" + e.getMessage());
        } catch (IOException e) {
            setErrorMessage("IOException :" + e.getMessage());
        } finally {
            if (outputFile != null) {
                try {
                    outputFile.close();
                } catch (IOException e) {
                    //ignore
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    //ignore
                }
            }
            if (errorReader != null) {
                try {
                    errorReader.close();
                } catch (IOException e) {
                    //ignore
                }
            }
        }

        return EXECUTION_FAILURE_ON_DEPLOY;
    }


    @Override
    public int compareTo(Request another) {
        return 0;
    }


    public static class Builder extends Request.Builder<DownloadJPEGRequest, Builder> {

        private SuccessListener<String> successListener;
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

        public Builder setSuccessListener(SuccessListener<String> success) {
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
