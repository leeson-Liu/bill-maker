package com.bill.maker.service.impl;

import com.bill.maker.data.GoodsData;
import com.bill.maker.entity.BillWX;
import com.bill.maker.entity.BillZFB;
import com.bill.maker.entity.Good;
import com.bill.maker.service.BillService;
import com.bill.maker.utils.CsvUtils;
import com.bill.maker.utils.WeightRandom;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.BeanUtils;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

import static com.bill.maker.data.GoodsData.GOOD_LIST;
import static com.bill.maker.data.GoodsData.ALL_GOOD_MIN_PRICE;

/**
 * @author liubin
 * @create 2021-02-24 19:09
 * @desc
 **/
@Service
@Slf4j
public class BillServiceImpl implements BillService {

    public static String FILEPATH = "D:\\file\\";
    public static String UPLOAD_FOLD_PATH = "D:\\file\\upload\\";
    public static String BILL_DONE_FOLD_PATH = "D:\\file\\pdf\\done\\";
    // 账单最大商品种类
    public static int MAX_GOODS_NUM = 15;

    // 地址
    private static String ADDRESS = "福岡県福岡市東区三苫7-16-11 ラメール三苫102号 ";
    // 邮编号码
    private static String POSTALCODE = "811-0201";
    // 代表社员
    private static String CUSTOMERNAME = "王　学実";
    // 电话
    private static String TELNO = "08039944428";

    // 日元汇款
    private static double EXCHANGE_RATE = 0.065;

    @Override
    public List<Good> randomGoodList(Double money, String customerName) {
        List<Good> result = new ArrayList<>();
        BigDecimal bigDecimalMoney = BigDecimal.valueOf(money);
        BigDecimal priceSum = BigDecimal.ZERO;
        BigDecimal remainingMoney = BigDecimal.ZERO;
        for (int i = 0; ; i++) {
            if (i == 500000) {
                log.error("问题数据 money:{},customerName:{}", money, customerName);
                break;
            }
            Good good = getRandomGood();
            BigDecimal price = getRandomPrice(good.getMaxPrice(), good.getMixPrice());
            good.setRealPrice(price);
            int num;
            if (result.size() == 0) {
                num = 1;
            } else {
                num = getNum(money);
            }
            good.setNum(num);
            BigDecimal totalPrice = price.multiply(BigDecimal.valueOf((double) num));
            good.setTotalPrice(totalPrice);
            if (totalPrice.add(priceSum).compareTo(bigDecimalMoney) <= 0) {
                result.add(good);
                priceSum = priceSum.add(totalPrice);
            }
            if (bigDecimalMoney.subtract(priceSum).compareTo(BigDecimal.valueOf(500D)) < 0) {
                remainingMoney = bigDecimalMoney.subtract(priceSum);
                break;
            }
        }

        //凑单
        if (remainingMoney.compareTo(ALL_GOOD_MIN_PRICE) > 0) {
            for (; ; ) {
                Good good = getRandomGood();
                if (BigDecimal.valueOf(good.getMixPrice()).compareTo(remainingMoney) < 0 && remainingMoney.compareTo(BigDecimal.valueOf(Double.valueOf(good.getMaxPrice()))) < 0) {
                    good.setRealPrice(remainingMoney);
                    good.setNum(1);
                    good.setTotalPrice(remainingMoney);
                    result.add(good);
                    break;
                }
            }
        } else {
            log.info("凑单触发: remainingMoney:{}", remainingMoney);
            Good good = result.get(0);
            log.info("凑单前 good:{}", result.get(0));
            good.setTotalPrice(good.getTotalPrice().add(remainingMoney));
            good.setRealPrice(good.getRealPrice().add(remainingMoney));
            result.set(0, good);
            log.info("凑单前 good:{}", result.get(0));
        }


        return result;
    }

    @Override
    public String uploadWX() {
        File uploadFileList = new File(UPLOAD_FOLD_PATH);
        File[] fileList = uploadFileList.listFiles();
        assert fileList != null;
        try {
            for (File file : fileList) {
                InputStream inputStream = new FileInputStream(file);
                MultipartFile multipartFile = new MockMultipartFile(file.getName(), inputStream);
                List<BillWX> billWXList = CsvUtils.readCsv(multipartFile, BillWX.class);
                for (BillWX billWX : billWXList) {
                    if ("收入".equals(billWX.getBillType())) {
                        String dataString = billWX.getPayTime().substring(0, billWX.getPayTime().indexOf(" "));
                        String[] aa = dataString.split("/");
                        LocalDate date = LocalDate.of(Integer.parseInt(aa[0]), Integer.parseInt(aa[1]), Integer.parseInt(aa[2]));
                        billWX.setPayTime(date.toString());
                        double money = Double.parseDouble(billWX.getMoney().replaceAll("¥", "").replaceAll(",", "").trim());
                        if (money <= 50) {
                            continue;
                        }
                        List<Good> goodList = this.randomGoodList(money, billWX.getCustomerName());
                        billWX.setGoodList(goodList);
                        Integer no = ++GoodsData.BILL_NO;
                        billWX.setRequestNO(billWX.getPayTime().replaceAll("-", "_") + String.format("%05d", no));
                        Map<String, Object> dataMap = creatDataMap(billWX.getGoodList(), billWX.getCustomerName(), billWX.getRequestNO().replaceAll("_", ""), billWX.getMoney(), billWX.getPayTime());
                        String fileName = billWX.getPayTime() + "_" + billWX.getCustomerName().replaceAll("\\*", "_") + billWX.getRequestNO();
                        log.info("fileName{}", fileName);
                        fillPdfTemplate(dataMap, fileName.replaceAll(" ", "").replaceAll("/", "_"));

                    }
                }
            }
        } catch (Exception e) {
            log.error("list error cause:{} message{}", e.getCause(), e.getMessage());
        }
        return "ok";
    }

    @Override
    public String uploadZFB() {
        File uploadFileList = new File(UPLOAD_FOLD_PATH);
        File[] fileList = uploadFileList.listFiles();
        assert fileList != null;
        try {
            for (File file : fileList) {
                InputStream inputStream = new FileInputStream(file);
                MultipartFile multipartFile = new MockMultipartFile(file.getName(), inputStream);
                List<BillZFB> billZFBList = CsvUtils.readCsv(multipartFile, BillZFB.class);
                for (BillZFB billZFB : billZFBList) {
                    double money;
                    if (StringUtils.isNotEmpty(billZFB.getMoney()) && !"转账到卡".equals(billZFB.getWay())) {
                        money = Double.parseDouble(billZFB.getMoney().replaceAll("¥", "").replaceAll(",", "").trim());
                    } else {
                        continue;
                    }
                    if (BigDecimal.valueOf(money).compareTo(ALL_GOOD_MIN_PRICE) > 0) {
                        billZFB.setPayTime(billZFB.getPayTime().substring(0, billZFB.getPayTime().indexOf(" ")));
                        List<Good> goodList = this.randomGoodList(money, billZFB.getCustomerName());
                        billZFB.setGoodList(goodList);
                        Integer no = ++GoodsData.BILL_NO;
                        billZFB.setRequestNO(billZFB.getPayTime().replaceAll("-", "_") + String.format("%05d", no));
                        Map<String, Object> dataMap = creatDataMap(billZFB.getGoodList(), billZFB.getCustomerName(), billZFB.getRequestNO().replaceAll("_", ""), billZFB.getMoney(), billZFB.getPayTime());
                        String fileName = billZFB.getPayTime() + "_" + billZFB.getCustomerName().replaceAll("\\*", "_") + billZFB.getRequestNO();
                        log.info("fileName{}", fileName);
                        fillPdfTemplate(dataMap, fileName.replaceAll(" ", "").replaceAll("/", "_"));
                    }
                }
            }
        } catch (Exception e) {
            log.error("deal file error cause:{} message{}", e.getCause(), e.getMessage());
        }
        return "ok";
    }

    /**
     * @param goodsList
     * @param customerName
     * @param billNo
     * @param money
     * @return
     */
    private Map<String, Object> creatDataMap(List<Good> goodsList, String customerName, String billNo, String money, String payTime) {
        Map<String, String> dataMap = new HashMap<>();
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

        dataMap.put("fill_1", customerName);
        dataMap.put("yubin", POSTALCODE);
        dataMap.put("address", ADDRESS);
        dataMap.put("daihyoName", CUSTOMERNAME);
        dataMap.put("tel", TELNO);
        // 时间
        dataMap.put("time", payTime.replaceAll("-", "/"));
        // 请求号码
        dataMap.put("requestNo", billNo);
        // 请求金额(元)
        dataMap.put("seikyuKingaku_gen", money);
        // 请求金额(円)
        double requestMoneyCH = Double.parseDouble(money.replaceAll("¥", "").replaceAll(",", ""));
        int moneyJP = (int) (requestMoneyCH / EXCHANGE_RATE);
        DecimalFormat df = new DecimalFormat("#,###");
        dataMap.put("seikyuKingaku_en", "¥" + df.format(moneyJP));
        Map<String, Object> mappingMap = new HashMap<>();
        mappingMap.put("datemap", dataMap);
        return mappingMap;
    }

    /**
     * 填充PDF模板
     *
     * @param map 数据列表
     */
    private void fillPdfTemplate(Map<String, Object> map, String fileName) {
        // 模板路径
        String templatePath = "D:\\file\\pdf\\請求書_表单.pdf";//原PDF模板
        // 生成的新文件路径
        String newPDFPath = "D:\\file\\pdf\\done\\" + fileName + ".pdf";
        PdfReader reader = null;
        FileOutputStream out = null;
        ByteArrayOutputStream bos = null;
        PdfStamper stamper = null;
        try {
            // 字体设置
            BaseFont bf = BaseFont.createFont("C:/windows/fonts/simsun.ttc,1", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
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
        } catch (Exception e) {
            log.error("make pdf error cause:{}message:{} fileName:{}", e.getCause(), e.getMessage(), fileName);
        } finally {
            if (reader != null) {
                reader.close();
            }
            try {
                if (out != null) {
                    out.close();
                }
                if (bos != null) {
                    bos.close();
                }
                if (stamper != null) {
                    stamper.close();
                }

            } catch (Exception e) {
                log.error("关闭流error  cause:{}message:{}", e.getCause(), e.getMessage());
            }

        }
    }


    private BigDecimal getRandomPrice(Integer max, Integer min) {
        int result = (int) (Math.random() * (max - min) + min);
        return BigDecimal.valueOf((double) (result / 10 * 10));
    }

    private Integer getNum(Double money) {
        int times;
        if (money > 100000 && money < 150000) {
            times = money.intValue() / 10000 + 1;
        } else if (money >= 150000 && money < 250000) {
            times = money.intValue() / 20000 + 1;
        } else if (money >= 250000) {
            times = money.intValue() / 30000 + 1;
        } else {
            times = money.intValue() / 6000 + 1;
        }

        int max = 7 * times;
        int min = times;
        return (int) (Math.random() * (max - min) + min);
    }

    private Good getRandomGood() {
        List<Pair<Good, Integer>> pairList = new ArrayList<>();
        for (Good good : GOOD_LIST) {
            Pair<Good, Integer> pair = new ImmutablePair<>(good, good.getWeight());
            pairList.add(pair);
        }
        WeightRandom<Good, Integer> weightRandom = new WeightRandom(pairList);
        Good randomGood = weightRandom.random();
        Good good = new Good();
        BeanUtils.copyProperties(randomGood, good);
        return good;
    }


}
