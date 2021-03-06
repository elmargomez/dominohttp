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
import android.net.Uri;
import android.os.Message;

import com.elmargomez.dominohttp.DominoLog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class DownloadImage extends Request<File, DownloadImage.ImageInfo> {

    private String filename;

    public DownloadImage(SuccessListener<ImageInfo> successListener,
                         FailedListeners failedListenersListener) {
        super("DownloadImage", successListener, failedListenersListener);
        setContentType(IMAGE_JPEG);
    }

    @Override
    public byte[] getByteData() {
        return null;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    @Override
    public ImageInfo generateResponse(byte[] b) {
        ImageInfo imageInfo = new ImageInfo();
        File file = null;
        try {
            file = File.createTempFile(filename, ".JPG", getBody());
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(file);
            stream.write(b, 0, b.length);
            stream.flush();

            BitmapFactory.Options option = new BitmapFactory.Options();
            option.inJustDecodeBounds = true; // meaning decode only the size and no creation
            BitmapFactory.decodeFile(file.getAbsolutePath(), option);
            imageInfo.width = option.outWidth;
            imageInfo.height = option.outHeight;
            imageInfo.path = Uri.fromFile(file).toString();
            imageInfo.filename = filename;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return imageInfo;
    }

    @Override
    public int compareTo(Object another) {
        return 0;
    }

    public class ImageInfo {
        public int width;
        public int height;
        public String path;
        public String filename;
    }
}
