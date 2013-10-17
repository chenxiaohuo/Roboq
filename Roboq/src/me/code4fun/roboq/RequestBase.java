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


@SuppressWarnings("unchecked")
class RequestBase<T extends RequestBase> {

    protected String url;
    protected final Request.Options options = new Request.Options();
    protected Request.Modifier modifier;
    protected String paramsEncoding;
    protected String fieldsEncoding;
    protected Integer retryCount = null;
    protected Boolean redirect = null;
    protected Integer connectionTimeout = null;
    protected Integer soTimeout = null;
    protected Boolean multipart = null;

    RequestBase() {
    }

    public String getUrl() {
        return url;
    }

    public T setUrl(String url) {
        this.url = url;
        return (T) this;
    }

    public Request.Options getOptions() {
        return options;
    }

    public T with(Request.Options options) {
        if (options != null && !options.isEmpty()) {
            this.options.putAll(options);
        }
        return (T) this;
    }

    public T with(Object... kvs) {
        return with(new Request.Options(kvs));
    }

    public T with(String k, Object v) {
        if (k != null) {
            this.options.put(k, v);
        }
        return (T) this;
    }

    public T withIf(boolean b, String k, Object v) {
        if (b) {
            with(k, v);
        }
        return (T) this;
    }

    public Request.Modifier getModifier() {
        return modifier;
    }

    public T setModifier(Request.Modifier modifier) {
        this.modifier = modifier;
        return (T) this;
    }

    public String getParamsEncoding() {
        return paramsEncoding;
    }

    public T setParamsEncoding(String paramsEncoding) {
        this.paramsEncoding = paramsEncoding;
        return (T) this;
    }

    public String getFieldsEncoding() {
        return fieldsEncoding;
    }

    public T setFieldsEncoding(String fieldsEncoding) {
        this.fieldsEncoding = fieldsEncoding;
        return (T) this;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public T setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
        return (T) this;
    }

    public Boolean getRedirect() {
        return redirect;
    }

    public T setRedirect(Boolean redirect) {
        this.redirect = redirect;
        return (T) this;
    }

    public Integer getConnectionTimeout() {
        return connectionTimeout;
    }

    public T setConnectionTimeout(Integer connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
        return (T) this;
    }

    public Integer getSoTimeout() {
        return soTimeout;
    }

    public T setSoTimeout(Integer soTimeout) {
        this.soTimeout = soTimeout;
        return (T) this;
    }

    public Boolean getMultipart() {
        return multipart;
    }

    public T setMultipart(Boolean multipart) {
        this.multipart = multipart;
        return (T) this;
    }
}
