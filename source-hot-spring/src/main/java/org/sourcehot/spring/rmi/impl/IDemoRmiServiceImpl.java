package org.sourcehot.spring.rmi.impl;


import org.sourcehot.spring.rmi.IDemoRmiService;

public class IDemoRmiServiceImpl implements IDemoRmiService {
    @Override
    public int add(int a, int b) {
        return a + b;
    }
}
