package org.sourcehot.spring.mvc.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class IndexController {
    @RequestMapping("/qc")
    public ModelAndView index() {

        List<Item> itemList = new ArrayList<>();
        itemList.add(new Item("吃的", 3.3, new Date()));
        itemList.add(new Item("玩的", 3.3, new Date()));
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("itemList", itemList);
        modelAndView.setViewName("success");
        return modelAndView;
    }

    @RequestMapping("/hello")
    public String hello() {
        return "hello";
    }
}
