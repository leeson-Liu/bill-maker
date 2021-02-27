package com.bill.maker.controller;

import com.bill.maker.entity.Bill;
import com.bill.maker.entity.Good;
import com.bill.maker.service.BillService;
import com.bill.maker.utils.ExcelUtils;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

//import com.bill.maker.utils.ReadExcelFilesUtils;

@RestController("BillMakerController")
@RequestMapping("/bill")
@Slf4j
public class BillMakerController {

    @Autowired
    private BillService billService;
    public static String FILEPATH = "D:\\file\\";
    // 账单最大商品种类
    public static int MAX_GOODS_NUM = 15;

    @GetMapping("/helloWorld")
    public String hello(@RequestParam(value = "word") String word) {
        return billService.helloWord(word);
    }

    @GetMapping("/make")
    public List<Good> hello(@RequestParam(value = "money") Integer money) {
        return billService.makeBill(money);
    }

    @GetMapping("/upload")
    public String upload(@RequestParam(value = "filename") String filename) {
        log.info("开始读取excel文件");
        String fileFullName = FILEPATH + filename;
        List<Good> goodsList = ExcelUtils.readExcel(fileFullName, Good.class);
        log.info("文件读取结束");
        log.info("创建映射数据");
        Bill bill = new Bill();
        bill.setGoodList(goodsList);
        Map<String, Object> dataMap = creatDataMap(bill);
        log.info("填充PDF模板");
        fillPdfTemplate(dataMap);
        log.info("打印数据");
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

    /**
     * 创建映射数据
     *
     * @param bill 账单信息
     */
    private Map<String, Object> creatDataMap(Bill bill) {
        List<Good> goodsList = bill.getGoodList();
        Map<String, String> dataMap = new HashMap();
        int textIndex = 2;
        for (int index = 0; index < goodsList.size(); index++) {
            if (index == MAX_GOODS_NUM) {
                break;
            }
            // 品名
            dataMap.put("fill_" + textIndex, goodsList.get(index).getName());
            textIndex++;
            // 单价
            dataMap.put("fill_" + textIndex, goodsList.get(index).getRealPrice().toString());
            textIndex++;
            // 数量
            dataMap.put("fill_"+textIndex,goodsList.get(index).getNum().toString());
            textIndex++;
            // 金额
            dataMap.put("fill_" + textIndex, goodsList.get(index).getTotalPrice().toString());
            textIndex++;
        }
        // 客户名称
        dataMap.put("fill_1", bill.getGustName());
        // 邮编号码
        dataMap.put("yubin", bill.getPostalCode());
        // 邮编号码
        dataMap.put("address", bill.getAddress());
        // 邮编号码
        dataMap.put("daihyoName", bill.getRepresentName());
        // 电话号码
        dataMap.put("tel", bill.getTelNo());
        // 时间
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        dataMap.put("time", sdf.format(new Date()));
        // 请求号码
        dataMap.put("requestNo", bill.getRequestNO());
        Map<String, Object> mappingMap = new HashMap();
        mappingMap.put("datemap",dataMap);
        return mappingMap;
    }

    /**
     * 填充PDF模板
     *
     * @param map 数据列表
     */
    private void fillPdfTemplate(Map<String, Object> map) {
        // 模板路径
        String templatePath = "D:\\file\\pdf\\請求書_表单.pdf";//原PDF模板
        // 生成的新文件路径
        String newPDFPath = "D:\\file\\pdf\\請求書.pdf";
        PdfReader reader;
        FileOutputStream out;
        ByteArrayOutputStream bos;
        PdfStamper stamper;
        try {
            // 字体设置
            BaseFont bf = BaseFont.createFont("C:/windows/fonts/simsun.ttc,1", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font FontChinese = new Font(bf, 12f, Font.BOLD);
            // 输出流
            out = new FileOutputStream(newPDFPath);
            // 读取pdf模板
            reader = new PdfReader(templatePath);
            bos = new ByteArrayOutputStream();
            stamper = new PdfStamper(reader, bos);
            AcroFields form = stamper.getAcroFields();
            Map<String, String> datemap = (Map<String, String>) map.get("datemap");
            form.addSubstitutionFont(bf);
            for (String key : datemap.keySet()) {
                String value = datemap.get(key);
                System.out.println(value + "*(*(*(**---：" + key);
                form.setField(key, value);
            }
            stamper.setFormFlattening(true);// 如果为false，生成的PDF文件可以编辑，如果为true，生成的PDF文件不可以编辑
            stamper.close();
            Document doc = new Document(PageSize.A4, 0, 0, 0, 0);
            Font font = new Font(bf, 32);
            PdfCopy copy = new PdfCopy(doc, out);
            doc.open();
            PdfImportedPage importPage = null;
            ///循环是处理成品只显示一页的问题
            for (int i = 1; i <= reader.getNumberOfPages(); i++) {
                importPage = copy.getImportedPage(new PdfReader(bos.toByteArray()), i);
                copy.addPage(importPage);
            }
            doc.close();
            System.err.println("生成pdf文件完成！");
        } catch (IOException e) {
            System.out.println(e);
        } catch (DocumentException e) {
            System.out.println(e);

        }
    }

}
