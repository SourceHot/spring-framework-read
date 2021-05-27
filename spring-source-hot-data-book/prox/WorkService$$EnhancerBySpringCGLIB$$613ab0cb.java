package com.github.source.hot.data.tx;

import com.github.source.hot.data.tx.WorkService;

import java.lang.reflect.Method;

import org.aopalliance.aop.Advice;

import org.springframework.aop.Advisor;
import org.springframework.aop.SpringProxy;
import org.springframework.aop.TargetClassAware;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.AopConfigException;
import org.springframework.cglib.core.ReflectUtils;
import org.springframework.cglib.core.Signature;
import org.springframework.cglib.proxy.Callback;
import org.springframework.cglib.proxy.Dispatcher;
import org.springframework.cglib.proxy.Factory;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.cglib.proxy.NoOp;
import org.springframework.jdbc.core.JdbcTemplate;

public class WorkService$$EnhancerBySpringCGLIB$$613ab0cb
		extends WorkService
		implements SpringProxy,
		Advised,
		Factory {
	private static final ThreadLocal CGLIB$THREAD_CALLBACKS;

	private static final Callback[] CGLIB$STATIC_CALLBACKS;

	private static final Method CGLIB$setJdbcTemplate$0$Method;

	private static final MethodProxy CGLIB$setJdbcTemplate$0$Proxy;

	private static final Object[] CGLIB$emptyArgs;

	private static final Method CGLIB$getJdbcTemplate$1$Method;

	private static final MethodProxy CGLIB$getJdbcTemplate$1$Proxy;

	private static final Method CGLIB$work$2$Method;

	private static final MethodProxy CGLIB$work$2$Proxy;

	private static final Method CGLIB$equals$3$Method;

	private static final MethodProxy CGLIB$equals$3$Proxy;

	private static final Method CGLIB$toString$4$Method;

	private static final MethodProxy CGLIB$toString$4$Proxy;

	private static final Method CGLIB$hashCode$5$Method;

	private static final MethodProxy CGLIB$hashCode$5$Proxy;

	private static final Method CGLIB$clone$6$Method;

	private static final MethodProxy CGLIB$clone$6$Proxy;

	public static Object CGLIB$FACTORY_DATA;

	private static Object CGLIB$CALLBACK_FILTER;

	private boolean CGLIB$BOUND;

	private MethodInterceptor CGLIB$CALLBACK_0;

	private MethodInterceptor CGLIB$CALLBACK_1;

	private NoOp CGLIB$CALLBACK_2;

	private Dispatcher CGLIB$CALLBACK_3;

	private Dispatcher CGLIB$CALLBACK_4;

	private MethodInterceptor CGLIB$CALLBACK_5;

	private MethodInterceptor CGLIB$CALLBACK_6;

	public WorkService$$EnhancerBySpringCGLIB$$613ab0cb() {
		WorkService$$EnhancerBySpringCGLIB$$613ab0cb workService$$EnhancerBySpringCGLIB$$613ab0cb = this;
		WorkService$$EnhancerBySpringCGLIB$$613ab0cb.CGLIB$BIND_CALLBACKS(workService$$EnhancerBySpringCGLIB$$613ab0cb);
	}

	public static void CGLIB$SET_THREAD_CALLBACKS(Callback[] callbackArray) {
		CGLIB$THREAD_CALLBACKS.set(callbackArray);
	}

	public static void CGLIB$SET_STATIC_CALLBACKS(Callback[] callbackArray) {
		CGLIB$STATIC_CALLBACKS = callbackArray;
	}

	public static MethodProxy CGLIB$findMethodProxy(Signature signature) {
		String string = ((Object) signature).toString();
		switch (string.hashCode()) {
			case -1245458157: {
				if (!string.equals("setJdbcTemplate(Lorg/springframework/jdbc/core/JdbcTemplate;)V")) break;
				return CGLIB$setJdbcTemplate$0$Proxy;
			}
			case -508378822: {
				if (!string.equals("clone()Ljava/lang/Object;")) break;
				return CGLIB$clone$6$Proxy;
			}
			case 145057057: {
				if (!string.equals("getJdbcTemplate()Lorg/springframework/jdbc/core/JdbcTemplate;")) break;
				return CGLIB$getJdbcTemplate$1$Proxy;
			}
			case 1525100228: {
				if (!string.equals("work()V")) break;
				return CGLIB$work$2$Proxy;
			}
			case 1826985398: {
				if (!string.equals("equals(Ljava/lang/Object;)Z")) break;
				return CGLIB$equals$3$Proxy;
			}
			case 1913648695: {
				if (!string.equals("toString()Ljava/lang/String;")) break;
				return CGLIB$toString$4$Proxy;
			}
			case 1984935277: {
				if (!string.equals("hashCode()I")) break;
				return CGLIB$hashCode$5$Proxy;
			}
		}
		return null;
	}

	private static final void CGLIB$BIND_CALLBACKS(Object object) {
		block2:
		{
			Object object2;
			WorkService$$EnhancerBySpringCGLIB$$613ab0cb workService$$EnhancerBySpringCGLIB$$613ab0cb;
			block3:
			{
				workService$$EnhancerBySpringCGLIB$$613ab0cb = (WorkService$$EnhancerBySpringCGLIB$$613ab0cb) object;
				if (workService$$EnhancerBySpringCGLIB$$613ab0cb.CGLIB$BOUND) break block2;
				workService$$EnhancerBySpringCGLIB$$613ab0cb.CGLIB$BOUND = true;
				object2 = CGLIB$THREAD_CALLBACKS.get();
				if (object2 != null) break block3;
				object2 = CGLIB$STATIC_CALLBACKS;
				if (CGLIB$STATIC_CALLBACKS == null) break block2;
			}
			Callback[] callbackArray = (Callback[]) object2;
			WorkService$$EnhancerBySpringCGLIB$$613ab0cb workService$$EnhancerBySpringCGLIB$$613ab0cb2 = workService$$EnhancerBySpringCGLIB$$613ab0cb;
			workService$$EnhancerBySpringCGLIB$$613ab0cb2.CGLIB$CALLBACK_6 = (MethodInterceptor) callbackArray[6];
			workService$$EnhancerBySpringCGLIB$$613ab0cb2.CGLIB$CALLBACK_5 = (MethodInterceptor) callbackArray[5];
			workService$$EnhancerBySpringCGLIB$$613ab0cb2.CGLIB$CALLBACK_4 = (Dispatcher) callbackArray[4];
			workService$$EnhancerBySpringCGLIB$$613ab0cb2.CGLIB$CALLBACK_3 = (Dispatcher) callbackArray[3];
			workService$$EnhancerBySpringCGLIB$$613ab0cb2.CGLIB$CALLBACK_2 = (NoOp) callbackArray[2];
			workService$$EnhancerBySpringCGLIB$$613ab0cb2.CGLIB$CALLBACK_1 = (MethodInterceptor) callbackArray[1];
			workService$$EnhancerBySpringCGLIB$$613ab0cb2.CGLIB$CALLBACK_0 = (MethodInterceptor) callbackArray[0];
		}
	}

	static void CGLIB$STATICHOOK3() {
		CGLIB$THREAD_CALLBACKS = new ThreadLocal();
		CGLIB$emptyArgs = new Object[0];
		Class<?> clazz = Class.forName("com.github.source.hot.data.tx.WorkService$$EnhancerBySpringCGLIB$$613ab0cb");
		Class<?> clazz2 = Class.forName("com.github.source.hot.data.tx.WorkService");
		Method[] methodArray = ReflectUtils.findMethods(new String[] {"setJdbcTemplate", "(Lorg/springframework/jdbc/core/JdbcTemplate;)V", "getJdbcTemplate", "()Lorg/springframework/jdbc/core/JdbcTemplate;", "work", "()V"}, clazz2.getDeclaredMethods());
		CGLIB$setJdbcTemplate$0$Method = methodArray[0];
		CGLIB$setJdbcTemplate$0$Proxy = MethodProxy.create(clazz2, clazz, "(Lorg/springframework/jdbc/core/JdbcTemplate;)V", "setJdbcTemplate", "CGLIB$setJdbcTemplate$0");
		CGLIB$getJdbcTemplate$1$Method = methodArray[1];
		CGLIB$getJdbcTemplate$1$Proxy = MethodProxy.create(clazz2, clazz, "()Lorg/springframework/jdbc/core/JdbcTemplate;", "getJdbcTemplate", "CGLIB$getJdbcTemplate$1");
		CGLIB$work$2$Method = methodArray[2];
		CGLIB$work$2$Proxy = MethodProxy.create(clazz2, clazz, "()V", "work", "CGLIB$work$2");
		clazz2 = Class.forName("java.lang.Object");
		Method[] methodArray2 = ReflectUtils.findMethods(new String[] {"equals", "(Ljava/lang/Object;)Z", "toString", "()Ljava/lang/String;", "hashCode", "()I", "clone", "()Ljava/lang/Object;"}, clazz2.getDeclaredMethods());
		CGLIB$equals$3$Method = methodArray2[0];
		CGLIB$equals$3$Proxy = MethodProxy.create(clazz2, clazz, "(Ljava/lang/Object;)Z", "equals", "CGLIB$equals$3");
		CGLIB$toString$4$Method = methodArray2[1];
		CGLIB$toString$4$Proxy = MethodProxy.create(clazz2, clazz, "()Ljava/lang/String;", "toString", "CGLIB$toString$4");
		CGLIB$hashCode$5$Method = methodArray2[2];
		CGLIB$hashCode$5$Proxy = MethodProxy.create(clazz2, clazz, "()I", "hashCode", "CGLIB$hashCode$5");
		CGLIB$clone$6$Method = methodArray2[3];
		CGLIB$clone$6$Proxy = MethodProxy.create(clazz2, clazz, "()Ljava/lang/Object;", "clone", "CGLIB$clone$6");
	}

	public final boolean equals(Object object) {
		MethodInterceptor methodInterceptor = this.CGLIB$CALLBACK_5;
		if (methodInterceptor == null) {
			WorkService$$EnhancerBySpringCGLIB$$613ab0cb.CGLIB$BIND_CALLBACKS(this);
			methodInterceptor = this.CGLIB$CALLBACK_5;
		}
		if (methodInterceptor != null) {
			Object object2 = methodInterceptor.intercept(this, CGLIB$equals$3$Method, new Object[] {object}, CGLIB$equals$3$Proxy);
			return object2 == null ? false : (Boolean) object2;
		}
		return super.equals(object);
	}

	public final String toString() {
		MethodInterceptor methodInterceptor = this.CGLIB$CALLBACK_0;
		if (methodInterceptor == null) {
			WorkService$$EnhancerBySpringCGLIB$$613ab0cb.CGLIB$BIND_CALLBACKS(this);
			methodInterceptor = this.CGLIB$CALLBACK_0;
		}
		if (methodInterceptor != null) {
			return (String) methodInterceptor.intercept(this, CGLIB$toString$4$Method, CGLIB$emptyArgs, CGLIB$toString$4$Proxy);
		}
		return super.toString();
	}

	public final int hashCode() {
		MethodInterceptor methodInterceptor = this.CGLIB$CALLBACK_6;
		if (methodInterceptor == null) {
			WorkService$$EnhancerBySpringCGLIB$$613ab0cb.CGLIB$BIND_CALLBACKS(this);
			methodInterceptor = this.CGLIB$CALLBACK_6;
		}
		if (methodInterceptor != null) {
			Object object = methodInterceptor.intercept(this, CGLIB$hashCode$5$Method, CGLIB$emptyArgs, CGLIB$hashCode$5$Proxy);
			return object == null ? 0 : ((Number) object).intValue();
		}
		return super.hashCode();
	}

	protected final Object clone() throws CloneNotSupportedException {
		MethodInterceptor methodInterceptor = this.CGLIB$CALLBACK_0;
		if (methodInterceptor == null) {
			WorkService$$EnhancerBySpringCGLIB$$613ab0cb.CGLIB$BIND_CALLBACKS(this);
			methodInterceptor = this.CGLIB$CALLBACK_0;
		}
		if (methodInterceptor != null) {
			return methodInterceptor.intercept(this, CGLIB$clone$6$Method, CGLIB$emptyArgs, CGLIB$clone$6$Proxy);
		}
		return super.clone();
	}

	@Override
	public final int indexOf(Advisor advisor) {
		Dispatcher dispatcher = this.CGLIB$CALLBACK_4;
		if (dispatcher == null) {
			WorkService$$EnhancerBySpringCGLIB$$613ab0cb.CGLIB$BIND_CALLBACKS(this);
			dispatcher = this.CGLIB$CALLBACK_4;
		}
		return ((Advised) dispatcher.loadObject()).indexOf(advisor);
	}

	@Override
	public final int indexOf(Advice advice) {
		Dispatcher dispatcher = this.CGLIB$CALLBACK_4;
		if (dispatcher == null) {
			WorkService$$EnhancerBySpringCGLIB$$613ab0cb.CGLIB$BIND_CALLBACKS(this);
			dispatcher = this.CGLIB$CALLBACK_4;
		}
		return ((Advised) dispatcher.loadObject()).indexOf(advice);
	}

	@Override
	public Object newInstance(Callback[] callbackArray) {
		WorkService$$EnhancerBySpringCGLIB$$613ab0cb.CGLIB$SET_THREAD_CALLBACKS(callbackArray);
		WorkService$$EnhancerBySpringCGLIB$$613ab0cb workService$$EnhancerBySpringCGLIB$$613ab0cb = new WorkService$$EnhancerBySpringCGLIB$$613ab0cb();
		WorkService$$EnhancerBySpringCGLIB$$613ab0cb.CGLIB$SET_THREAD_CALLBACKS(null);
		return workService$$EnhancerBySpringCGLIB$$613ab0cb;
	}

	@Override
	public Object newInstance(Class[] classArray, Object[] objectArray, Callback[] callbackArray) {
		WorkService$$EnhancerBySpringCGLIB$$613ab0cb workService$$EnhancerBySpringCGLIB$$613ab0cb;
		WorkService$$EnhancerBySpringCGLIB$$613ab0cb.CGLIB$SET_THREAD_CALLBACKS(callbackArray);
		Class[] classArray2 = classArray;
		switch (classArray.length) {
			case 0: {
				workService$$EnhancerBySpringCGLIB$$613ab0cb = new WorkService$$EnhancerBySpringCGLIB$$613ab0cb();
				break;
			}
			default: {
				throw new IllegalArgumentException("Constructor not found");
			}
		}
		WorkService$$EnhancerBySpringCGLIB$$613ab0cb.CGLIB$SET_THREAD_CALLBACKS(null);
		return workService$$EnhancerBySpringCGLIB$$613ab0cb;
	}

	@Override
	public Object newInstance(Callback callback) {
		throw new IllegalStateException("More than one callback object required");
	}

	@Override
	public final boolean isFrozen() {
		Dispatcher dispatcher = this.CGLIB$CALLBACK_4;
		if (dispatcher == null) {
			WorkService$$EnhancerBySpringCGLIB$$613ab0cb.CGLIB$BIND_CALLBACKS(this);
			dispatcher = this.CGLIB$CALLBACK_4;
		}
		return ((Advised) dispatcher.loadObject()).isFrozen();
	}

	@Override
	public final JdbcTemplate getJdbcTemplate() {
		MethodInterceptor methodInterceptor = this.CGLIB$CALLBACK_0;
		if (methodInterceptor == null) {
			WorkService$$EnhancerBySpringCGLIB$$613ab0cb.CGLIB$BIND_CALLBACKS(this);
			methodInterceptor = this.CGLIB$CALLBACK_0;
		}
		if (methodInterceptor != null) {
			return (JdbcTemplate) methodInterceptor.intercept(this, CGLIB$getJdbcTemplate$1$Method, CGLIB$emptyArgs, CGLIB$getJdbcTemplate$1$Proxy);
		}
		return super.getJdbcTemplate();
	}

	@Override
	public final void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		MethodInterceptor methodInterceptor = this.CGLIB$CALLBACK_0;
		if (methodInterceptor == null) {
			WorkService$$EnhancerBySpringCGLIB$$613ab0cb.CGLIB$BIND_CALLBACKS(this);
			methodInterceptor = this.CGLIB$CALLBACK_0;
		}
		if (methodInterceptor != null) {
			Object object = methodInterceptor.intercept(this, CGLIB$setJdbcTemplate$0$Method, new Object[] {jdbcTemplate}, CGLIB$setJdbcTemplate$0$Proxy);
			return;
		}
		super.setJdbcTemplate(jdbcTemplate);
	}

	@Override
	public final void work() {
		MethodInterceptor methodInterceptor = this.CGLIB$CALLBACK_0;
		if (methodInterceptor == null) {
			WorkService$$EnhancerBySpringCGLIB$$613ab0cb.CGLIB$BIND_CALLBACKS(this);
			methodInterceptor = this.CGLIB$CALLBACK_0;
		}
		if (methodInterceptor != null) {
			Object object = methodInterceptor.intercept(this, CGLIB$work$2$Method, CGLIB$emptyArgs, CGLIB$work$2$Proxy);
			return;
		}
		super.work();
	}

	public final Class getTargetClass() {
		Dispatcher dispatcher = this.CGLIB$CALLBACK_4;
		if (dispatcher == null) {
			WorkService$$EnhancerBySpringCGLIB$$613ab0cb.CGLIB$BIND_CALLBACKS(this);
			dispatcher = this.CGLIB$CALLBACK_4;
		}
		return ((TargetClassAware) dispatcher.loadObject()).getTargetClass();
	}

	@Override
	public final boolean replaceAdvisor(Advisor advisor, Advisor advisor2) throws AopConfigException {
		Dispatcher dispatcher = this.CGLIB$CALLBACK_4;
		if (dispatcher == null) {
			WorkService$$EnhancerBySpringCGLIB$$613ab0cb.CGLIB$BIND_CALLBACKS(this);
			dispatcher = this.CGLIB$CALLBACK_4;
		}
		return ((Advised) dispatcher.loadObject()).replaceAdvisor(advisor, advisor2);
	}

	@Override
	public final Advisor[] getAdvisors() {
		Dispatcher dispatcher = this.CGLIB$CALLBACK_4;
		if (dispatcher == null) {
			WorkService$$EnhancerBySpringCGLIB$$613ab0cb.CGLIB$BIND_CALLBACKS(this);
			dispatcher = this.CGLIB$CALLBACK_4;
		}
		return ((Advised) dispatcher.loadObject()).getAdvisors();
	}

	@Override
	public final void addAdvice(Advice advice) throws AopConfigException {
		Dispatcher dispatcher = this.CGLIB$CALLBACK_4;
		if (dispatcher == null) {
			WorkService$$EnhancerBySpringCGLIB$$613ab0cb.CGLIB$BIND_CALLBACKS(this);
			dispatcher = this.CGLIB$CALLBACK_4;
		}
		((Advised) dispatcher.loadObject()).addAdvice(advice);
	}

	@Override
	public final void addAdvice(int n, Advice advice) throws AopConfigException {
		Dispatcher dispatcher = this.CGLIB$CALLBACK_4;
		if (dispatcher == null) {
			WorkService$$EnhancerBySpringCGLIB$$613ab0cb.CGLIB$BIND_CALLBACKS(this);
			dispatcher = this.CGLIB$CALLBACK_4;
		}
		((Advised) dispatcher.loadObject()).addAdvice(n, advice);
	}

	@Override
	public final boolean isPreFiltered() {
		Dispatcher dispatcher = this.CGLIB$CALLBACK_4;
		if (dispatcher == null) {
			WorkService$$EnhancerBySpringCGLIB$$613ab0cb.CGLIB$BIND_CALLBACKS(this);
			dispatcher = this.CGLIB$CALLBACK_4;
		}
		return ((Advised) dispatcher.loadObject()).isPreFiltered();
	}

	@Override
	public final void setPreFiltered(boolean bl) {
		Dispatcher dispatcher = this.CGLIB$CALLBACK_4;
		if (dispatcher == null) {
			WorkService$$EnhancerBySpringCGLIB$$613ab0cb.CGLIB$BIND_CALLBACKS(this);
			dispatcher = this.CGLIB$CALLBACK_4;
		}
		((Advised) dispatcher.loadObject()).setPreFiltered(bl);
	}

	@Override
	public final void removeAdvisor(int n) throws AopConfigException {
		Dispatcher dispatcher = this.CGLIB$CALLBACK_4;
		if (dispatcher == null) {
			WorkService$$EnhancerBySpringCGLIB$$613ab0cb.CGLIB$BIND_CALLBACKS(this);
			dispatcher = this.CGLIB$CALLBACK_4;
		}
		((Advised) dispatcher.loadObject()).removeAdvisor(n);
	}

	@Override
	public final boolean removeAdvisor(Advisor advisor) {
		Dispatcher dispatcher = this.CGLIB$CALLBACK_4;
		if (dispatcher == null) {
			WorkService$$EnhancerBySpringCGLIB$$613ab0cb.CGLIB$BIND_CALLBACKS(this);
			dispatcher = this.CGLIB$CALLBACK_4;
		}
		return ((Advised) dispatcher.loadObject()).removeAdvisor(advisor);
	}

	@Override
	public final boolean removeAdvice(Advice advice) {
		Dispatcher dispatcher = this.CGLIB$CALLBACK_4;
		if (dispatcher == null) {
			WorkService$$EnhancerBySpringCGLIB$$613ab0cb.CGLIB$BIND_CALLBACKS(this);
			dispatcher = this.CGLIB$CALLBACK_4;
		}
		return ((Advised) dispatcher.loadObject()).removeAdvice(advice);
	}

	@Override
	public final void addAdvisor(int n, Advisor advisor) throws AopConfigException {
		Dispatcher dispatcher = this.CGLIB$CALLBACK_4;
		if (dispatcher == null) {
			WorkService$$EnhancerBySpringCGLIB$$613ab0cb.CGLIB$BIND_CALLBACKS(this);
			dispatcher = this.CGLIB$CALLBACK_4;
		}
		((Advised) dispatcher.loadObject()).addAdvisor(n, advisor);
	}

	@Override
	public final void addAdvisor(Advisor advisor) throws AopConfigException {
		Dispatcher dispatcher = this.CGLIB$CALLBACK_4;
		if (dispatcher == null) {
			WorkService$$EnhancerBySpringCGLIB$$613ab0cb.CGLIB$BIND_CALLBACKS(this);
			dispatcher = this.CGLIB$CALLBACK_4;
		}
		((Advised) dispatcher.loadObject()).addAdvisor(advisor);
	}

	@Override
	public final boolean isProxyTargetClass() {
		Dispatcher dispatcher = this.CGLIB$CALLBACK_4;
		if (dispatcher == null) {
			WorkService$$EnhancerBySpringCGLIB$$613ab0cb.CGLIB$BIND_CALLBACKS(this);
			dispatcher = this.CGLIB$CALLBACK_4;
		}
		return ((Advised) dispatcher.loadObject()).isProxyTargetClass();
	}

	@Override
	public final TargetSource getTargetSource() {
		Dispatcher dispatcher = this.CGLIB$CALLBACK_4;
		if (dispatcher == null) {
			WorkService$$EnhancerBySpringCGLIB$$613ab0cb.CGLIB$BIND_CALLBACKS(this);
			dispatcher = this.CGLIB$CALLBACK_4;
		}
		return ((Advised) dispatcher.loadObject()).getTargetSource();
	}

	@Override
	public final void setTargetSource(TargetSource targetSource) {
		Dispatcher dispatcher = this.CGLIB$CALLBACK_4;
		if (dispatcher == null) {
			WorkService$$EnhancerBySpringCGLIB$$613ab0cb.CGLIB$BIND_CALLBACKS(this);
			dispatcher = this.CGLIB$CALLBACK_4;
		}
		((Advised) dispatcher.loadObject()).setTargetSource(targetSource);
	}

	@Override
	public final boolean isExposeProxy() {
		Dispatcher dispatcher = this.CGLIB$CALLBACK_4;
		if (dispatcher == null) {
			WorkService$$EnhancerBySpringCGLIB$$613ab0cb.CGLIB$BIND_CALLBACKS(this);
			dispatcher = this.CGLIB$CALLBACK_4;
		}
		return ((Advised) dispatcher.loadObject()).isExposeProxy();
	}

	@Override
	public final void setExposeProxy(boolean bl) {
		Dispatcher dispatcher = this.CGLIB$CALLBACK_4;
		if (dispatcher == null) {
			WorkService$$EnhancerBySpringCGLIB$$613ab0cb.CGLIB$BIND_CALLBACKS(this);
			dispatcher = this.CGLIB$CALLBACK_4;
		}
		((Advised) dispatcher.loadObject()).setExposeProxy(bl);
	}

	@Override
	public final String toProxyConfigString() {
		Dispatcher dispatcher = this.CGLIB$CALLBACK_4;
		if (dispatcher == null) {
			WorkService$$EnhancerBySpringCGLIB$$613ab0cb.CGLIB$BIND_CALLBACKS(this);
			dispatcher = this.CGLIB$CALLBACK_4;
		}
		return ((Advised) dispatcher.loadObject()).toProxyConfigString();
	}

	public final boolean isInterfaceProxied(Class clazz) {
		Dispatcher dispatcher = this.CGLIB$CALLBACK_4;
		if (dispatcher == null) {
			WorkService$$EnhancerBySpringCGLIB$$613ab0cb.CGLIB$BIND_CALLBACKS(this);
			dispatcher = this.CGLIB$CALLBACK_4;
		}
		return ((Advised) dispatcher.loadObject()).isInterfaceProxied(clazz);
	}

	public final Class[] getProxiedInterfaces() {
		Dispatcher dispatcher = this.CGLIB$CALLBACK_4;
		if (dispatcher == null) {
			WorkService$$EnhancerBySpringCGLIB$$613ab0cb.CGLIB$BIND_CALLBACKS(this);
			dispatcher = this.CGLIB$CALLBACK_4;
		}
		return ((Advised) dispatcher.loadObject()).getProxiedInterfaces();
	}

	@Override
	public void setCallback(int n, Callback callback) {
		switch (n) {
			case 0: {
				this.CGLIB$CALLBACK_0 = (MethodInterceptor) callback;
				break;
			}
			case 1: {
				this.CGLIB$CALLBACK_1 = (MethodInterceptor) callback;
				break;
			}
			case 2: {
				this.CGLIB$CALLBACK_2 = (NoOp) callback;
				break;
			}
			case 3: {
				this.CGLIB$CALLBACK_3 = (Dispatcher) callback;
				break;
			}
			case 4: {
				this.CGLIB$CALLBACK_4 = (Dispatcher) callback;
				break;
			}
			case 5: {
				this.CGLIB$CALLBACK_5 = (MethodInterceptor) callback;
				break;
			}
			case 6: {
				this.CGLIB$CALLBACK_6 = (MethodInterceptor) callback;
				break;
			}
		}
	}

	@Override
	public Callback getCallback(int n) {
		Callback callback;
		WorkService$$EnhancerBySpringCGLIB$$613ab0cb.CGLIB$BIND_CALLBACKS(this);
		WorkService$$EnhancerBySpringCGLIB$$613ab0cb workService$$EnhancerBySpringCGLIB$$613ab0cb = this;
		switch (n) {
			case 0: {
				callback = workService$$EnhancerBySpringCGLIB$$613ab0cb.CGLIB$CALLBACK_0;
				break;
			}
			case 1: {
				callback = workService$$EnhancerBySpringCGLIB$$613ab0cb.CGLIB$CALLBACK_1;
				break;
			}
			case 2: {
				callback = workService$$EnhancerBySpringCGLIB$$613ab0cb.CGLIB$CALLBACK_2;
				break;
			}
			case 3: {
				callback = workService$$EnhancerBySpringCGLIB$$613ab0cb.CGLIB$CALLBACK_3;
				break;
			}
			case 4: {
				callback = workService$$EnhancerBySpringCGLIB$$613ab0cb.CGLIB$CALLBACK_4;
				break;
			}
			case 5: {
				callback = workService$$EnhancerBySpringCGLIB$$613ab0cb.CGLIB$CALLBACK_5;
				break;
			}
			case 6: {
				callback = workService$$EnhancerBySpringCGLIB$$613ab0cb.CGLIB$CALLBACK_6;
				break;
			}
			default: {
				callback = null;
			}
		}
		return callback;
	}

	@Override
	public Callback[] getCallbacks() {
		WorkService$$EnhancerBySpringCGLIB$$613ab0cb.CGLIB$BIND_CALLBACKS(this);
		WorkService$$EnhancerBySpringCGLIB$$613ab0cb workService$$EnhancerBySpringCGLIB$$613ab0cb = this;
		return new Callback[] {this.CGLIB$CALLBACK_0, this.CGLIB$CALLBACK_1, this.CGLIB$CALLBACK_2, this.CGLIB$CALLBACK_3, this.CGLIB$CALLBACK_4, this.CGLIB$CALLBACK_5, this.CGLIB$CALLBACK_6};
	}

	@Override
	public void setCallbacks(Callback[] callbackArray) {
		this.CGLIB$CALLBACK_0 = (MethodInterceptor) callbackArray[0];
		this.CGLIB$CALLBACK_1 = (MethodInterceptor) callbackArray[1];
		this.CGLIB$CALLBACK_2 = (NoOp) callbackArray[2];
		this.CGLIB$CALLBACK_3 = (Dispatcher) callbackArray[3];
		this.CGLIB$CALLBACK_4 = (Dispatcher) callbackArray[4];
		this.CGLIB$CALLBACK_5 = (MethodInterceptor) callbackArray[5];
		Callback[] callbackArray2 = callbackArray;
		WorkService$$EnhancerBySpringCGLIB$$613ab0cb workService$$EnhancerBySpringCGLIB$$613ab0cb = this;
		this.CGLIB$CALLBACK_6 = (MethodInterceptor) callbackArray[6];
	}

	final void CGLIB$setJdbcTemplate$0(JdbcTemplate jdbcTemplate) {
		super.setJdbcTemplate(jdbcTemplate);
	}

	final JdbcTemplate CGLIB$getJdbcTemplate$1() {
		return super.getJdbcTemplate();
	}

	final String CGLIB$toString$4() {
		return super.toString();
	}

	final void CGLIB$work$2() {
		super.work();
	}

	final int CGLIB$hashCode$5() {
		return super.hashCode();
	}

	final Object CGLIB$clone$6() throws CloneNotSupportedException {
		return super.clone();
	}

	final boolean CGLIB$equals$3(Object object) {
		return super.equals(object);
	}

	static {
		WorkService$$EnhancerBySpringCGLIB$$613ab0cb.CGLIB$STATICHOOK3();
	}
}