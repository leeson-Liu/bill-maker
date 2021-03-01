package com.bill.maker.controller;

import com.bill.maker.entity.Good;
import com.bill.maker.service.BillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController("BillMakerController")
@RequestMapping("/bill")
@Slf4j
public class BillMakerController {

    @Autowired
    private BillService billService;


    @GetMapping("/randomGoodList")
    public List<Good> randomGoodList(@RequestParam(value = "money") Double money) {
        return billService.randomGoodList(money);
    }

    /**
     * @param type 支付宝：zfb ;  微信：wx
     * @return
     * @throws Exception
     */
    @GetMapping("/upload")
    public String uploadWX(@RequestParam(value = "type") String type) throws Exception {
        //之后考虑改成工厂模式
        if ("zfb".equals(type)) {
            return billService.uploadZFB();
        } else if ("wx".equals(type)) {
            return billService.uploadWX();
        }
        return "wrong type:" + type;
    }


}
