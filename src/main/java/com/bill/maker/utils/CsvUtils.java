package com.bill.maker.utils;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.opencsv.RFC4180Parser;
//import com.paasoo.utils.ExceptionTools;
//import com.paasoo.web.common.annotation.ExcelColumn;
//import com.paasoo.web.common.constants.WebGlobalConst;
//import com.paasoo.web.common.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.CellType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author WangXuGuang
 * @since 2018/8/22
 */
@Slf4j
@Component
public class CsvUtils {

    private static final String SUFFIX = ".csv";
    private static final String LINE_BREAK = "\n";
//    private final ByteCounter counter;
//
//    @Autowired
//    public CsvUtils(ByteCounter counter) {
//        this.counter = counter;
//    }

    /**
     * 是否过滤本行
     *
     * @param row                    Csv记录行
     * @param notEmptyFieldIndexList 不允许为空的字段，如果其中一个字段为空，则忽略
     * @return true：过滤本行，false，不过滤本行
     */
    public static boolean isFilterRow(String line, String[] row, List<Integer> notEmptyFieldIndexList) {
        boolean isFilter = false;
        if (null == row) {
            log.debug("row or notEmptyFieldList is ");
            return true;
        }
        if (CollectionUtils.isEmpty(notEmptyFieldIndexList)) {
            //不做过滤操作
            return false;
        }
        if (notEmptyFieldIndexList.get(notEmptyFieldIndexList.size() - 1) > row.length - 1) {
            log.warn("row length is less than notEmptyFieldList :{} , line:{} , ", JSON.toJSONString(notEmptyFieldIndexList), line);
            return true;
        }
        List<Boolean> booleanList = Lists.newArrayList();
        for (Integer idx : notEmptyFieldIndexList) {
            if (StringUtils.isBlank(row[idx])) {
                booleanList.add(true);
            } else {
                booleanList.add(false);
            }
        }
        if (!booleanList.contains(Boolean.FALSE)) {
            isFilter = true;
        }
        return isFilter;
    }

    /**
     * @param file           file
     * @param cls            cls
     * @param charsetName    charsetName
     * @param strictMatching boolean  true-代表每一类都要与cls一样; false 则会略过
     * @param <T>
     * @return
     */
    private static <T> List<T> csvInCommon(MultipartFile file, Class<T> cls, String charsetName, List<String> notEmptyFieldList,
                                           boolean strictMatching) {
        List<String> dataList = new ArrayList<String>();
        List<T> dataListAll = new ArrayList<>();
        //不允许为空列的索引
        List<Integer> notEmptyFieldIndexList = new ArrayList<>();
        boolean bReadNext = false;
        String wholeLine = "";
        BufferedReader br = null;
        RFC4180Parser parser = new RFC4180Parser();
        try {
            br = new BufferedReader(new InputStreamReader(file.getInputStream(), charsetName));
            String line = "";
            char ch = 65533;
            int headerSize = 0;
            boolean isHeader = true;
            while ((line = br.readLine()) != null) {
                //去掉bom
                if (line.startsWith("\uFEFF")) {
                    line = line.replace("\uFEFF", "");
                }
                //去掉€ 符号
                line = line.replace(Character.toString(ch), " ").replace("€", " ");
                if (bReadNext) {
                    line = wholeLine + line;
                    bReadNext = false;
                }


                if (countChar(line, '"', 0) % 2 == 1) {
                    wholeLine = line;
                    bReadNext = true;
                    continue;
                }


                String[] row = parser.parseLine(line);
                if (isHeader) {
                    headerSize = row.length;
                    isHeader = false;
                    //处理索引列
                    if (CollectionUtils.isNotEmpty(notEmptyFieldList)) {
                        for (int i = 0; i < row.length; i++) {
                            if (notEmptyFieldList.contains(row[i])) {
                                notEmptyFieldIndexList.add(i);
                            }
                        }
                    }
                }
                //行数不同直接略过
                if (row.length != headerSize) {
                    wholeLine = line;
                    bReadNext = true;
                    continue;
                }
                if (!isFilterRow(line, row, notEmptyFieldIndexList)) {
                    //添加没有被过滤的行
                    dataList.add(line);
                }
                wholeLine = "";
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            if (br != null) {
                try {
                    br.close();
                    br = null;
                } catch (IOException e) {
                    log.error("error={}", e);
                }
            }
        }
        Map<String, List<Field>> classMap = new HashMap<>();
        Field[] fields = cls.getDeclaredFields();
        for (Field field : fields) {
            ExcelColumn annotation = field.getAnnotation(ExcelColumn.class);
            if (annotation != null) {
                String value = annotation.value().trim().toLowerCase();
                if (!classMap.containsKey(value)) {
                    classMap.put(value, new ArrayList<>());
                }
                field.setAccessible(true);
                classMap.get(value).add(field);
            }
        }
        Map<Integer, List<Field>> reflectionMap = new HashMap<>();
        for (int i = 0; i < dataList.size(); i++) {
            try {
                String[] title = parser.parseLine(dataList.get(i));
                if (i == 0) {
                    for (int j = 0; j < title.length; j++) {
                        List<Field> fieldList = classMap.get(title[j].trim().toLowerCase());
                        if (strictMatching) {
                            if (CollectionUtils.isEmpty(fieldList)) {
                                return Lists.newArrayList();
                            }
                        }
                        reflectionMap.put(j, fieldList);
                    }
                } else {
                    try {
                        T t = cls.newInstance();
                        for (int j = 0; j < title.length; j++) {
                            if (reflectionMap.containsKey(j)) {
                                List<Field> fieldList = reflectionMap.get(j);
                                if (fieldList == null) {
                                    continue;
                                }
                                for (Field field : fieldList) {
                                    try {
                                        if (field.getType().equals(double.class)) {
                                            field.set(t, Double.valueOf(title[j].trim()));
                                        }if (field.getType().equals(int.class)) {
                                            field.set(t, Integer.valueOf(title[j].trim()));
                                        } else {
                                            field.set(t, title[j].trim());
                                        }
                                    } catch (Exception e) {
                                        log.error("excel.setField.error={}", e);
                                    }

                                }
                            }
                        }
                        dataListAll.add(t);
                    } catch (Exception e) {
                        log.error("excel.addRow.error={}", e);
                    }
                }
            } catch (IOException e) {
                log.error("error={}", e);
            }
        }
        return dataListAll;
    }

    /**
     * 获取excel数据 将之转换成bean  上传格式：UTF-8
     */
    public static <T> List<T> readCsv(MultipartFile file, Class<T> cls) {
        return csvInCommon(file, cls, "UTF-8", null, false);
    }

    public static <T> List<T> readCsv(MultipartFile file, Class<T> cls, String charsetName) {
        return csvInCommon(file, cls, charsetName, null, false);
    }


    public static <T> List<T> readCharsetCsv(MultipartFile file, Class<T> cls) {
        return readCharsetCsv(file, cls, null, false);
    }

    /**
     * 获取excel数据 将之转换成bean
     */
    public static <T> List<T> readCharsetCsv(MultipartFile file, Class<T> cls, List<String> notEmptyFieldList, boolean strictMatching) {
        InputStream inputStream = null;
        try {
            inputStream = file.getInputStream();
            String charsetName = getFileCharset(inputStream);
            return csvInCommon(file, cls, charsetName, notEmptyFieldList, strictMatching);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Lists.newArrayList();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    /**
     * 获取excel数据 将之转换成bean  上传格式：GB2312
     */
    public static <T> List<T> readCsvGB2312(MultipartFile file, Class<T> cls) {
        return csvInCommon(file, cls, "GB2312", null, false);
    }

    public static String getFileCharset(InputStream inputStream) throws Exception {
        //默认GBK
        String charset = "GBK";
        byte[] first3Bytes = new byte[3];
        try (BufferedInputStream bis = new BufferedInputStream(inputStream)) {
            bis.mark(0);
            int read = bis.read(first3Bytes, 0, 3);
            // ANSI
            if (read == -1) {
                return charset;
            }
            // Unicode
            if (first3Bytes[0] == (byte) 0xFF && first3Bytes[1] == (byte) 0xFE) {
                return StandardCharsets.UTF_16LE.name();
            }
            // Unicode big endian
            if (first3Bytes[0] == (byte) 0xFE && first3Bytes[1] == (byte) 0xFF) {
                return StandardCharsets.UTF_16BE.name();
            }
            // UTF-8
            if (first3Bytes[0] == (byte) 0xEF && first3Bytes[1] == (byte) 0xBB && first3Bytes[2] == (byte) 0xBF) {
                return StandardCharsets.UTF_8.name();
            }
            bis.reset();
            while ((read = bis.read()) != -1) {
                if (read >= 0xF0) {
                    break;
                }
                // 单独出现BF以下的，也算是GBK
                if (0x80 <= read && read <= 0xBF) {
                    break;
                }
                if (0xC0 <= read && read <= 0xDF) {
                    read = bis.read();
                    // 双字节 (0xC0 - 0xDF)
                    if (0x80 <= read && read <= 0xBF) {
                        continue;
                    }
                    break;
                }
                if (0xE0 <= read && read <= 0xEF) {
                    read = bis.read();
                    if (0x80 <= read && read <= 0xBF) {
                        read = bis.read();
                        if (0x80 <= read && read <= 0xBF) {
                            charset = StandardCharsets.UTF_8.name();
                        }
                    }
                    break;
                }
            }
            return charset;
        } catch (Exception e) {
            log.error("get csv file charset error:{}", e);
            throw new Exception("get csv file charset error");
        }
    }

    private static int countChar(String str, char c, int start) {
        int i = 0;
        int index = str.indexOf(c, start);
        if (index == -1) {
            return i;
        } else {
            return countChar(str, c, index + 1) + 1;
        }
    }


    public List<String> generateCSVList(List<String[]> dataList) {
        List<String> result = new ArrayList<>();
        RFC4180Parser parser = new RFC4180Parser();
        for (String[] line : dataList) {
            String[] line1 =
                Arrays.stream(line).map(v -> v + "").collect(Collectors.toList()).toArray(new String[Arrays.stream(line).map(v -> v + "")
                    .collect(Collectors.toList()).size()]);
            String[] convertedLine = Stream.of(line1).map(Strings::nullToEmpty).toArray(String[]::new);
            result.add(parser.parseToLine(convertedLine, false) + LINE_BREAK);
        }
        return result;
    }

    //dnd专用  这里处理大量数据，不做判空操作，节约性能
    public List<String> generateDndCSVList(List<String[]> dataList) {
        List<String> result = new ArrayList<>();
        Iterator<String[]> iterator = dataList.iterator();
        while (iterator.hasNext()) {
            StringBuilder sb = new StringBuilder();
            String[] data = iterator.next();
            sb.append(Joiner.on(",").join(data)).append("\n");
            result.add(sb.toString());
        }
        return result;
    }

    /**
     * typeEnum
     */
    public static enum typeEnum {
        //sms
        DATA_SEARCH("SMS_report_"),
        //mms
        MMS_DATA_SEARCH("MMS_report_"),
        //sip
        SIP_DATA_SEARCH("SIP_report_"),
        //hlr
        HLR_DATA_SEARCH("Phone Number Format API_report_report_"),
        //mo
        MO_DATA_SEARCH("Numbers_report_");


        private String fileName;

        typeEnum(String fileName) {
            this.fileName = fileName;
        }
    }

    public static ByteArrayOutputStream getCsvFileToZip(String zipName, Map<String, byte[]> byteMap) {
        ZipOutputStream zipOutputStream = null;
        ByteArrayOutputStream outputStream = null;
        try {
            outputStream = new ByteArrayOutputStream();
            zipOutputStream = new ZipOutputStream(outputStream);
            for (Map.Entry<String, byte[]> entry : byteMap.entrySet()) {
                byte[] bytes = entry.getValue();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                String fileName = entry.getKey();
                baos.write(bytes);
                compressFileToZipStream(zipOutputStream, baos, fileName);
            }
        } catch (IOException e) {
            log.error("ZipFile.error=", e);
        } finally {
            try {
                if (null != zipOutputStream) {
                    zipOutputStream.flush();
                    zipOutputStream.close();
                }
            } catch (IOException e) {
                log.error("ZipFile.error=", e);
            }
        }
        return outputStream;
    }

    /**
     * 压缩文件
     */
    private static void compressFileToZipStream(ZipOutputStream zipOutputStream, ByteArrayOutputStream outputStream, String filename) {
        byte[] buf = new byte[1024];
        ByteArrayInputStream is = null;
        BufferedInputStream bis = null;
        try {
            byte[] content = outputStream.toByteArray();
            is = new ByteArrayInputStream(content);
            bis = new BufferedInputStream(is);
            zipOutputStream.putNextEntry(new ZipEntry(filename));
            int len;
            while ((len = bis.read(buf)) > 0) {
                zipOutputStream.write(buf, 0, len);
            }
        } catch (IOException e) {
            log.error("compressFileToZipStream.error=", e);
        } finally {
            try {
                zipOutputStream.closeEntry();
                if (null != bis) {
                    bis.close();
                }
                if (null != is) {
                    is.close();
                }
                outputStream.close();
            } catch (IOException e) {
                log.error("compressFileToZipStream.error=", e);
            }
        }
    }

}

