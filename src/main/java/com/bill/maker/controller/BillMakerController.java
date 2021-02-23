package com.bill.maker.controller;

import com.bill.maker.entity.Good;
import com.bill.maker.utils.ExcelUtils;
import com.bill.maker.utils.WeightRandom;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

//import com.bill.maker.utils.ReadExcelFilesUtils;

@RestController("BillMakerController")
@RequestMapping("/bill")
@Slf4j
public class BillMakerController {

    @Autowired
    private ExcelUtils excelUtils;
    public static String filepath = "D:\\file\\";

    @GetMapping("/helloWorld")
    public String hello(@RequestParam(value = "word") String word) {
        Pair<String, Integer> pair1 = new ImmutablePair<>("test1", 1);
        Pair<String, Integer> pair2 = new ImmutablePair<>("test2", 2);
        Pair<String, Integer> pair3 = new ImmutablePair<>("test3", 7);
        WeightRandom<String, Integer> weightRandom = new WeightRandom(Lists.newArrayList(pair1, pair2, pair3));

        AtomicInteger test1 = new AtomicInteger(0);
        AtomicInteger test2 = new AtomicInteger(0);
        AtomicInteger test3 = new AtomicInteger(0);
        for (int i = 0; i < 100000000; i++) {
            String result = weightRandom.random();
            switch (result) {
                case "test1":
                    test1.incrementAndGet();
                    break;
                case "test2":
                    test2.incrementAndGet();
                    break;
                case "test3":
                    test3.incrementAndGet();
                    break;
            }
        }

        log.info("test1:{}", test1.get());
        log.info("test2:{}", test2.get());
        log.info("test3:{}", test3.get());
        return "ok " + word;

    }

    @GetMapping("/upload")
    public String upload(@RequestParam(value = "filename") String filename) {
        log.info("开始读取excel文件");
        String fileFullName = filepath + filename;
        List<Good> goodsList = ExcelUtils.readExcel(fileFullName, Good.class);
        log.info("文件读取结束");
        return goodsList.toString();
    }

    /**
     * 生成商品列表
     *
     * @param dataMap 源数据列表
     */
    private List<Good> creatGoodsList(Map<Integer, Map<Integer, Object>> dataMap) {

        List<Good> goodsList = new ArrayList<>();
        int dataSize = dataMap.size();
        int dataIndex = 1;

        while (dataIndex < dataSize) {
            Good good = new Good();
            // 品名
            good.setName(dataMap.get(dataIndex).get(0).toString());
            // 最低价格
            good.setMixPrice(new Double(Double.parseDouble(
                    dataMap.get(dataIndex).get(1).toString())).intValue());
            // 最高价格
            good.setMaxPrice(new Double(Double.parseDouble(
                    dataMap.get(dataIndex).get(2).toString())).intValue());
            // 权重
            good.setWeight(new Double(Double.parseDouble(
                    dataMap.get(dataIndex).get(3).toString())).intValue());
            goodsList.add(good);
            dataIndex++;
        }
        return goodsList;
    }


}
