package com.bill.maker.controller;

import com.bill.maker.entity.Good;
import com.bill.maker.entity.GoodUpload;
import com.bill.maker.service.BillService;
import com.bill.maker.utils.CsvUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;


@RestController("BillMakerController")
@RequestMapping("/bill")
@Slf4j
public class BillMakerController {

    @Autowired
    private BillService billService;


    @GetMapping("/randomGoodList")
    public List<Good> randomGoodList(@RequestParam(value = "money") Double money) {
        List<Good> goodList =  billService.randomGoodList(money, "test");
        if (dataVail(goodList, money)) {
            log.info("对了");
        }else {
            log.error("数据有误");
        }
        return goodList;
    }
    private boolean dataVail(List<Good> goodList, double money) {
        BigDecimal total = BigDecimal.ZERO;
        for (Good good : goodList) {
            total = total.add(good.getTotalPrice());
        }
        return total.doubleValue() == money;
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

    /**
     *
     * @param file
     * @return
     * 用于生成品名初始数据的java代码
     */
    @GetMapping("/uploadGood")
    public String upload(@RequestParam("file") MultipartFile file) {
        int id = 1;
        List<GoodUpload> goods = CsvUtils.readCsv(file, GoodUpload.class);
        for (GoodUpload good : goods) {
            System.out.println("GOOD_LIST.add(Good.builder().id(" + id + ").name(\"" + good.getName().trim() +"\").minPrice("+good.getMinPrice().trim()+").maxPrice("
            +good.getMaxPrice()+").weight("+good.getWeight()+").build());");
            id++;
        }
        return "ok";
    }


}
