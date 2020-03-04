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

package org.springframework.jdbc.support;

import org.springframework.lang.Nullable;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * JavaBean for holding JDBC error codes for a particular database.
 * Instances of this class are normally loaded through a bean factory.
 *
 * <p>Used by Spring's {@link SQLErrorCodeSQLExceptionTranslator}.
 * The file "sql-error-codes.xml" in this package contains default
 * {@code SQLErrorCodes} instances for various databases.
 *
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @see SQLErrorCodesFactory
 * @see SQLErrorCodeSQLExceptionTranslator
 */
public class SQLErrorCodes {

    @Nullable
    private String[] databaseProductNames;

    private boolean useSqlStateForTranslation = false;

    private String[] badSqlGrammarCodes = new String[0];

    private String[] invalidResultSetAccessCodes = new String[0];

    private String[] duplicateKeyCodes = new String[0];

    private String[] dataIntegrityViolationCodes = new String[0];

    private String[] permissionDeniedCodes = new String[0];

    private String[] dataAccessResourceFailureCodes = new String[0];

    private String[] transientDataAccessResourceCodes = new String[0];

    private String[] cannotAcquireLockCodes = new String[0];

    private String[] deadlockLoserCodes = new String[0];

    private String[] cannotSerializeTransactionCodes = new String[0];

    @Nullable
    private CustomSQLErrorCodesTranslation[] customTranslations;

    @Nullable
    private SQLExceptionTranslator customSqlExceptionTranslator;

    @Nullable
    public String getDatabaseProductName() {
        return (this.databaseProductNames != null && this.databaseProductNames.length > 0 ?
                this.databaseProductNames[0] : null);
    }

    /**
     * Set this property if the database name contains spaces,
     * in which case we can not use the bean name for lookup.
     */
    public void setDatabaseProductName(@Nullable String databaseProductName) {
        this.databaseProductNames = new String[]{databaseProductName};
    }

    @Nullable
    public String[] getDatabaseProductNames() {
        return this.databaseProductNames;
    }

    /**
     * Set this property to specify multiple database names that contains spaces,
     * in which case we can not use bean names for lookup.
     */
    public void setDatabaseProductNames(@Nullable String... databaseProductNames) {
        this.databaseProductNames = databaseProductNames;
    }

    public boolean isUseSqlStateForTranslation() {
        return this.useSqlStateForTranslation;
    }

    /**
     * Set this property to true for databases that do not provide an error code
     * but that do provide SQL State (this includes PostgreSQL).
     */
    public void setUseSqlStateForTranslation(boolean useStateCodeForTranslation) {
        this.useSqlStateForTranslation = useStateCodeForTranslation;
    }

    public String[] getBadSqlGrammarCodes() {
        return this.badSqlGrammarCodes;
    }

    public void setBadSqlGrammarCodes(String... badSqlGrammarCodes) {
        this.badSqlGrammarCodes = StringUtils.sortStringArray(badSqlGrammarCodes);
    }

    public String[] getInvalidResultSetAccessCodes() {
        return this.invalidResultSetAccessCodes;
    }

    public void setInvalidResultSetAccessCodes(String... invalidResultSetAccessCodes) {
        this.invalidResultSetAccessCodes = StringUtils.sortStringArray(invalidResultSetAccessCodes);
    }

    public String[] getDuplicateKeyCodes() {
        return this.duplicateKeyCodes;
    }

    public void setDuplicateKeyCodes(String... duplicateKeyCodes) {
        this.duplicateKeyCodes = duplicateKeyCodes;
    }

    public String[] getDataIntegrityViolationCodes() {
        return this.dataIntegrityViolationCodes;
    }

    public void setDataIntegrityViolationCodes(String... dataIntegrityViolationCodes) {
        this.dataIntegrityViolationCodes = StringUtils.sortStringArray(dataIntegrityViolationCodes);
    }

    public String[] getPermissionDeniedCodes() {
        return this.permissionDeniedCodes;
    }

    public void setPermissionDeniedCodes(String... permissionDeniedCodes) {
        this.permissionDeniedCodes = StringUtils.sortStringArray(permissionDeniedCodes);
    }

    public String[] getDataAccessResourceFailureCodes() {
        return this.dataAccessResourceFailureCodes;
    }

    public void setDataAccessResourceFailureCodes(String... dataAccessResourceFailureCodes) {
        this.dataAccessResourceFailureCodes = StringUtils.sortStringArray(dataAccessResourceFailureCodes);
    }

    public String[] getTransientDataAccessResourceCodes() {
        return this.transientDataAccessResourceCodes;
    }

    public void setTransientDataAccessResourceCodes(String... transientDataAccessResourceCodes) {
        this.transientDataAccessResourceCodes = StringUtils.sortStringArray(transientDataAccessResourceCodes);
    }

    public String[] getCannotAcquireLockCodes() {
        return this.cannotAcquireLockCodes;
    }

    public void setCannotAcquireLockCodes(String... cannotAcquireLockCodes) {
        this.cannotAcquireLockCodes = StringUtils.sortStringArray(cannotAcquireLockCodes);
    }

    public String[] getDeadlockLoserCodes() {
        return this.deadlockLoserCodes;
    }

    public void setDeadlockLoserCodes(String... deadlockLoserCodes) {
        this.deadlockLoserCodes = StringUtils.sortStringArray(deadlockLoserCodes);
    }

    public String[] getCannotSerializeTransactionCodes() {
        return this.cannotSerializeTransactionCodes;
    }

    public void setCannotSerializeTransactionCodes(String... cannotSerializeTransactionCodes) {
        this.cannotSerializeTransactionCodes = StringUtils.sortStringArray(cannotSerializeTransactionCodes);
    }

    @Nullable
    public CustomSQLErrorCodesTranslation[] getCustomTranslations() {
        return this.customTranslations;
    }

    public void setCustomTranslations(CustomSQLErrorCodesTranslation... customTranslations) {
        this.customTranslations = customTranslations;
    }

    public void setCustomSqlExceptionTranslatorClass(@Nullable Class<? extends SQLExceptionTranslator> customTranslatorClass) {
        if (customTranslatorClass != null) {
            try {
                this.customSqlExceptionTranslator =
                        ReflectionUtils.accessibleConstructor(customTranslatorClass).newInstance();
            } catch (Throwable ex) {
                throw new IllegalStateException("Unable to instantiate custom translator", ex);
            }
        } else {
            this.customSqlExceptionTranslator = null;
        }
    }

    @Nullable
    public SQLExceptionTranslator getCustomSqlExceptionTranslator() {
        return this.customSqlExceptionTranslator;
    }

    public void setCustomSqlExceptionTranslator(@Nullable SQLExceptionTranslator customSqlExceptionTranslator) {
        this.customSqlExceptionTranslator = customSqlExceptionTranslator;
    }

}
