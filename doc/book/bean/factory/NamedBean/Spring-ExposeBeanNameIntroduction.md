# Spring ExposeBeanNameIntroduction
- 类全路径: `org.springframework.aop.interceptor.ExposeBeanNameAdvisors.ExposeBeanNameIntroduction`


## 成员变量
- beanName 


## 方法分析
### invoke
- 方法签名: `org.springframework.aop.interceptor.ExposeBeanNameAdvisors.ExposeBeanNameIntroduction.invoke`

- 方法作用: 设置`BEAN_NAME_ATTRIBUTE`属性为 beanName



### 完整代码

```java
	@SuppressWarnings("serial")
	private static class ExposeBeanNameIntroduction extends DelegatingIntroductionInterceptor implements NamedBean {

		private final String beanName;

		public ExposeBeanNameIntroduction(String beanName) {
			this.beanName = beanName;
		}

		@Override
		public Object invoke(MethodInvocation mi) throws Throwable {
			if (!(mi instanceof ProxyMethodInvocation)) {
				throw new IllegalStateException("MethodInvocation is not a Spring ProxyMethodInvocation: " + mi);
			}
			ProxyMethodInvocation pmi = (ProxyMethodInvocation) mi;
			// 设置属性
			pmi.setUserAttribute(BEAN_NAME_ATTRIBUTE, this.beanName);
			return super.invoke(mi);
		}

		@Override
		public String getBeanName() {
			return this.beanName;
		}
	}

```