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

package org.springframework.web.method.annotation;

import java.beans.ConstructorProperties;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.Validator;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.bind.support.WebRequestDataBinder;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Resolve {@code @ModelAttribute} annotated method arguments and handle
 * return values from {@code @ModelAttribute} annotated methods.
 *
 * <p>Model attributes are obtained from the model or created with a default
 * constructor (and then added to the model). Once created the attribute is
 * populated via data binding to Servlet request parameters. Validation may be
 * applied if the argument is annotated with {@code @javax.validation.Valid}.
 * or Spring's own {@code @org.springframework.validation.annotation.Validated}.
 *
 * <p>When this handler is created with {@code annotationNotRequired=true}
 * any non-simple type argument and return value is regarded as a model
 * attribute with or without the presence of an {@code @ModelAttribute}.
 *
 * @author Rossen Stoyanchev
 * @author Juergen Hoeller
 * @author Sebastien Deleuze
 * @since 3.1
 */
public class ModelAttributeMethodProcessor implements HandlerMethodArgumentResolver, HandlerMethodReturnValueHandler {

	private static final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

	protected final Log logger = LogFactory.getLog(getClass());

	private final boolean annotationNotRequired;


	/**
	 * Class constructor.
	 * @param annotationNotRequired if "true", non-simple method arguments and
	 * return values are considered model attributes with or without a
	 * {@code @ModelAttribute} annotation
	 */
	public ModelAttributeMethodProcessor(boolean annotationNotRequired) {
		this.annotationNotRequired = annotationNotRequired;
	}


	/**
	 * Returns {@code true} if the parameter is annotated with
	 * {@link ModelAttribute} or, if in default resolution mode, for any
	 * method parameter that is not a simple type.
	 * 判断是否是受支持的处理
	 */
	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		// 1. 参数具备ModelAttribute注解
		// 2. 是否不属于基本类型并且 annotationNotRequired 为true
		return (parameter.hasParameterAnnotation(ModelAttribute.class) ||
				(this.annotationNotRequired && !BeanUtils.isSimpleProperty(parameter.getParameterType())));
	}

	/**
	 * Resolve the argument from the model or if not found instantiate it with
	 * its default if it is available. The model attribute is then populated
	 * with request values via data binding and optionally validated
	 * if {@code @java.validation.Valid} is present on the argument.
	 * @throws BindException if data binding and validation result in an error
	 * and the next method parameter is not of type {@link Errors}
	 * @throws Exception if WebDataBinder initialization fails
	 */
	@Override
	@Nullable
	public final Object resolveArgument(MethodParameter parameter, @Nullable ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, @Nullable WebDataBinderFactory binderFactory) throws Exception {

		Assert.state(mavContainer != null, "ModelAttributeMethodProcessor requires ModelAndViewContainer");
		Assert.state(binderFactory != null, "ModelAttributeMethodProcessor requires WebDataBinderFactory");

		// 获取注解的value数据
		String name = ModelFactory.getNameForParameter(parameter);
		// 提取注解
		ModelAttribute ann = parameter.getParameterAnnotation(ModelAttribute.class);
		// 注解不为空进行数据绑定操作
		if (ann != null) {
			mavContainer.setBinding(name, ann.binding());
		}

		Object attribute = null;
		BindingResult bindingResult = null;

		// 数据持有器中是否存有, 如果存有将name对应的数据值提取
		if (mavContainer.containsAttribute(name)) {
			attribute = mavContainer.getModel().get(name);
		}
		// 如果不存在
		else {
			// Create attribute instance
			try {
				// 创建对象
				attribute = createAttribute(name, parameter, binderFactory, webRequest);
			}
			catch (BindException ex) {
				if (isBindExceptionRequired(parameter)) {
					// No BindingResult parameter -> fail with BindException
					throw ex;
				}
				// Otherwise, expose null/empty value and associated BindingResult
				if (parameter.getParameterType() == Optional.class) {
					attribute = Optional.empty();
				}
				bindingResult = ex.getBindingResult();
			}
		}

		// 绑定结果为空进行处理
		if (bindingResult == null) {
			// Bean property binding and validation;
			// skipped in case of binding failure on construction.
			// 创建Web数据绑定器
			WebDataBinder binder = binderFactory.createBinder(webRequest, attribute, name);
			if (binder.getTarget() != null) {
				// 绑定request请求数据
				if (!mavContainer.isBindingDisabled(name)) {
					bindRequestParameters(binder, webRequest);
				}
				// 进行验证
				validateIfApplicable(binder, parameter);
				// binder中如果出现异常将抛出
				if (binder.getBindingResult().hasErrors() && isBindExceptionRequired(binder, parameter)) {
					throw new BindException(binder.getBindingResult());
				}
			}
			// Value type adaptation, also covering java.util.Optional
			// 类型转换
			if (!parameter.getParameterType().isInstance(attribute)) {
				attribute = binder.convertIfNecessary(binder.getTarget(), parameter.getParameterType(), parameter);
			}
			bindingResult = binder.getBindingResult();
		}

		// Add resolved attribute and BindingResult at the end of the model
		// 移除历史数据并添加新的数据
		Map<String, Object> bindingResultModel = bindingResult.getModel();
		mavContainer.removeAttributes(bindingResultModel);
		mavContainer.addAllAttributes(bindingResultModel);

		return attribute;
	}

	/**
	 * Extension point to create the model attribute if not found in the model,
	 * with subsequent parameter binding through bean properties (unless suppressed).
	 * <p>The default implementation typically uses the unique public no-arg constructor
	 * if available but also handles a "primary constructor" approach for data classes:
	 * It understands the JavaBeans {@link ConstructorProperties} annotation as well as
	 * runtime-retained parameter names in the bytecode, associating request parameters
	 * with constructor arguments by name. If no such constructor is found, the default
	 * constructor will be used (even if not public), assuming subsequent bean property
	 * bindings through setter methods.
	 * @param attributeName the name of the attribute (never {@code null})
	 * @param parameter the method parameter declaration
	 * @param binderFactory for creating WebDataBinder instance
	 * @param webRequest the current request
	 * @return the created model attribute (never {@code null})
	 * @throws BindException in case of constructor argument binding failure
	 * @throws Exception in case of constructor invocation failure
	 * @see #constructAttribute(Constructor, String, MethodParameter, WebDataBinderFactory, NativeWebRequest)
	 * @see BeanUtils#findPrimaryConstructor(Class)
	 */
	protected Object createAttribute(String attributeName, MethodParameter parameter,
			WebDataBinderFactory binderFactory, NativeWebRequest webRequest) throws Exception {

		// 提取方法参数
		MethodParameter nestedParameter = parameter.nestedIfOptional();
		// 提取参数类型
		Class<?> clazz = nestedParameter.getNestedParameterType();

		// 提取构造函数
		Constructor<?> ctor = BeanUtils.findPrimaryConstructor(clazz);
		// 构造函数为空的情况下再次进行提取
		if (ctor == null) {
			Constructor<?>[] ctors = clazz.getConstructors();
			if (ctors.length == 1) {
				ctor = ctors[0];
			}
			else {
				try {
					ctor = clazz.getDeclaredConstructor();
				}
				catch (NoSuchMethodException ex) {
					throw new IllegalStateException("No primary or default constructor found for " + clazz, ex);
				}
			}
		}

		// 创建对象并进行属性设置
		Object attribute = constructAttribute(ctor, attributeName, parameter, binderFactory, webRequest);
		if (parameter != nestedParameter) {
			attribute = Optional.of(attribute);
		}
		return attribute;
	}

	/**
	 * Construct a new attribute instance with the given constructor.
	 * <p>Called from
	 * {@link #createAttribute(String, MethodParameter, WebDataBinderFactory, NativeWebRequest)}
	 * after constructor resolution.
	 * @param ctor the constructor to use
	 * @param attributeName the name of the attribute (never {@code null})
	 * @param binderFactory for creating WebDataBinder instance
	 * @param webRequest the current request
	 * @return the created model attribute (never {@code null})
	 * @throws BindException in case of constructor argument binding failure
	 * @throws Exception in case of constructor invocation failure
	 * @since 5.1
	 */
	@SuppressWarnings("deprecation")
	protected Object constructAttribute(Constructor<?> ctor, String attributeName, MethodParameter parameter,
			WebDataBinderFactory binderFactory, NativeWebRequest webRequest) throws Exception {

		// 目前是空方法
		Object constructed = constructAttribute(ctor, attributeName, binderFactory, webRequest);
		if (constructed != null) {
			return constructed;
		}

		// 构造函数的参数列表为空
		if (ctor.getParameterCount() == 0) {
			// A single default constructor -> clearly a standard JavaBeans arrangement.
			return BeanUtils.instantiateClass(ctor);
		}

		// A single data class constructor -> resolve constructor arguments from request parameters.

		ConstructorProperties cp = ctor.getAnnotation(ConstructorProperties.class);
		String[] paramNames = (cp != null ? cp.value() : parameterNameDiscoverer.getParameterNames(ctor));
		Assert.state(paramNames != null, () -> "Cannot resolve parameter names for constructor " + ctor);
		Class<?>[] paramTypes = ctor.getParameterTypes();
		Assert.state(paramNames.length == paramTypes.length,
				() -> "Invalid number of parameter names: " + paramNames.length + " for constructor " + ctor);

		Object[] args = new Object[paramTypes.length];
		WebDataBinder binder = binderFactory.createBinder(webRequest, null, attributeName);
		String fieldDefaultPrefix = binder.getFieldDefaultPrefix();
		String fieldMarkerPrefix = binder.getFieldMarkerPrefix();
		boolean bindingFailure = false;
		Set<String> failedParams = new HashSet<>(4);

		// 循环参数名称进行参数值确认,最终数据会放入到args中,在最后通过构造函数+参数列表进行创建
		for (int i = 0; i < paramNames.length; i++) {
			String paramName = paramNames[i];
			Class<?> paramType = paramTypes[i];
			// 从请求中提取参数名称对应的数据
			Object value = webRequest.getParameterValues(paramName);
			if (value == null) {
				if (fieldDefaultPrefix != null) {
					// 尝试前缀获取
					value = webRequest.getParameter(fieldDefaultPrefix + paramName);
				}
				if (value == null && fieldMarkerPrefix != null) {
					// 尝试标记位前缀获取
					if (webRequest.getParameter(fieldMarkerPrefix + paramName) != null) {
						value = binder.getEmptyValue(paramType);
					}
				}
			}
			try {
				// 创建方法参数对象
				MethodParameter methodParam = new FieldAwareConstructorParameter(ctor, i, paramName);
				// 进行方法参数是否是Optional类型的处理
				if (value == null && methodParam.isOptional()) {
					args[i] = (methodParam.getParameterType() == Optional.class ? Optional.empty() : null);
				}
				// 做一次类型转换
				else {
					args[i] = binder.convertIfNecessary(value, paramType, methodParam);
				}
			}
			// 异常:类型不匹配
			catch (TypeMismatchException ex) {
				ex.initPropertyName(paramName);
				args[i] = value;
				failedParams.add(paramName);
				binder.getBindingResult().recordFieldValue(paramName, paramType, value);
				binder.getBindingErrorProcessor().processPropertyAccessException(ex, binder.getBindingResult());
				bindingFailure = true;
			}
		}

		// 是否绑定失败
		if (bindingFailure) {
			// 提取绑定结果
			BindingResult result = binder.getBindingResult();
			for (int i = 0; i < paramNames.length; i++) {
				String paramName = paramNames[i];
				if (!failedParams.contains(paramName)) {
					Object value = args[i];
					result.recordFieldValue(paramName, paramTypes[i], value);
					// 验证
					validateValueIfApplicable(binder, parameter, ctor.getDeclaringClass(), paramName, value);
				}
			}
			throw new BindException(result);
		}

		return BeanUtils.instantiateClass(ctor, args);
	}

	/**
	 * Construct a new attribute instance with the given constructor.
	 * @since 5.0
	 * @deprecated as of 5.1, in favor of
	 * {@link #constructAttribute(Constructor, String, MethodParameter, WebDataBinderFactory, NativeWebRequest)}
	 */
	@Deprecated
	@Nullable
	protected Object constructAttribute(Constructor<?> ctor, String attributeName,
			WebDataBinderFactory binderFactory, NativeWebRequest webRequest) throws Exception {

		return null;
	}

	/**
	 * Extension point to bind the request to the target object.
	 * @param binder the data binder instance to use for the binding
	 * @param request the current request
	 */
	protected void bindRequestParameters(WebDataBinder binder, NativeWebRequest request) {
		((WebRequestDataBinder) binder).bind(request);
	}

	/**
	 * Validate the model attribute if applicable.
	 * <p>The default implementation checks for {@code @javax.validation.Valid},
	 * Spring's {@link org.springframework.validation.annotation.Validated},
	 * and custom annotations whose name starts with "Valid".
	 * @param binder the DataBinder to be used
	 * @param parameter the method parameter declaration
	 * @see WebDataBinder#validate(Object...)
	 * @see SmartValidator#validate(Object, Errors, Object...)
	 */
	protected void validateIfApplicable(WebDataBinder binder, MethodParameter parameter) {
		for (Annotation ann : parameter.getParameterAnnotations()) {
			Object[] validationHints = determineValidationHints(ann);
			if (validationHints != null) {
				binder.validate(validationHints);
				break;
			}
		}
	}

	/**
	 * Validate the specified candidate value if applicable.
	 * <p>The default implementation checks for {@code @javax.validation.Valid},
	 * Spring's {@link org.springframework.validation.annotation.Validated},
	 * and custom annotations whose name starts with "Valid".
	 * @param binder the DataBinder to be used
	 * @param parameter the method parameter declaration
	 * @param targetType the target type
	 * @param fieldName the name of the field
	 * @param value the candidate value
	 * @since 5.1
	 * @see #validateIfApplicable(WebDataBinder, MethodParameter)
	 * @see SmartValidator#validateValue(Class, String, Object, Errors, Object...)
	 */
	protected void validateValueIfApplicable(WebDataBinder binder, MethodParameter parameter,
			Class<?> targetType, String fieldName, @Nullable Object value) {

		for (Annotation ann : parameter.getParameterAnnotations()) {
			Object[] validationHints = determineValidationHints(ann);
			if (validationHints != null) {
				for (Validator validator : binder.getValidators()) {
					if (validator instanceof SmartValidator) {
						try {
							((SmartValidator) validator).validateValue(targetType, fieldName, value,
									binder.getBindingResult(), validationHints);
						}
						catch (IllegalArgumentException ex) {
							// No corresponding field on the target class...
						}
					}
				}
				break;
			}
		}
	}

	/**
	 * Determine any validation triggered by the given annotation.
	 * @param ann the annotation (potentially a validation annotation)
	 * @return the validation hints to apply (possibly an empty array),
	 * or {@code null} if this annotation does not trigger any validation
	 * @since 5.1
	 */
	@Nullable
	private Object[] determineValidationHints(Annotation ann) {
		Validated validatedAnn = AnnotationUtils.getAnnotation(ann, Validated.class);
		if (validatedAnn != null || ann.annotationType().getSimpleName().startsWith("Valid")) {
			Object hints = (validatedAnn != null ? validatedAnn.value() : AnnotationUtils.getValue(ann));
			if (hints == null) {
				return new Object[0];
			}
			return (hints instanceof Object[] ? (Object[]) hints : new Object[] {hints});
		}
		return null;
	}

	/**
	 * Whether to raise a fatal bind exception on validation errors.
	 * <p>The default implementation delegates to {@link #isBindExceptionRequired(MethodParameter)}.
	 * @param binder the data binder used to perform data binding
	 * @param parameter the method parameter declaration
	 * @return {@code true} if the next method parameter is not of type {@link Errors}
	 * @see #isBindExceptionRequired(MethodParameter)
	 */
	protected boolean isBindExceptionRequired(WebDataBinder binder, MethodParameter parameter) {
		return isBindExceptionRequired(parameter);
	}

	/**
	 * Whether to raise a fatal bind exception on validation errors.
	 * @param parameter the method parameter declaration
	 * @return {@code true} if the next method parameter is not of type {@link Errors}
	 * @since 5.0
	 */
	protected boolean isBindExceptionRequired(MethodParameter parameter) {
		int i = parameter.getParameterIndex();
		Class<?>[] paramTypes = parameter.getExecutable().getParameterTypes();
		boolean hasBindingResult = (paramTypes.length > (i + 1) && Errors.class.isAssignableFrom(paramTypes[i + 1]));
		return !hasBindingResult;
	}

	/**
	 * Return {@code true} if there is a method-level {@code @ModelAttribute}
	 * or, in default resolution mode, for any return value type that is not
	 * a simple type.
	 */
	@Override
	public boolean supportsReturnType(MethodParameter returnType) {
		return (returnType.hasMethodAnnotation(ModelAttribute.class) ||
				(this.annotationNotRequired && !BeanUtils.isSimpleProperty(returnType.getParameterType())));
	}

	/**
	 * Add non-null return values to the {@link ModelAndViewContainer}.
	 */
	@Override
	public void handleReturnValue(@Nullable Object returnValue, MethodParameter returnType,
			ModelAndViewContainer mavContainer, NativeWebRequest webRequest) throws Exception {

		if (returnValue != null) {
			String name = ModelFactory.getNameForReturnValue(returnValue, returnType);
			mavContainer.addAttribute(name, returnValue);
		}
	}


	/**
	 * {@link MethodParameter} subclass which detects field annotations as well.
	 * @since 5.1
	 */
	private static class FieldAwareConstructorParameter extends MethodParameter {

		private final String parameterName;

		@Nullable
		private volatile Annotation[] combinedAnnotations;

		public FieldAwareConstructorParameter(Constructor<?> constructor, int parameterIndex, String parameterName) {
			super(constructor, parameterIndex);
			this.parameterName = parameterName;
		}

		@Override
		public Annotation[] getParameterAnnotations() {
			Annotation[] anns = this.combinedAnnotations;
			if (anns == null) {
				anns = super.getParameterAnnotations();
				try {
					Field field = getDeclaringClass().getDeclaredField(this.parameterName);
					Annotation[] fieldAnns = field.getAnnotations();
					if (fieldAnns.length > 0) {
						List<Annotation> merged = new ArrayList<>(anns.length + fieldAnns.length);
						merged.addAll(Arrays.asList(anns));
						for (Annotation fieldAnn : fieldAnns) {
							boolean existingType = false;
							for (Annotation ann : anns) {
								if (ann.annotationType() == fieldAnn.annotationType()) {
									existingType = true;
									break;
								}
							}
							if (!existingType) {
								merged.add(fieldAnn);
							}
						}
						anns = merged.toArray(new Annotation[0]);
					}
				}
				catch (NoSuchFieldException | SecurityException ex) {
					// ignore
				}
				this.combinedAnnotations = anns;
			}
			return anns;
		}

		@Override
		public String getParameterName() {
			return this.parameterName;
		}
	}

}
