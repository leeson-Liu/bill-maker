package com.bill.maker.controller;

import com.bill.maker.data.GoodsData;
import com.bill.maker.entity.Bill;
import com.bill.maker.entity.Good;
import com.bill.maker.service.BillService;
import com.bill.maker.utils.CsvUtils;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;


@RestController("BillMakerController")
@RequestMapping("/bill")
@Slf4j
public class BillMakerController {

    @Autowired
    private BillService billService;
    public static String FILEPATH = "D:\\file\\";
    // 账单最大商品种类
    public static int MAX_GOODS_NUM = 15;
    public static String UPLOAD_FOLD_PATH = "D:\\file\\upload\\";
    public static String BILL_DONE_FOLD_PATH = "D:\\file\\pdf\\done\\";

    @GetMapping("/make")
    public List<Good> makeBill(@RequestParam(value = "money") Double money) {
        return billService.makeBill(money);
    }

    @GetMapping("/upload")
    public String upload(@RequestParam(value = "filename") String filename) throws Exception {
        log.info("csv");
        File uploadFileList = new File(UPLOAD_FOLD_PATH);

        File[] fileList = uploadFileList.listFiles();
        assert fileList != null;
        for (File file : fileList) {
            InputStream inputStream = new FileInputStream(file);
            MultipartFile multipartFile = new MockMultipartFile(file.getName(), inputStream);
            List<Bill> billList = CsvUtils.readCsv(multipartFile, Bill.class);
            for (Bill bill : billList) {
                if ("收入".equals(bill.getBillType())) {
                    bill.setPayTime(bill.getPayTime().substring(0, bill.getPayTime().indexOf(" ")));
                    Double money = Double.valueOf(bill.getMoney().replaceAll("¥", "").replaceAll(",", "").trim());
                    List<Good> goodList = billService.makeBill(money);
                    bill.setGoodList(goodList);
                    Integer no = ++GoodsData.BILL_NO;
                    bill.setRequestNO(String.format("%08d", no));
                    Map<String, Object> dataMap = creatDataMap(bill);
                    log.info("填充PDF模板");
                    String fileName = bill.getCustomerName() + "_" + bill.getPayTime();
                    fillPdfTemplate(dataMap, fileName.replaceAll(" ", "").replaceAll("/","_"));
                    log.info("打印数据");
                }
            }
        }
        return "ok";
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
            dataMap.put("fill_" + textIndex, goodsList.get(index).getNum().toString());
            textIndex++;
            // 金额
            dataMap.put("fill_" + textIndex, goodsList.get(index).getTotalPrice().toString());
            textIndex++;
        }
        // 客户名称（代表社员）
        dataMap.put("fill_1", bill.getGustName());
        // 邮编号码
        dataMap.put("yubin", bill.getPostalCode());
        // 邮编号码
        dataMap.put("address", bill.getAddress());
        // 邮编号码
        dataMap.put("daihyoName", bill.getCustomerName());
        // 电话号码
        dataMap.put("tel", bill.getTelNo());
        // 时间
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        dataMap.put("time", sdf.format(new Date()));
        // 请求号码
        dataMap.put("requestNo", bill.getRequestNO());
        Map<String, Object> mappingMap = new HashMap();
        mappingMap.put("datemap", dataMap);
        return mappingMap;
    }

    /**
     * 填充PDF模板
     *
     * @param map 数据列表
     */
    private void fillPdfTemplate(Map<String, Object> map, String fileName) throws IOException, DocumentException {
        // 模板路径
        String templatePath = "D:\\file\\pdf\\請求書_表单.pdf";//原PDF模板
        // 生成的新文件路径
        String newPDFPath = "D:\\file\\pdf\\done\\" + fileName + ".pdf";
        String aaa = "D:\\file\\pdf\\done\\";
        PdfReader reader = null;
        FileOutputStream out = null;
        ByteArrayOutputStream bos = null;
        PdfStamper stamper = null;
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
            log.error("error:{}__{}", e.getCause(), e.getMessage());
        } finally {
            if (reader != null) {
                reader.close();
            }
            if (out != null) {
                out.close();
            }
            if (bos != null) {
                bos.close();
            }
            if (stamper != null) {
                stamper.close();
            }
        }
    }

    private static void createFile(File file, long length) throws IOException {
        RandomAccessFile r = null;
        try {
            r = new RandomAccessFile(file, "rw");
            r.setLength(length);
        } finally {
            if (r != null) {
                r.close();
            }
        }
    }


}
