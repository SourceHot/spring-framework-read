package org.sourcehot.springmvc.httpinvoker;

import org.springframework.stereotype.Service;

@Service("helloService")
public class IHttpInvokerServiceImpl implements IHttpInvokerService {
    @Override
    public int add(int a, int b) {
        return a + b;
    }
}
