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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;

public class JSONRequest extends Request {

    private SuccessListener<JSONObject> successListenerObject;
    private SuccessListener<JSONArray> successListenerArray;
    private String jsonBody;

    public JSONRequest() {
        setContentType(ContentType.APPLICATION_JSON);
    }

    public void setJSONBody(String s) {
        jsonBody = s;
    }

    public void setJSONBody(JSONObject s) {
        setJSONBody(s.toString());
    }

    public void setJSONBody(JSONArray s) {
        setJSONBody(s.toString());
    }

    public void setJSONObjectRequestListener(SuccessListener<JSONObject> success) {
        this.successListenerObject = success;
    }

    public void setJSONArrayRequestListener(SuccessListener<JSONArray> success) {
        this.successListenerArray = success;
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
                InputStream inputStream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder builder = new StringBuilder();
                String temp;
                while ((temp = reader.readLine()) != null) {
                    builder.append(temp);
                }

                reader.close();
                String s = builder.toString();
                Object object = new JSONTokener(s).nextValue();

                if (successListenerObject != null &&
                        (object instanceof JSONObject || object.toString().equals("null"))) {
                    successListenerObject.response(this, (JSONObject) object);
                } else if (successListenerArray != null && object instanceof JSONArray) {
                    successListenerArray.response(this, (JSONArray) object);
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
        } catch (JSONException e) {
            setErrorMessage("JSONException :" + e.getMessage());
        }
        return EXECUTION_FAILURE_ON_DEPLOY;
    }


    @Override
    public int compareTo(Request another) {
        return 0;
    }

    public static class Builder extends Request.Builder<JSONRequest, Builder> {

        private SuccessListener<JSONObject> successListenerObject;
        private SuccessListener<JSONArray> successListenerArray;
        private String jsonBody;

        public Builder() {
            super(JSONRequest.class);
        }

        public void setJSONBody(String s) {
            jsonBody = s;
        }

        public void setJSONBody(JSONObject s) {
            setJSONBody(s.toString());
        }

        public void setJSONBody(JSONArray s) {
            setJSONBody(s.toString());
        }

        public void setJSONObjectRequestListener(SuccessListener<JSONObject> success) {
            this.successListenerObject = success;
        }

        public void setJSONArrayRequestListener(SuccessListener<JSONArray> success) {
            this.successListenerArray = success;
        }

        @Override
        public JSONRequest build() {
            JSONRequest request = getBuildClass();
            request.setJSONBody(jsonBody);
            request.setJSONArrayRequestListener(successListenerArray);
            request.setJSONObjectRequestListener(successListenerObject);
            return request;
        }

    }
}
