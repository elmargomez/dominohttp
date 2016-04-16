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
        OutputStream outputStream = null;
        BufferedWriter buffWriter = null;
        InputStream resultStream = null;
        BufferedReader resultReader = null;
        InputStream errorStream = null;
        BufferedReader errorReader = null;

        try {
            HttpURLConnection connection = getConnection();

            if (jsonBody != null) {
                outputStream = connection.getOutputStream();
                buffWriter = new BufferedWriter(new OutputStreamWriter(outputStream,
                        "UTF-8"));
                buffWriter.write(jsonBody);
                buffWriter.flush();
            }

            int respondCode = connection.getResponseCode();
            if (200 == respondCode) {
                resultStream = connection.getInputStream();
                resultReader = new BufferedReader(new InputStreamReader(resultStream));

                StringBuilder builder = new StringBuilder();
                String temp;
                while ((temp = resultReader.readLine()) != null) {
                    builder.append(temp);
                }
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
                    errorStream = connection.getErrorStream();
                    errorReader = new BufferedReader(new InputStreamReader(errorStream));
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
        } catch (JSONException e) {
            setErrorMessage("JSONException :" + e.getMessage());
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    //ignore
                }
            }
            if (buffWriter != null) {
                try {
                    buffWriter.close();
                } catch (IOException e) {
                    //ignore
                }
            }
            if (resultStream != null) {
                try {
                    resultStream.close();
                } catch (IOException e) {
                    //ignore
                }
            }
            if (resultReader != null) {
                try {
                    resultReader.close();
                } catch (IOException e) {
                    //ignore
                }
            }
            if (errorStream != null) {
                try {
                    errorStream.close();
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

    public static class Builder extends Request.Builder<JSONRequest, Builder> {

        private SuccessListener<JSONObject> successListenerObject;
        private SuccessListener<JSONArray> successListenerArray;
        private String jsonBody;

        public Builder() {
            super(JSONRequest.class);
        }

        public Builder setJSONBody(String s) {
            jsonBody = s;
            return this;
        }

        public Builder setJSONBody(JSONObject s) {
            setJSONBody(s.toString());
            return this;
        }

        public Builder setJSONBody(JSONArray s) {
            setJSONBody(s.toString());
            return this;
        }

        public Builder setJSONObjectRequestListener(SuccessListener<JSONObject> success) {
            this.successListenerObject = success;
            return this;
        }

        public Builder setJSONArrayRequestListener(SuccessListener<JSONArray> success) {
            this.successListenerArray = success;
            return this;
        }

        @Override
        public JSONRequest build() {
            JSONRequest request = getBuildClass();
            // check first if the parameter was properly supplied.
            request.validateParameters();
            request.setJSONBody(jsonBody);
            request.setJSONArrayRequestListener(successListenerArray);
            request.setJSONObjectRequestListener(successListenerObject);
            return request;
        }

    }
}
