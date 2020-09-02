package org.source.hot.spring.overview.ioc.bean.init;


import org.springframework.beans.factory.FactoryBean;

public class UserFactoryBean implements FactoryBean<UserBean> {

  @Override
  public boolean isSingleton() {
    return true;
  }

  @Override
  public UserBean getObject() throws Exception {
    return UserBean.createInstance();
  }

  @Override
  public Class<?> getObjectType() {
    return UserBean.class;
  }
}
