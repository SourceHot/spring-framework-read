/*
 * Copyright 2002-2012 the original author or authors.
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

package org.springframework.remoting.httpinvoker;

import org.springframework.lang.Nullable;

/**
 * Configuration interface for executing HTTP invoker requests.
 *
 * @author Juergen Hoeller
 * @see HttpInvokerRequestExecutor
 * @see HttpInvokerClientInterceptor
 * @since 1.1
 */
public interface HttpInvokerClientConfiguration {

    /**
     * Return the HTTP URL of the target service.
     * 获取服务的url
     */
    String getServiceUrl();

    /**
     * Return the codebase URL to download classes from if not found locally.
     * Can consist of multiple URLs, separated by spaces.
     *
     * @return the codebase URL, or {@code null} if none
     * @see java.rmi.server.RMIClassLoader
     */
    @Nullable
    String getCodebaseUrl();

}
