package org.sourcehot.spring.hibernate;

import org.sourcehot.spring.dao.HsLog;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

public class HibernateTemplateSourceCode extends HibernateDaoSupport {
    public void demo(int id) {
        HsLog hsLog = this.getHibernateTemplate().get(HsLog.class, id);
    }

}
