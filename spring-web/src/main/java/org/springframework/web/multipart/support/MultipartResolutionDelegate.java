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

package org.springframework.web.multipart.support;

import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.lang.Nullable;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartRequest;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A common delegate for {@code HandlerMethodArgumentResolver} implementations
 * which need to resolve {@link MultipartFile} and {@link Part} arguments.
 *
 * @author Juergen Hoeller
 * @since 4.3
 */
public abstract class MultipartResolutionDelegate {

    /**
     * Indicates an unresolvable value.
     */
    public static final Object UNRESOLVABLE = new Object();


    @Nullable
    public static MultipartRequest resolveMultipartRequest(NativeWebRequest webRequest) {
        MultipartRequest multipartRequest = webRequest.getNativeRequest(MultipartRequest.class);
        if (multipartRequest != null) {
            return multipartRequest;
        }
        HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
        if (servletRequest != null && isMultipartContent(servletRequest)) {
            return new StandardMultipartHttpServletRequest(servletRequest);
        }
        return null;
    }

    public static boolean isMultipartRequest(HttpServletRequest request) {
        return (WebUtils.getNativeRequest(request, MultipartHttpServletRequest.class) != null ||
                isMultipartContent(request));
    }

    private static boolean isMultipartContent(HttpServletRequest request) {
        String contentType = request.getContentType();
        return (contentType != null && contentType.toLowerCase().startsWith("multipart/"));
    }

    static MultipartHttpServletRequest asMultipartHttpServletRequest(HttpServletRequest request) {
        MultipartHttpServletRequest unwrapped = WebUtils.getNativeRequest(request, MultipartHttpServletRequest.class);
        if (unwrapped != null) {
            return unwrapped;
        }
        return new StandardMultipartHttpServletRequest(request);
    }


    public static boolean isMultipartArgument(MethodParameter parameter) {
        Class<?> paramType = parameter.getNestedParameterType();
        return (MultipartFile.class == paramType ||
                isMultipartFileCollection(parameter) || isMultipartFileArray(parameter) ||
                (Part.class == paramType || isPartCollection(parameter) || isPartArray(parameter)));
    }

    @Nullable
    public static Object resolveMultipartArgument(String name, MethodParameter parameter, HttpServletRequest request)
            throws Exception {

        MultipartHttpServletRequest multipartRequest =
                WebUtils.getNativeRequest(request, MultipartHttpServletRequest.class);
        boolean isMultipart = (multipartRequest != null || isMultipartContent(request));

        if (MultipartFile.class == parameter.getNestedParameterType()) {
            if (multipartRequest == null && isMultipart) {
                multipartRequest = new StandardMultipartHttpServletRequest(request);
            }
            return (multipartRequest != null ? multipartRequest.getFile(name) : null);
        } else if (isMultipartFileCollection(parameter)) {
            if (multipartRequest == null && isMultipart) {
                multipartRequest = new StandardMultipartHttpServletRequest(request);
            }
            return (multipartRequest != null ? multipartRequest.getFiles(name) : null);
        } else if (isMultipartFileArray(parameter)) {
            if (multipartRequest == null && isMultipart) {
                multipartRequest = new StandardMultipartHttpServletRequest(request);
            }
            if (multipartRequest != null) {
                List<MultipartFile> multipartFiles = multipartRequest.getFiles(name);
                return multipartFiles.toArray(new MultipartFile[0]);
            } else {
                return null;
            }
        } else if (Part.class == parameter.getNestedParameterType()) {
            return (isMultipart ? request.getPart(name) : null);
        } else if (isPartCollection(parameter)) {
            return (isMultipart ? resolvePartList(request, name) : null);
        } else if (isPartArray(parameter)) {
            return (isMultipart ? resolvePartList(request, name).toArray(new Part[0]) : null);
        } else {
            return UNRESOLVABLE;
        }
    }

    private static boolean isMultipartFileCollection(MethodParameter methodParam) {
        return (MultipartFile.class == getCollectionParameterType(methodParam));
    }

    private static boolean isMultipartFileArray(MethodParameter methodParam) {
        return (MultipartFile.class == methodParam.getNestedParameterType().getComponentType());
    }

    private static boolean isPartCollection(MethodParameter methodParam) {
        return (Part.class == getCollectionParameterType(methodParam));
    }

    private static boolean isPartArray(MethodParameter methodParam) {
        return (Part.class == methodParam.getNestedParameterType().getComponentType());
    }

    @Nullable
    private static Class<?> getCollectionParameterType(MethodParameter methodParam) {
        Class<?> paramType = methodParam.getNestedParameterType();
        if (Collection.class == paramType || List.class.isAssignableFrom(paramType)) {
            Class<?> valueType = ResolvableType.forMethodParameter(methodParam).asCollection().resolveGeneric();
            if (valueType != null) {
                return valueType;
            }
        }
        return null;
    }

    private static List<Part> resolvePartList(HttpServletRequest request, String name) throws Exception {
        Collection<Part> parts = request.getParts();
        List<Part> result = new ArrayList<>(parts.size());
        for (Part part : parts) {
            if (part.getName().equals(name)) {
                result.add(part);
            }
        }
        return result;
    }

}
