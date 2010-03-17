/*
 * Copyright 2002-2008 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.springframework.web.filter;

import java.io.IOException;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.util.FlashMap;

/**
 * The filter needed to be activated to support the {@link FlashMap}, writing parameters within the flash map to the
 * current request and flushing out the map afterwards.
 * 
 * @author Micha Kiener
 * @since 3.1
 */
public class FlashMapFilter extends OncePerRequestFilter {

	/**
	 * @see org.springframework.web.filter.OncePerRequestFilter#doFilterInternal(javax.servlet.http.HttpServletRequest,
	 * javax.servlet.http.HttpServletResponse, javax.servlet.FilterChain)
	 */
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		Map<String, Object> flashMap = FlashMap.getFlashMap(false);
		if (flashMap != null) {
			for (Map.Entry<String, ?> entry : flashMap.entrySet()) {
				Object currentValue = request.getAttribute(entry.getKey());
				if (currentValue == null) {
					request.setAttribute(entry.getKey(), entry.getValue());
				}
			}
			FlashMap.removeFlashMap();
		}

		filterChain.doFilter(request, response);
	}

}
