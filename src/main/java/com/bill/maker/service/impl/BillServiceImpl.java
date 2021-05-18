package com.bill.maker.service.impl;

import com.bill.maker.data.GoodsData;
import com.bill.maker.entity.BillWX;
import com.bill.maker.entity.BillZFB;
import com.bill.maker.entity.Good;
import com.bill.maker.service.BillService;
import com.bill.maker.utils.CsvUtils;
import com.bill.maker.utils.WeightRandom;
import com.itextpdf.text.Document;
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
import java.time.LocalDate;
import java.util.*;

import static com.bill.maker.data.GoodsData.*;

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
        //根据金额设定确定的size
        int listSize = -1;
        List<Good> result = new ArrayList<>();
        if (money <= 1200D) {
            //只生成一个物品
            Good good = getOneGood(money);
            good.setRealPrice(BigDecimal.valueOf(money));
            good.setNum(1);
            good.setTotalPrice(BigDecimal.valueOf(money));
            result.add(good);
            return result;
        }
        if (money > 1200 && money <= 3000) {
            //两个
            listSize = 2;
        } else if (money > 3000 && money <= 5000) {
            //三个四个
            listSize = 3;
        } else if (money > 5000 && money <= 10000) {
            //五个六个
            listSize = 5;
        }


        BigDecimal bigDecimalMoney = BigDecimal.valueOf(money);
        BigDecimal priceSum = BigDecimal.ZERO;
        BigDecimal remainingMoney = BigDecimal.ZERO;
        for (int i = 0; ; i++) {
            int num;
            Good good;
            if (result.size() == 0) {
                num = 1;
                Random random = new Random();
                int n = random.nextInt(GOOD_FIRST_LIST.size());
                good = GOOD_FIRST_LIST.get(n);
            } else if (result.size() == 1) {
                Random random = new Random();
                int n = random.nextInt(GOOD_FIRST_LIST.size());
                good = GOOD_FIRST_LIST.get(n);
                num = getNum(money, good.getWeight());
            } else {
                good = getRandomGood(0);
                num = getNum(money, good.getWeight());
            }

            if (i > 500000 || good == null) {
                log.error("AA问题数据 money:{},customerName:{}", money, customerName);
                return this.randomGoodList(money, customerName);
            }
            BigDecimal price = getRandomPrice(good.getMaxPrice(), good.getMinPrice());
            good.setRealPrice(price);
            good.setNum(num);
            BigDecimal totalPrice = price.multiply(BigDecimal.valueOf((double) num));
            good.setTotalPrice(totalPrice);
            if (totalPrice.add(priceSum).compareTo(bigDecimalMoney) <= 0) {
                result.add(good);
                priceSum = priceSum.add(totalPrice);
            }
            if (bigDecimalMoney.subtract(priceSum).compareTo(BigDecimal.valueOf(400D)) < 0 || result.size() == listSize) {
                remainingMoney = bigDecimalMoney.subtract(priceSum);
                break;
            }
        }

        //凑单
        if (remainingMoney.compareTo(ALL_GOOD_MIN_PRICE) > 0) {

            if (remainingMoney.compareTo(BigDecimal.valueOf(400D)) > 0) {
                Good good = result.get(0);
                BigDecimal leaveMoney = remainingMoney.add(good.getRealPrice());
                BigDecimal price;
                int num;
                BigDecimal totalPrice;
                for (int i = 0; ; i++) {
                    price = getRandomPrice(good.getMaxPrice(), good.getMinPrice());
                    num = leaveMoney.divide(good.getRealPrice(), 0, BigDecimal.ROUND_DOWN).intValue();

                    totalPrice = price.multiply(BigDecimal.valueOf((double) num));
                    if (leaveMoney.subtract(totalPrice).compareTo(BigDecimal.valueOf(400D)) < 0 && leaveMoney.subtract(totalPrice).compareTo(BigDecimal.ZERO) > 0) {
                        break;
                    }
                    if (i > 500000 || good == null) {
                        log.error("凑单问题数据 money:{},customerName:{}", money, customerName);
                        return this.randomGoodList(money, customerName);
                    }
                }
                if (num > 1) {
                    log.info("price{},num{}", price, num);
                }
                good.setNum(num);
                good.setTotalPrice(totalPrice);
                good.setRealPrice(price);
//                result.set(0, good);

                Random random = new Random();
                int n = random.nextInt(GOOD_LAST_LIST.size());
                Good good2 = GOOD_LAST_LIST.get(n);
                good2.setRealPrice(leaveMoney.subtract(totalPrice));
                good2.setNum(1);
                good2.setTotalPrice(leaveMoney.subtract(totalPrice));
                result.add(good2);
            } else {
                Random random = new Random();
                int n = random.nextInt(GOOD_LAST_LIST.size());
                Good good = GOOD_LAST_LIST.get(n);
                good.setRealPrice(remainingMoney);
                good.setNum(1);
                good.setTotalPrice(remainingMoney);
                result.add(good);
            }

        } else {
            Good good = result.get(0);
            good.setTotalPrice(good.getTotalPrice().add(remainingMoney));
            good.setRealPrice(good.getRealPrice().add(remainingMoney));
            result.set(0, good);
            log.info("补单 good:{}", result.get(0));
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
                        List<Good> goodList;
                        for (; ; ) {
                            goodList = this.randomGoodList(money, billWX.getCustomerName());
                            if (dataVail(goodList, money)) {
                                log.info("对了,customerName{},money{}", billWX.getCustomerName(), money);
                                break;
                            } else {
                                log.error("数据有误,customerName{},money{}", billWX.getCustomerName(), money);
                            }
                        }
                        billWX.setGoodList(goodList);
                        Integer no = ++GoodsData.BILL_NO;
                        billWX.setRequestNO(billWX.getPayTime().replaceAll("-", "_") + String.format("%05d", no));
                        Map<String, Object> dataMap = creatDataMap(billWX.getGoodList(), billWX.getCustomerName(), billWX.getRequestNO().replaceAll("_", ""), billWX.getMoney(), billWX.getPayTime());
                        String fileName = billWX.getPayTime() + "_" + dealCustomerNameForFileName(billWX.getCustomerName()) + "_" + billWX.getMoney() + "_" + no;
                        log.info("fileName:{},no:{}", fileName, no);
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
                        List<Good> goodList;
                        for (; ; ) {
                            goodList = this.randomGoodList(money, billZFB.getCustomerName());
                            if (dataVail(goodList, money)) {
                                log.info("对了,customerName{},money{}", billZFB.getCustomerName(), money);
                                break;
                            } else {
                                log.error("数据有误,customerName{},money{}", billZFB.getCustomerName(), money);
                            }
                        }

                        billZFB.setGoodList(goodList);
                        Integer no = ++GoodsData.BILL_NO;
                        billZFB.setRequestNO(billZFB.getPayTime().replaceAll("-", "_") + String.format("%05d", no));
                        Map<String, Object> dataMap = creatDataMap(billZFB.getGoodList(), billZFB.getCustomerName(), billZFB.getRequestNO().replaceAll("_", ""), billZFB.getMoney(), billZFB.getPayTime());
                        String fileName = billZFB.getPayTime() + "_" + dealCustomerNameForFileName(billZFB.getCustomerName()) + "_" + billZFB.getMoney() + "_" + no;
                        log.info("fileName:{},no:{}", fileName, no);
                        fillPdfTemplate(dataMap, fileName.replaceAll(" ", "").replaceAll("/", "_"));
                    }
                }
            }
        } catch (Exception e) {
            log.error("deal file error cause:{} message{}", e.getCause(), e.getMessage());
        }
        return "ok";
    }

    private boolean dataVail(List<Good> goodList, double money) {
        BigDecimal total = BigDecimal.ZERO;
        for (Good good : goodList) {
            total = total.add(good.getTotalPrice());
        }
        return total.compareTo(BigDecimal.valueOf(money)) == 0;
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
//        log.info("pdf customerName:{}", customerName);
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
        String templatePath = "src/main/resources/file/請求書_表单.pdf";//原PDF模板
        // 生成的新文件路径
        String newPDFPath = "D:\\file\\pdf\\done\\" + fileName + ".pdf";
        PdfReader reader = null;
        FileOutputStream out = null;
        ByteArrayOutputStream bos = null;
        PdfStamper stamper = null;
        try {
            // 字体设置
            BaseFont bf = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
//             BaseFont bf = BaseFont.createFont("c://windows//fonts//simsun.ttc,1", BaseFont.IDENTITY_H,false);
//            Font fontZH = new Font(bf, 12, Font.NORMAL);
//            Font font = new Font(bf, 32);
            ArrayList<BaseFont> fontList = new ArrayList<BaseFont>(1);
            fontList.add(bf);
            // 输出流
            out = new FileOutputStream(newPDFPath);
            // 读取pdf模板
            reader = new PdfReader(templatePath);
            bos = new ByteArrayOutputStream();
            stamper = new PdfStamper(reader, bos);
            AcroFields form = stamper.getAcroFields();
            Map<String, String> datemap = (Map<String, String>) map.get("datemap");
            form.setSubstitutionFonts(fontList);
            for (String key : datemap.keySet()) {
                String value = datemap.get(key);
                form.setField(key, value);
            }
            stamper.setFormFlattening(true);// 如果为false，生成的PDF文件可以编辑，如果为true，生成的PDF文件不可以编辑
            stamper.close();
            Document doc = new Document(PageSize.A4, 0, 0, 0, 0);
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

    private Integer getNum(Double money, Integer weight) {
        int times;
        if (money > 100000 && money < 150000) {
            times = money.intValue() / 10000 + 1;
        } else if (money >= 150000 && money < 250000) {
            times = money.intValue() / 20000 + 1;
        } else if (money >= 250000) {
            times = money.intValue() / 30000 + 1;
        } else {
            times = money.intValue() / 5000 + 1;
        }

        int max = 10 * times;
        int min = times;
        int result = (int) (Math.random() * (max - min) + min) * weight / 150 + 1;
        if (money >= 10000 && result > 5) {
            return 5;
        } else {
            return result;
        }
    }

    private Good getRandomGood(Integer weight) {
        List<Pair<Good, Integer>> pairList = new ArrayList<>();
        for (Good good : GOOD_LIST) {
            Pair<Good, Integer> pair = new ImmutablePair<>(good, good.getWeight());
            pairList.add(pair);
        }
        WeightRandom<Good, Integer> weightRandom = new WeightRandom(pairList);
        Good randomGood = null;
        for (int i = 0; i < 100000; i++) {
            randomGood = weightRandom.random();
            if (randomGood.getWeight() >= weight) {
                break;
            }
        }
        Good good = new Good();
        BeanUtils.copyProperties(randomGood, good);
        return good;
    }

    private Good getOneGood(Double money) {
        List<Pair<Good, Integer>> pairList = new ArrayList<>();
        for (Good good : GOOD_LIST) {
            Pair<Good, Integer> pair = new ImmutablePair<>(good, good.getWeight());
            pairList.add(pair);
        }
        WeightRandom<Good, Integer> weightRandom = new WeightRandom(pairList);
        Good randomGood = null;
        for (int i = 0; i < 100000; i++) {
            randomGood = weightRandom.random();
            if (Double.valueOf(randomGood.getMaxPrice()) >= money && Double.valueOf(randomGood.getMinPrice()) <= money) {
                break;
            }
        }
        Good good = new Good();
        BeanUtils.copyProperties(randomGood, good);
        return good;
    }


    private String dealCustomerNameForFileName(String customerName) {
        return customerName.replaceAll("\\?", "_").replaceAll("\\*", "_").replaceAll("#", "_").replaceAll(" ", "").replaceAll("\\\\", "_");
    }

}
