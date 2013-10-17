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


import android.util.Log;
import me.code4fun.roboq.multipart.MultipartEntity;
import me.code4fun.roboq.multipart.content.ByteArrayBody;
import me.code4fun.roboq.multipart.content.FileBody;
import me.code4fun.roboq.multipart.content.InputStreamBody;
import me.code4fun.roboq.multipart.content.StringBody;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.RedirectHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.DefaultRedirectHandler;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HttpContext;

import java.io.*;
import java.lang.reflect.Array;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * Request描述一个HTTP请求，它的要素包含:<br/>
 * <table border="1">
 * <thead>
 * <tr>
 * <th>名称</th>
 * <th>说明</th>
 * <th>选项后缀</th>
 * </tr>
 * </thead>
 * <tbody>
 * <tr>
 * <td>Method</td
 * <td>HTTP的动词，例如GET,POST等</td>
 * <td>N/A</td>
 * </tr>
 * <tr>
 * <td>URL</td>
 * <td>HTTP请求的URL</td>
 * <td>N/A</td>
 * </tr>
 * <tr>
 * <td>PathVar</td>
 * <td>URL中的占位符号，例如URL "http://..../${id}/posts"中的"id"就是一个PathVar</td>
 * <td>$</td>
 * </tr>
 * <tr>
 * <td>Param</td>
 * <td>HTTP请求中附加在URL后的参数</td>
 * <td>=</td>
 * </tr>
 * <tr>
 * <td>Header</td>
 * <td>HTTP的头项目</td>
 * <td>:</td>
 * </tr>
 * <tr>
 * <td>Field</td>
 * <td>在POST请求中参数</td>
 * <td>@</td>
 * </tr>
 * </tbody>
 * </table>
 * 例如:
 * <pre>
 * Request req = new Request(
 *      POST,                                   // Method
 *      "http://.../${id}/posts",               // URL
 *      "User-Agent :", "RoboqClient",          // Header "User-Agent: RoboqClient"
 *      "param1     =", 3,                      // Param  "&param1=3"
 *      "id         $", "user@email.com",       // PathVar 替换URL中的"${id}"为"user@email.com",
 *      "file1      @", new File("/file/path")  // 上传文件使用"file1"作为Field名称
 * );
 * </pre>
 *
 * @since 0.1
 */
public class Request extends RequestBase<Request> {

    public static final String LOG_TAG = "Roboq.Request";

    public static final int GET = 1;
    public static final int POST = 2;
    public static final int PUT = 3;
    public static final int DELETE = 4;
    public static final int TRACE = 5;
    public static final int HEAD = 6;
    public static final int OPTIONS = 7;

    private static final String DEFAULT_BINARY_CONTENT_TYPE = "application/octet-stream";
    private static final String DEFAULT_TEXT_CONTENT_TYPE = "text/plain";

    protected final Prepared prepared;
    protected int method;
    protected Object tag;

    public Request(int method, String url) {
        this(null, method, url, (Options) null);
    }

    public Request(int method, String url, Object... options) {
        this(null, method, url, options);
    }

    public Request(int method, String url, Options options) {
        this(null, method, url, options);
    }

    public Request(Prepared prepared, int method, String url) {
        this(prepared, method, url, (Options) null);
    }

    public Request(Prepared prepared, int method, String url, Object... options) {
        this(prepared, method, url, new Options(options));
    }

    public Request(Prepared prepared, int method, String url, Options options) {
        this.prepared = prepared;
        setMethod(method);
        setUrl(url);
        with(options);
    }

    public Prepared getPrepared() {
        return prepared;
    }

    public int getMethod() {
        return method;
    }

    public Request setMethod(int method) {
        this.method = method;
        return this;
    }

    public String getMethodAsText() {
        return getMethodAsText(this.method);
    }

    public Object getTag() {
        return tag;
    }

    public Request setTag(Object tag) {
        this.tag = tag;
        return this;
    }


    protected int mergeMethod() {
        int method1 = method;
        if (prepared != null && prepared.modifier != null)
            method1 = prepared.modifier.modifyMethod(method1);
        if (modifier != null)
            method1 = modifier.modifyMethod(method1);

        return method1;
    }

    protected String mergeUrl() {
        String url1;
        if (url != null) {
            if (url.startsWith("http://") || url.startsWith("https://")) {
                url1 = url;
            } else {
                if (prepared != null && prepared.url != null)
                    url1 = prepared.url + url;
                else
                    url1 = url;
            }
        } else {
            url1 = prepared != null && prepared.url != null ? prepared.url : null;
        }

        if (prepared != null && prepared.modifier != null)
            url1 = prepared.modifier.modifyUrl(url1);
        if (modifier != null)
            url1 = modifier.modifyUrl(url1);

        return url1;
    }

    protected Options mergeOptions() {
        Options options1 = new Options();
        if (prepared != null) {
            options1.putAll(prepared.options);
        }
        options1.putAll(options);

        if (prepared != null && prepared.modifier != null)
            options1 = prepared.modifier.modifyOptions(options1);
        if (modifier != null)
            options1 = modifier.modifyOptions(options1);

        if (options1 == null)
            options1 = new Options();

        return options1;
    }

    protected static <T> T selectValue(T val, T preparedVal, T def) {
        if (val != null)
            return val;

        if (preparedVal != null)
            return preparedVal;

        return def;
    }

    protected Response createResponse(HttpResponse httpResp) {
        return new Response(httpResp);
    }

    private String makeUrl(String url, Options opts) {
        String url1 = url;

        // expand path vars
        for (Map.Entry<String, Object> entry : opts.getPathVars().entrySet()) {
            String k = entry.getKey();
            url1 = url1.replace("${" + k + "}", o2s(entry.getValue(), ""));
        }

        // params
        ArrayList<NameValuePair> nvl = new ArrayList<NameValuePair>();
        for (Map.Entry<String, Object> entry : opts.getParams().entrySet()) {
            nvl.add(new BasicNameValuePair(entry.getKey(), o2s(entry.getValue(), "")));
        }
        if (nvl.isEmpty()) {
            return url1;
        } else {
            return url1 + "?" + URLEncodedUtils.format(nvl,
                    selectValue(paramsEncoding, prepared != null ? prepared.paramsEncoding : null, "UTF-8"));
        }
    }


    protected HttpUriRequest createHttpRequest(int method, String url, Options opts) {
        String url1 = makeUrl(url, opts);

        // Create HTTP request
        HttpUriRequest httpReq;
        if (GET == method) {
            httpReq = new HttpGet(url1);
        } else if (POST == method) {
            httpReq = new HttpPost(url1);
        } else if (PUT == method) {
            httpReq = new HttpPut(url1);
        } else if (DELETE == method) {
            httpReq = new HttpDelete(url1);
        } else if (TRACE == method) {
            httpReq = new HttpTrace(url1);
        } else if (HEAD == method) {
            httpReq = new HttpHead(url1);
        } else if (OPTIONS == method) {
            httpReq = new HttpOptions(url1);
        } else {
            throw new IllegalStateException("Illegal HTTP method " + method);
        }

        // Set headers
        Map<String, Object> headers = opts.getHeaders();
        for (Map.Entry<String, Object> entry : headers.entrySet()) {
            String k = entry.getKey();
            Object v = entry.getValue();
            if (v != null && v.getClass().isArray()) {
                int len = Array.getLength(v);
                for (int i = 0; i < len; i++) {
                    Object v0 = Array.get(v, i);
                    httpReq.addHeader(k, o2s(v0, ""));
                }
            } else if (v instanceof List) {
                for (Object v0 : (List) v)
                    httpReq.addHeader(k, o2s(v0, ""));
            } else {
                httpReq.addHeader(k, o2s(v, ""));
            }
        }

        // set body
        if (httpReq instanceof HttpEntityEnclosingRequestBase) {
            ((HttpEntityEnclosingRequestBase) httpReq).setEntity(createRequestBody(method, url, opts));
        }

        return httpReq;
    }


    protected HttpClient createHttpClient(int method, String url, Options opts) {
        DefaultHttpClient httpClient = new DefaultHttpClient();

        // retry count
        int retryCount1 = selectValue(retryCount, prepared != null ? prepared.retryCount : null, 1);
        if (retryCount1 <= 0)
            retryCount1 = 1;
        httpClient.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(retryCount1, false));

        // redirect
        boolean redirect1 = selectValue(redirect, prepared != null ? prepared.redirect : null, true);
        if (redirect1) {
            httpClient.setRedirectHandler(new DefaultRedirectHandler());
        } else {
            httpClient.setRedirectHandler(new RedirectHandler() {
                @Override
                public boolean isRedirectRequested(HttpResponse httpResponse, HttpContext httpContext) {
                    return false;
                }

                @Override
                public URI getLocationURI(HttpResponse httpResponse, HttpContext httpContext) {
                    return null;
                }
            });
        }

        // connection timeout
        Integer connTimeout1 = selectValue(connectionTimeout, prepared != null ? prepared.connectionTimeout : null, 20000);
        httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, connTimeout1);

        // so timeout
        Integer soTimeout1 = selectValue(soTimeout, prepared != null ? prepared.soTimeout : null, 0);
        httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, soTimeout1);

        return httpClient;
    }

    protected String o2s(Object obj, String def) {
        return obj != null ? obj.toString() : def;
    }

    protected String getFileContentType(File f) {
        // TODO: 获取常用文件扩展名的content-type
        return DEFAULT_BINARY_CONTENT_TYPE;
    }

    protected HttpEntity createRequestBody(int method, String url, Options opts) {
        BodyOutputDecorator bod = getBodyOutputDecorator();
        Object bodyObj = opts.getBody();
        if (bodyObj != null) {
            return createBody(bodyObj, bod);
        } else {
            return createMultipartBody(opts.getFields(), bod);
        }
    }

    protected static abstract class BodyOutputDecorator {
        public Object before() {
            return null;
        }

        public void after(HttpEntity body, Object concomitant) {
        }

        public abstract OutputStream decorate(OutputStream out, Object concomitant);
    }

    protected BodyOutputDecorator getBodyOutputDecorator() {
        return null;
    }

    private static OutputStream decorateOutput(BodyOutputDecorator bod, OutputStream out, Object concomitant) {
        return bod != null ? bod.decorate(out, concomitant) : out;
    }

    private HttpEntity createBody(Object o, final BodyOutputDecorator bod) {
        Object obj1 = Value.getLength(o);
        String ct1 = Value.getContentType(o);

        final Object concomitant = bod != null ? bod.before() : null;
        if (obj1 instanceof byte[]) {
            ByteArrayEntity entity = new ByteArrayEntity((byte[]) obj1) {
                @Override
                public void writeTo(OutputStream outstream) throws IOException {
                    super.writeTo(decorateOutput(bod, outstream, concomitant));
                }
            };
            entity.setContentType(ct1 != null ? ct1 : DEFAULT_BINARY_CONTENT_TYPE);
            if (bod != null) {
                bod.after(entity, concomitant);
            }
            return entity;
        } else if (obj1 instanceof InputStream) {
            long len = Value.getLength(o, -1);
            InputStreamEntity entity = new InputStreamEntity((InputStream) obj1, len) {
                @Override
                public void writeTo(OutputStream outstream) throws IOException {
                    super.writeTo(decorateOutput(bod, outstream, concomitant));
                }
            };
            entity.setContentType(ct1 != null ? ct1 : DEFAULT_BINARY_CONTENT_TYPE);
            if (bod != null) {
                bod.after(entity, concomitant);
            }
            return entity;
        } else if (obj1 instanceof File) {
            File f = (File) obj1;
            FileEntity entity = new FileEntity(f, ct1 != null ? ct1 : getFileContentType(f)) {
                @Override
                public void writeTo(OutputStream outstream) throws IOException {
                    super.writeTo(decorateOutput(bod, outstream, concomitant));
                }
            };
            if (bod != null) {
                bod.after(entity, concomitant);
            }
            return entity;
        } else {
            String s = o2s(obj1, "");
            String encoding = selectValue(fieldsEncoding,
                    prepared != null ? prepared.fieldsEncoding : null, "UTF-8");
            try {
                StringEntity entity;
                entity = new StringEntity(s, encoding) {
                    @Override
                    public void writeTo(OutputStream outstream) throws IOException {
                        super.writeTo(decorateOutput(bod, outstream, concomitant));
                    }
                };
                entity.setContentType(ct1 != null ? ct1 : DEFAULT_TEXT_CONTENT_TYPE);
                if (bod != null) {
                    bod.after(entity, concomitant);
                }
                return entity;
            } catch (UnsupportedEncodingException e) {
                throw new RoboqException("Illegal encoding for request body", e);
            }
        }
    }

    private static boolean isBytesEquivalent(Object obj) {
        return obj instanceof byte[] || obj instanceof InputStream || obj instanceof File;
    }

    private HttpEntity createMultipartBody(Map<String, Object> fields, final BodyOutputDecorator bod) {
        // multipart post?
        boolean multipart1 = selectValue(multipart, prepared != null ? prepared.multipart : null, false);
        for (Object obj : fields.values()) {
            if (isBytesEquivalent(Value.getLength(obj))) {
                multipart1 = true;
                break;
            }
        }

        final Object concomitant = bod != null ? bod.before() : null;
        String encoding = selectValue(fieldsEncoding, prepared != null ? prepared.fieldsEncoding : null, "UTF-8");
        try {
            if (multipart1) {

                MultipartEntity entity = new MultipartEntity();
                for (Map.Entry<String, Object> entry : fields.entrySet()) {
                    String field = entry.getKey();
                    Object obj1 = Value.getLength(entry.getValue());
                    String ct1 = Value.getContentType(entry.getValue());
                    String fn1 = Value.getFilename(entry.getValue());
                    if (isBytesEquivalent(obj1)) {
                        if (obj1 instanceof byte[]) {
                            ByteArrayBody cb = new ByteArrayBody((byte[]) obj1,
                                    ct1 != null ? ct1 : DEFAULT_BINARY_CONTENT_TYPE,
                                    fn1 != null ? fn1 : "") {
                                @Override
                                public void writeTo(OutputStream out) throws IOException {
                                    super.writeTo(decorateOutput(bod, out, concomitant));
                                }
                            };
                            entity.addPart(field, cb);
                        } else if (obj1 instanceof InputStream) {
                            InputStreamBody cb = new InputStreamBody((InputStream) obj1,
                                    ct1 != null ? ct1 : DEFAULT_BINARY_CONTENT_TYPE,
                                    fn1 != null ? fn1 : "") {
                                @Override
                                public void writeTo(OutputStream out) throws IOException {
                                    super.writeTo(decorateOutput(bod, out, concomitant));
                                }
                            };
                            entity.addPart(field, cb);
                        } else if (obj1 instanceof File) {
                            File f = (File) obj1;
                            FileBody cb = new FileBody(f, fn1 != null ? fn1 : null,
                                    ct1 != null ? ct1 : getFileContentType(f), encoding) {
                                @Override
                                public void writeTo(OutputStream out) throws IOException {
                                    super.writeTo(decorateOutput(bod, out, concomitant));
                                }
                            };
                            entity.addPart(field, cb);
                        } else {
                            throw new RoboqException("Here is unreached");
                        }
                    } else {
                        String s = o2s(obj1, "");
                        StringBody cb = new StringBody(s, ct1 != null ? ct1 : DEFAULT_TEXT_CONTENT_TYPE,
                                Charset.forName(encoding)) {
                            @Override
                            public void writeTo(OutputStream out) throws IOException {
                                super.writeTo(decorateOutput(bod, out, concomitant));
                            }
                        };
                        entity.addPart(field, cb);
                    }
                }
                if (bod != null) {
                    bod.after(entity, concomitant);
                }
                return entity;
            } else {
                ArrayList<NameValuePair> nvl = new ArrayList<NameValuePair>();
                for (Map.Entry<String, Object> entry : fields.entrySet()) {
                    Object obj1 = Value.getLength(entry.getValue());
                    nvl.add(new BasicNameValuePair(entry.getKey(), o2s(obj1, "")));
                }

                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(nvl, encoding) {
                    @Override
                    public void writeTo(OutputStream outstream) throws IOException {
                        super.writeTo(decorateOutput(bod, outstream, concomitant));
                    }
                };
                if (bod != null) {
                    bod.after(entity, concomitant);
                }
                return entity;
            }
        } catch (UnsupportedEncodingException e) {
            throw new RoboqException(e);
        }
    }

    private static String getMethodAsText(int method) {
        switch (method) {
            case GET: return "GET";
            case POST: return "POST";
            case PUT: return "PUT";
            case DELETE: return "DELETE";
            case OPTIONS: return "OPTIONS";
            case TRACE: return "TRACE";
            case HEAD: return "HEAD";
        }
        return "";
    }

    private static String formatRequest(int method, String url, Options opts) {
        StringBuilder buff = new StringBuilder();
        buff.append("HTTP ").append(getMethodAsText(method)).append(" ").append(url);
        // TODO: 加入显示opts
        return buff.toString();
    }

    public Response execute() {
        int method = mergeMethod();
        String url = mergeUrl();
        Options opts = mergeOptions();

        HttpClient httpClient = createHttpClient(method, url, opts);
        HttpUriRequest httpReq = createHttpRequest(method, url, opts);
        try {
            if (Log.isLoggable(LOG_TAG, Log.DEBUG)) {
                Log.d(LOG_TAG, formatRequest(method, url, opts));
            }
            HttpResponse httpResp = httpClient.execute(httpReq);
            return createResponse(httpResp);
        } catch (IOException e) {
            throw new RoboqException(e);
        }
    }

    public void execute(Executor executor, final Callback cb) {
        Executor executor1 = selectValue(executor, prepared != null ? prepared.executor : null, null);
        if (executor1 == null)
            throw new NullPointerException("The executor is null");

        executor1.execute(new Runnable() {
            @Override
            public void run() {
                Response resp;
                Exception error;
                try {
                    resp = execute();
                    error = null;
                } catch (Exception e) {
                    resp = null;
                    error = e;
                }
                if (cb != null)
                    cb.onResponse(Request.this, resp, error);
            }
        });
    }

    public void execute(Callback cb) {
        execute(null, cb);
    }

    public static class Options extends LinkedHashMap<String, Object> {
        public Options() {
        }

        public Options(Map<? extends String, ?> map) {
            super(map);
        }

        public Options(Object... kvs) {
            if (kvs == null)
                throw new NullPointerException("kvs is null");
            if (kvs.length % 2 != 0)
                throw new IllegalArgumentException("Illegal kvs pair");

            for (int i = 0; i < kvs.length; i += 2) {
                Object k = kvs[i];
                Object v = kvs[i + 1];
                if (k == null)
                    throw new NullPointerException("The key is null");

                put(k.toString(), v);
            }
        }

        public Options add(Map<String, Object> m) {
            if (m != null && !m.isEmpty()) {
                putAll(m);
            }
            return this;
        }

        public Options add(String k, Object v) {
            put(k, v);
            return this;
        }

        public Options addIf(boolean b, String k, Object v) {
            if (b) {
                put(k, v);
            }
            return this;
        }

        public Map<String, Object> getPathVars() {
            return filter("$");
        }

        public Map<String, Object> getHeaders() {
            return filter(":");
        }

        public Map<String, Object> getParams() {
            return filter("=");
        }

        public Map<String, Object> getFields() {
            return filter("@");
        }

        public Object getBody() {
            return get("@");
        }


        protected Map<String, Object> filter(String keyPostfix) {
            LinkedHashMap<String, Object> m = new LinkedHashMap<String, Object>();
            for (Map.Entry<String, Object> entry : this.entrySet()) {
                String k = entry.getKey();
                if (k != null && k.endsWith(keyPostfix)) {
                    k = k.substring(0, k.length() - keyPostfix.length()).trim();
                    if (k != null && k.length() > 0)
                        m.put(k, entry.getValue());
                }
            }
            return m;
        }
    }

    public static class Prepared extends RequestBase<Prepared> {

        protected Executor executor;

        public Prepared() {
        }

        public Prepared(String url) {
            this(url, (Options) null);
        }

        public Prepared(String url, Object... options) {
            this(url, new Options(options));
        }

        public Prepared(String url, Options options) {
            setUrl(url);
            with(options);
        }

        public Executor getExecutor() {
            return executor;
        }

        public Prepared setExecutor(Executor executor) {
            this.executor = executor;
            return this;
        }
    }

    public static interface Callback {
        void onResponse(Request req, Response resp, Exception error);
    }

    public static abstract class Modifier {

        protected Modifier() {
        }

        public int modifyMethod(int method) {
            return method;
        }

        public String modifyUrl(String url) {
            return url;
        }

        public Options modifyOptions(Options options) {
            return options;
        }
    }

    public static class Value {
        public final Object object;
        public final String contentType;
        public final String filename;
        public long length;

        public Value(Object object, String contentType) {
            this(object, contentType, null);
        }

        public Value(Object object, String contentType, String filename) {
            this.object = object;
            this.contentType = contentType;
            this.filename = filename;
        }

        public Value withLength(long len) {
            this.length = len;
            return this;
        }

        public static Object getLength(Object obj) {
            return obj instanceof Value ? ((Value) obj).object : obj;
        }

        public static String getContentType(Object obj) {
            return obj instanceof Value ? ((Value) obj).contentType : null;
        }

        public static String getFilename(Object obj) {
            return obj instanceof Value ? ((Value) obj).filename : null;
        }

        public static long getLength(Object obj, long def) {
            return obj instanceof Value ? ((Value) obj).length : def;
        }
    }
}
