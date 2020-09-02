package org.source.hot.spring.overview.ioc.bean.init;


public class UserBeanFactoryImpl implements
        UserBeanFactory {


    @Override
    public UserBean factory() {
        return UserBean.createInstance();
    }
}
