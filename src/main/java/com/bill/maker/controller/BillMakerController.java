package com.bill.maker.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController("BillMakerController")
@RequestMapping("/bill")
public class BillMakerController {

    @GetMapping("/helloWorld")
    public String hello(@RequestParam(value = "word") String word) {

        return "ok " + word;
    }


}
