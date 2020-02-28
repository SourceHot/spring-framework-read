/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.http.client.support;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * {@link ClientHttpRequestInterceptor} to apply a given HTTP Basic Authentication
 * username/password pair, unless a custom Authorization header has been set before.
 * <p>
 * http 基本验证
 *
 * @author Juergen Hoeller
 * @see HttpHeaders#setBasicAuth
 * @see HttpHeaders#AUTHORIZATION
 * @since 5.1.1
 */
public class BasicAuthenticationInterceptor implements ClientHttpRequestInterceptor {

    /**
     * 用户名
     */
    private final String username;
    /**
     * 密码
     */
    private final String password;

    @Nullable
    private final Charset charset;


    /**
     * Create a new interceptor which adds Basic Authentication for the
     * given username and password.
     * <p>
     * 构造器
     *
     * @param username the username to use
     * @param password the password to use
     * @see HttpHeaders#setBasicAuth(String, String)
     */
    public BasicAuthenticationInterceptor(String username, String password) {
        this(username, password, null);
    }

    /**
     * Create a new interceptor which adds Basic Authentication for the
     * given username and password, encoded using the specified charset.
     *
     * @param username the username to use
     * @param password the password to use
     * @param charset  the charset to use
     * @see HttpHeaders#setBasicAuth(String, String, Charset)
     */
    public BasicAuthenticationInterceptor(String username, String password, @Nullable Charset charset) {
        Assert.doesNotContain(username, ":", "Username must not contain a colon");
        this.username = username;
        this.password = password;
        this.charset = charset;
    }


    /**
     * 拦截器创建?
     *
     * @param request   the request, containing method, URI, and headers
     * @param body      the body of the request
     * @param execution the request execution
     * @return
     * @throws IOException
     */
    @Override
    public ClientHttpResponse intercept(
            HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {

        HttpHeaders headers = request.getHeaders();
        // 是否存在 Authorization 头信息
        if (!headers.containsKey(HttpHeaders.AUTHORIZATION)) {
            // 设置头信息 Authorization base64 加密
            headers.setBasicAuth(this.username, this.password, this.charset);
        }
        return execution.execute(request, body);
    }

}
