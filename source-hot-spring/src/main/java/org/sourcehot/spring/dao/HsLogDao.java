package org.sourcehot.spring.dao;

import java.util.List;

public interface HsLogDao {
    List<HsLog> findAll();

    void save(HsLog hsLog);

    HsLog byId(Integer id);
}
