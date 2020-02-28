package org.sourcehot.springmvc.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DemoController {
    @GetMapping("/")
    public String in() {
        return "jlkasjld";
    }

    @GetMapping("/de")
    public String de() {
        return "hello";
    }
}
