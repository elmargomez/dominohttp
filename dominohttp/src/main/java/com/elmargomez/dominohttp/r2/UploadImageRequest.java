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

package com.elmargomez.dominohttp.r2;

import com.elmargomez.dominohttp.Request;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class UploadImageRequest extends Request<File, String> {

    public UploadImageRequest(SuccessListener<String> successListenerListener,
                              FailedListeners failedListenersListener) {
        super(successListenerListener, failedListenersListener);
        setContentType(IMAGE_JPEG);
    }

    @Override
    public byte[] getByteData() {
        if (data == null) {
            data = new byte[]{}; // placeholder
            ByteArrayOutputStream bStream = new ByteArrayOutputStream();
            InputStream s = null;
            try {
                s = new BufferedInputStream(new FileInputStream(getBody()));
                final byte[] patch = new byte[1024];
                int bytesRead = 0;
                while (-1 != (bytesRead = s.read(patch))) {
                    bStream.write(patch, 0, bytesRead);
                }
                data = bStream.toByteArray();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (s != null) {
                        s.close();
                    }
                    bStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return data;
    }

    @Override
    public String generateResponse(byte[] b) {
        return new String(b);
    }


}
