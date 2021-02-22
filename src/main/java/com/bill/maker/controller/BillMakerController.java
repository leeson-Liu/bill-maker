package com.bill.maker.controller;

import com.bill.maker.entity.Good;
import com.bill.maker.utils.ReadExcelFilesUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.bill.maker.data.GoodsData.GOOD_LIST;

@RestController("BillMakerController")
@RequestMapping("/bill")
@Slf4j
public class BillMakerController {

    public static String filepath = "D:\\file\\";

    @GetMapping("/helloWorld")
    public String hello(@RequestParam(value = "word") String word) {
        log.info("good init data size :{}", GOOD_LIST.size());
        return "ok " + word;
    }

    @GetMapping("/upload")
    public String upload(@RequestParam(value = "filename") String filename) {
        // public String upload(@RequestParam("mfile") MultipartFile mfile) {
        log.info("开始读取excel文件");
        String fileFullName = filepath + filename;
        ReadExcelFilesUtils readExcelFilesUtils = new ReadExcelFilesUtils(fileFullName);
        List<Good> goodsList = new ArrayList<Good>();
        try {
            Map<Integer, Map<Integer, Object>> dataMap = readExcelFilesUtils.readExcelContent();
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
        } catch (Exception e) {
            e.printStackTrace();
            log.error("e:{}", e.getMessage());
        }
        log.info("文件读取结束");
        return goodsList.toString();
    }


}
