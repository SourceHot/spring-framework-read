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

package org.springframework.web.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.ResolvableType;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.GenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Response extractor that uses the given {@linkplain HttpMessageConverter entity converters}
 * to convert the response into a type {@code T}.
 *
 * @param <T> the data type
 * @author Arjen Poutsma
 * @see RestTemplate
 * @since 3.0
 */
public class HttpMessageConverterExtractor<T> implements ResponseExtractor<T> {

    private final Type responseType;

    /**
     * 结果类
     */
    @Nullable
    private final Class<T> responseClass;

    /**
     * 消息转换器
     */
    private final List<HttpMessageConverter<?>> messageConverters;

    /**
     * 日志
     */
    private final Log logger;


    /**
     * Create a new instance of the {@code HttpMessageConverterExtractor} with the given response
     * type and message converters. The given converters must support the response type.
     */
    public HttpMessageConverterExtractor(Class<T> responseType, List<HttpMessageConverter<?>> messageConverters) {
        this((Type) responseType, messageConverters);
    }

    /**
     * Creates a new instance of the {@code HttpMessageConverterExtractor} with the given response
     * type and message converters. The given converters must support the response type.
     */
    public HttpMessageConverterExtractor(Type responseType, List<HttpMessageConverter<?>> messageConverters) {
        this(responseType, messageConverters, LogFactory.getLog(HttpMessageConverterExtractor.class));
    }

    @SuppressWarnings("unchecked")
    HttpMessageConverterExtractor(Type responseType, List<HttpMessageConverter<?>> messageConverters, Log logger) {
        Assert.notNull(responseType, "'responseType' must not be null");
        Assert.notEmpty(messageConverters, "'messageConverters' must not be empty");
        this.responseType = responseType;
        this.responseClass = (responseType instanceof Class ? (Class<T>) responseType : null);
        this.messageConverters = messageConverters;
        this.logger = logger;
    }


    /**
     * 提取结果
     *
     * @param response the HTTP response
     * @return
     * @throws IOException
     */
    @Override
    @SuppressWarnings({"unchecked", "rawtypes", "resource"})
    public T extractData(ClientHttpResponse response) throws IOException {
        // 装个对象
        MessageBodyClientHttpResponseWrapper responseWrapper = new MessageBodyClientHttpResponseWrapper(response);
        // 判断是否由body
        if (!responseWrapper.hasMessageBody() || responseWrapper.hasEmptyMessageBody()) {
            return null;
        }
        // 媒体类型
        MediaType contentType = getContentType(responseWrapper);

        try {
            // 玄幻选择不同的消息转换器
            for (HttpMessageConverter<?> messageConverter : this.messageConverters) {
                if (messageConverter instanceof GenericHttpMessageConverter) {
                    GenericHttpMessageConverter<?> genericMessageConverter =
                            (GenericHttpMessageConverter<?>) messageConverter;
                    if (genericMessageConverter.canRead(this.responseType, null, contentType)) {
                        if (logger.isDebugEnabled()) {
                            ResolvableType resolvableType = ResolvableType.forType(this.responseType);
                            logger.debug("Reading to [" + resolvableType + "]");
                        }
                        return (T) genericMessageConverter.read(this.responseType, null, responseWrapper);
                    }
                }
                if (this.responseClass != null) {
                    if (messageConverter.canRead(this.responseClass, contentType)) {
                        if (logger.isDebugEnabled()) {
                            String className = this.responseClass.getName();
                            logger.debug("Reading to [" + className + "] as \"" + contentType + "\"");
                        }
                        return (T) messageConverter.read((Class) this.responseClass, responseWrapper);
                    }
                }
            }
        } catch (IOException | HttpMessageNotReadableException ex) {
            throw new RestClientException("Error while extracting response for type [" +
                    this.responseType + "] and content type [" + contentType + "]", ex);
        }

        throw new RestClientException("Could not extract response: no suitable HttpMessageConverter found " +
                "for response type [" + this.responseType + "] and content type [" + contentType + "]");
    }

    /**
     * Determine the Content-Type of the response based on the "Content-Type"
     * header or otherwise default to {@link MediaType#APPLICATION_OCTET_STREAM}.
     *
     * @param response the response
     * @return the MediaType, possibly {@code null}.
     */
    @Nullable
    protected MediaType getContentType(ClientHttpResponse response) {
        MediaType contentType = response.getHeaders().getContentType();
        if (contentType == null) {
            if (logger.isTraceEnabled()) {
                logger.trace("No content-type, using 'application/octet-stream'");
            }
            contentType = MediaType.APPLICATION_OCTET_STREAM;
        }
        return contentType;
    }

}
