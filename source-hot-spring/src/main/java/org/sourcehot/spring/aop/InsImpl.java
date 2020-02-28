package org.sourcehot.spring.aop;

import org.springframework.stereotype.Service;

@Service
public class InsImpl implements Ins {
    @Override
    public void hh() {
        System.out.println("hh");
    }
}
