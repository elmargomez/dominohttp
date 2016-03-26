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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;

public class JSONObjectRequest extends Request<JSONObjectRequest> {

    protected OnSuccessListener<JSONObject> successListener;
    private String jsonBody;

    public JSONObjectRequest() {
        setContentType(ContentType.APPLICATION_JSON);
    }

    public JSONObjectRequest setJSONBody(String s) {
        jsonBody = s;
        return this;
    }

    public JSONObjectRequest setOnSuccessListener(OnSuccessListener<JSONObject> success) {
        this.successListener = success;
        return this;
    }

    public int executed() {
        try {
            HttpURLConnection connection = getConnection();

            if (jsonBody != null) {
                OutputStream outputStream = connection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream,
                        "UTF-8"));
                writer.write(jsonBody);
                writer.flush();
                writer.close();
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

                    JSONObject object = new JSONObject(builder.toString());
                    successListener.response(this, object);
                }
                return EXECUTION_REQUEST_SUCCESS;
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
                    setErrorMessage(builder.toString());
                    listener.response(this, respondCode);
                }
                return EXECUTION_REQUEST_ERROR;
            }

        } catch (MalformedURLException e) {
            setErrorMessage("MalformedURLException :" + e.getMessage());
        } catch (IOException e) {
            setErrorMessage("IOException :" + e.getMessage());
        } catch (JSONException e) {
            setErrorMessage("JSONException :" + e.getMessage());
        }
        return EXECUTION_FAILURE_ON_DEPLOY;
    }


}
