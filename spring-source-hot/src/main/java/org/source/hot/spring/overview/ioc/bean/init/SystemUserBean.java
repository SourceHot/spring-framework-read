package org.source.hot.spring.overview.ioc.bean.init;

public class SystemUserBean {
	private UserBean userBean;

	public UserBean getUserBean() {
		return userBean;
	}

	public void setUserBean(UserBean userBean) {
		this.userBean = userBean;
	}
}
