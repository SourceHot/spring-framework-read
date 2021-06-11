/*
 * Copyright 2002-2017 the original author or authors.
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

package org.springframework.orm.jpa.persistenceunit;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * Spring's base implementation of the JPA
 * {@link javax.persistence.spi.PersistenceUnitInfo} interface,
 * used to bootstrap an {@code EntityManagerFactory} in a container.
 *
 * <p>This implementation is largely a JavaBean, offering mutators
 * for all standard {@code PersistenceUnitInfo} properties.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Costin Leau
 * @since 2.0
 */
public class MutablePersistenceUnitInfo implements SmartPersistenceUnitInfo {
	/**
	 * 映射文件名称集合
	 */
	private final List<String> mappingFileNames = new LinkedList<>();

	/**
	 * 托管类名集合
	 */
	private final List<String> managedClassNames = new LinkedList<>();

	/**
	 * 托管包名集合
	 */
	private final List<String> managedPackages = new LinkedList<>();

	/**
	 * 持久单元名称
	 */
	@Nullable
	private String persistenceUnitName;

	/**
	 * persistence.xml文件中的provider元素
	 */
	@Nullable
	private String persistenceProviderClassName;

	/**
	 * 持久单元的事务类型
	 */
	@Nullable
	private PersistenceUnitTransactionType transactionType;

	/**
	 * 非JTA数据源
	 */
	@Nullable
	private DataSource nonJtaDataSource;

	/**
	 * JTA数据源
	 */
	@Nullable
	private DataSource jtaDataSource;

	/**
	 * JAR协议的URL地址集合
	 */
	private final List<URL> jarFileUrls = new LinkedList<>();

	/**
	 * 持久单元根路径
	 */
	@Nullable
	private URL persistenceUnitRootUrl;

	/**
	 * 是否排除未列出的类
	 */
	private boolean excludeUnlistedClasses = false;

	/**
	 * 缓存拱秀模式
	 */
	private SharedCacheMode sharedCacheMode = SharedCacheMode.UNSPECIFIED;

	/**
	 * 验证模式
	 */
	private ValidationMode validationMode = ValidationMode.AUTO;

	/**
	 * 属性表
	 */
	private Properties properties = new Properties();

	/**
	 * 持久单元xml版本号
	 */
	private String persistenceXMLSchemaVersion = "2.0";

	/**
	 * 持久能力提供者的包名
	 */
	@Nullable
	private String persistenceProviderPackageName;

	@Override
	@Nullable
	public String getPersistenceUnitName() {
		return this.persistenceUnitName;
	}

	public void setPersistenceUnitName(@Nullable String persistenceUnitName) {
		this.persistenceUnitName = persistenceUnitName;
	}

	@Override
	@Nullable
	public String getPersistenceProviderClassName() {
		return this.persistenceProviderClassName;
	}

	public void setPersistenceProviderClassName(@Nullable String persistenceProviderClassName) {
		this.persistenceProviderClassName = persistenceProviderClassName;
	}

	@Override
	public PersistenceUnitTransactionType getTransactionType() {
		if (this.transactionType != null) {
			return this.transactionType;
		}
		else {
			return (this.jtaDataSource != null ?
					PersistenceUnitTransactionType.JTA : PersistenceUnitTransactionType.RESOURCE_LOCAL);
		}
	}

	public void setTransactionType(PersistenceUnitTransactionType transactionType) {
		this.transactionType = transactionType;
	}

	@Override
	@Nullable
	public DataSource getJtaDataSource() {
		return this.jtaDataSource;
	}

	public void setJtaDataSource(@Nullable DataSource jtaDataSource) {
		this.jtaDataSource = jtaDataSource;
	}

	@Override
	@Nullable
	public DataSource getNonJtaDataSource() {
		return this.nonJtaDataSource;
	}

	public void setNonJtaDataSource(@Nullable DataSource nonJtaDataSource) {
		this.nonJtaDataSource = nonJtaDataSource;
	}

	public void addMappingFileName(String mappingFileName) {
		this.mappingFileNames.add(mappingFileName);
	}

	@Override
	public List<String> getMappingFileNames() {
		return this.mappingFileNames;
	}

	public void addJarFileUrl(URL jarFileUrl) {
		this.jarFileUrls.add(jarFileUrl);
	}

	@Override
	public List<URL> getJarFileUrls() {
		return this.jarFileUrls;
	}

	@Override
	@Nullable
	public URL getPersistenceUnitRootUrl() {
		return this.persistenceUnitRootUrl;
	}

	public void setPersistenceUnitRootUrl(@Nullable URL persistenceUnitRootUrl) {
		this.persistenceUnitRootUrl = persistenceUnitRootUrl;
	}

	/**
	 * Add a managed class name to the persistence provider's metadata.
	 * @see javax.persistence.spi.PersistenceUnitInfo#getManagedClassNames()
	 * @see #addManagedPackage
	 */
	public void addManagedClassName(String managedClassName) {
		this.managedClassNames.add(managedClassName);
	}

	@Override
	public List<String> getManagedClassNames() {
		return this.managedClassNames;
	}

	/**
	 * Add a managed package to the persistence provider's metadata.
	 * <p>Note: This refers to annotated {@code package-info.java} files. It does
	 * <i>not</i> trigger entity scanning in the specified package; this is
	 * rather the job of {@link DefaultPersistenceUnitManager#setPackagesToScan}.
	 * @since 4.1
	 * @see SmartPersistenceUnitInfo#getManagedPackages()
	 * @see #addManagedClassName
	 */
	public void addManagedPackage(String packageName) {
		this.managedPackages.add(packageName);
	}

	@Override
	public List<String> getManagedPackages() {
		return this.managedPackages;
	}

	public void setExcludeUnlistedClasses(boolean excludeUnlistedClasses) {
		this.excludeUnlistedClasses = excludeUnlistedClasses;
	}

	@Override
	public boolean excludeUnlistedClasses() {
		return this.excludeUnlistedClasses;
	}

	@Override
	public SharedCacheMode getSharedCacheMode() {
		return this.sharedCacheMode;
	}

	public void setSharedCacheMode(SharedCacheMode sharedCacheMode) {
		this.sharedCacheMode = sharedCacheMode;
	}

	@Override
	public ValidationMode getValidationMode() {
		return this.validationMode;
	}

	public void setValidationMode(ValidationMode validationMode) {
		this.validationMode = validationMode;
	}

	public void addProperty(String name, String value) {
		this.properties.setProperty(name, value);
	}

	@Override
	public Properties getProperties() {
		return this.properties;
	}

	public void setProperties(Properties properties) {
		Assert.notNull(properties, "Properties must not be null");
		this.properties = properties;
	}

	@Override
	public String getPersistenceXMLSchemaVersion() {
		return this.persistenceXMLSchemaVersion;
	}

	public void setPersistenceXMLSchemaVersion(String persistenceXMLSchemaVersion) {
		this.persistenceXMLSchemaVersion = persistenceXMLSchemaVersion;
	}

	@Nullable
	public String getPersistenceProviderPackageName() {
		return this.persistenceProviderPackageName;
	}

	@Override
	public void setPersistenceProviderPackageName(@Nullable String persistenceProviderPackageName) {
		this.persistenceProviderPackageName = persistenceProviderPackageName;
	}

	/**
	 * This implementation returns the default ClassLoader.
	 * @see org.springframework.util.ClassUtils#getDefaultClassLoader()
	 */
	@Override
	@Nullable
	public ClassLoader getClassLoader() {
		return ClassUtils.getDefaultClassLoader();
	}

	/**
	 * This implementation throws an UnsupportedOperationException.
	 */
	@Override
	public void addTransformer(ClassTransformer classTransformer) {
		throw new UnsupportedOperationException("addTransformer not supported");
	}

	/**
	 * This implementation throws an UnsupportedOperationException.
	 */
	@Override
	public ClassLoader getNewTempClassLoader() {
		throw new UnsupportedOperationException("getNewTempClassLoader not supported");
	}


	@Override
	public String toString() {
		return "PersistenceUnitInfo: name '" + this.persistenceUnitName +
				"', root URL [" + this.persistenceUnitRootUrl + "]";
	}

}
