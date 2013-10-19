/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package me.code4fun.roboq;


import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 描述一个HTTP请求的返回结果
 *
 * @since 0.1
 */
public class Response {

    protected final HttpResponse httpResponse;
    private volatile InputStream content;

    public Response(HttpResponse httpResponse) {
        this.httpResponse = httpResponse;
    }

    public HttpResponse getHttpResponse() {
        return httpResponse;
    }

    public int statusCode() {
        return httpResponse.getStatusLine().getStatusCode();
    }

    public String contentType() {
        Header header = httpResponse.getEntity().getContentType();
        return header != null ? header.getValue() : null;
    }

    public long contentLength() {
        return httpResponse.getEntity().getContentLength();
    }

    public Map<String, String> headers() {
        LinkedHashMap<String, String> headers = new LinkedHashMap<String, String>();
        for (Header h : httpResponse.getAllHeaders()) {
            headers.put(h.getName(), h.getValue());
        }
        return headers;
    }

    public String header(String name) {
        return header(name, null);
    }

    public String header(String name, String def) {
        Header hs = httpResponse.getFirstHeader(name);
        return hs != null ? hs.getValue() : def;
    }

    public boolean hasHeader(String name) {
        return header(name, null) != null;
    }

    private static void copy(InputStream in, OutputStream out) throws IOException {
        try {
            byte[] buff = new byte[2048];
            int n;
            while ((n = in.read(buff)) != -1) {
                if (n > 0)
                    out.write(buff, 0, n);
            }
        } finally {
            if (in != null)
                in.close();
        }
    }

    private static byte[] copyToArray(InputStream in) throws IOException {
        ByteArrayOutputStream buff = new ByteArrayOutputStream();
        copy(in, buff);
        return buff.toByteArray();
    }

    private InputStream getContentInputStream() throws IOException {
        if (content == null) {
            content = decorateContent(httpResponse.getEntity().getContent());
        }
        return content;
    }

    protected InputStream decorateContent(InputStream content) {
        return content;
    }

    public InputStream content() {
        try {
            return getContentInputStream();
        } catch (IOException e) {
            throw new RoboqException(e);
        }
    }

    public byte[] asBytes() {
        try {
            return copyToArray(getContentInputStream());
        } catch (IOException e) {
            throw new RoboqException(e);
        }
    }

    public String asText(String encoding) {
        try {
            return new String(asBytes(), encoding);
        } catch (UnsupportedEncodingException e) {
            throw new RoboqException(e);
        }
    }

    private final static Pattern catchCharsetPatt = Pattern.compile("charset=((\\w|-)+)");
    public String asText() {
        String cs = null;
        String ct = header("Content-Type", null);
        if (ct != null) {
            Matcher m = catchCharsetPatt.matcher(ct);
            if (m.find())
                cs = m.group(1);
        }
        if (cs == null || cs.length() == 0) {
            cs = "UTF-8";
        }
        return asText(cs);
    }



    public Object asJson() {
        try {
            JSONTokener parser = new JSONTokener(asText());
            return parser.nextValue();
        } catch (JSONException e) {
            throw new RoboqException("Parse json error", e);
        }
    }

    public JSONObject asJsonObject() {
        return (JSONObject) asJson();
    }

    public JSONArray asJsonArray() {
        return (JSONArray) asJson();
    }

    public <R> R as(ResultMapper<R> rm) {
        return rm.getResult(this);
    }

    public void write(OutputStream out) {
        try {
            copy(content(), out);
        } catch (IOException e) {
            throw new RoboqException(e);
        }
    }

    public void writeFile(File f, boolean append) {
        try {
            FileOutputStream out = new FileOutputStream(f, append);
            write(out);
        } catch (FileNotFoundException e) {
            throw new RoboqException(e);
        }
    }

    public void writeFile(File f) {
        writeFile(f, false);
    }

    public static interface ResultMapper<R> {
        R getResult(Response resp);
    }
}
