package com.bill.maker.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 导出、导入Excel工具
 *
 * @author lezc
 */
@Slf4j
@Component
public final class ExcelUtils {

    public static final DataFormatter DF = new DataFormatter();

    /**
     * 读取Excel数据
     *
     * @param file    文件
     * @param postfix 后缀
     * @return
     */
    public static List<String[]> readExcel(MultipartFile file, String postfix) {
        List<String[]> dataList = new ArrayList<>();
        InputStream in = null;
        if (file.isEmpty() || file.getSize() < 0) {
            return dataList;
        }
        try {
            if (postfix.endsWith("xlsx")) {
                in = file.getInputStream();
            } else if (postfix.endsWith("xls")) {
                in = new BufferedInputStream(file.getInputStream());
            } else {
                return null;
            }
            /*List<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
            ExcelListener excelListener = new ExcelListener(data);
            EasyExcelFactory.readBySax(in, new com.alibaba.excel.metadata.Sheet(1, 0), excelListener);
            dataList = data.stream().map(row -> {
                return row.toArray(new String[]{});
            }).collect(Collectors.toList());*/

            Workbook workbook;
            if (postfix.endsWith("xlsx")) {
                workbook = new XSSFWorkbook(in);
            } else if (postfix.endsWith("xls")) {
                workbook = new HSSFWorkbook(in);
            } else {
                return null;
            }

            Sheet dataTypeSheet = workbook.getSheetAt(0);
            Iterator<Row> iterator = dataTypeSheet.iterator();
            while (iterator.hasNext()) {
                Row currentRow = iterator.next();
                Iterator<Cell> cellIterator = currentRow.iterator();
                List<String> list = new ArrayList<>();
                while (cellIterator.hasNext()) {
                    Cell currentCell = cellIterator.next();
                    String cellString = getStringValueFromCell(currentCell);
                    if (StringUtils.isNotEmpty(cellString)) {
                        list.add(cellString);
                    }

                }
                String[] arr = new String[list.size()];
                if (arr.length > 0) {
                    dataList.add(list.toArray(arr));
                }
            }
            workbook.close();
        } catch (IOException e) {
            log.error("excel.read.IOException={}", e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    log.error("excel.read.IOException={}", e);
                }
            }
        }
        return dataList;
    }

    /**
     * 读取Excel数据
     *
     * @param inputStream 流
     * @param postfix     后缀
     * @return
     */
    public static List<String[]> readExcel(InputStream inputStream, String postfix) {
        List<String[]> dataList = new ArrayList<>();
        if (inputStream == null) {
            return dataList;
        }
        try {
            if (postfix.endsWith("xls")) {
                inputStream = new BufferedInputStream(inputStream);
            }
            Workbook workbook;
            if (postfix.endsWith("xlsx")) {
                workbook = new XSSFWorkbook(inputStream);
            } else if (postfix.endsWith("xls")) {
                workbook = new HSSFWorkbook(inputStream);
            } else {
                return dataList;
            }

            Sheet dataTypeSheet = workbook.getSheetAt(0);
            Iterator<Row> iterator = dataTypeSheet.iterator();
            while (iterator.hasNext()) {
                Row currentRow = iterator.next();
                Iterator<Cell> cellIterator = currentRow.iterator();
                List<String> list = new ArrayList<>();
                while (cellIterator.hasNext()) {
                    Cell currentCell = cellIterator.next();
                    list.add(getStringValueFromCell(currentCell));
                }
                String[] arr = new String[list.size()];
                dataList.add(list.toArray(arr));
            }
            workbook.close();
        } catch (IOException e) {
            log.error("excel.read.IOException={}", e);
        }
        return dataList;
    }


    private static byte[] processData(List<List<Object>> dataTypes) {
        String csvContent = "";
        String single = "";
        for (List<Object> oList : dataTypes) {
            for (Object o : oList) {
                if (o == null) {
                    single = "";
                } else {
                    single = o.toString();
                }
                if (single.contains(",")) {
                    single = "\"\t" + single + "\"";
                }
                single = single + ",";
                csvContent = csvContent + single;
            }
            csvContent = csvContent.substring(0, csvContent.length() - 1) + "\n";
        }
        return csvContent.getBytes();
    }

    /**
     * @param fileName  文件名
     * @param dataTypes 数据
     * @param response  响应
     * @throws Exception 异常
     */
    public static void writeCsv(String fileName, List<List<Object>> dataTypes, HttpServletResponse response) throws Exception {
        byte[] result = processData(dataTypes);
        String processFileName = processFileName(fileName);
        writeCsvCommon(result, response, processFileName);
    }

    private static String processFileName(String fileName) throws UnsupportedEncodingException {
        return URLEncoder.encode(fileName, "UTF-8").replace("+", "%20");
    }

    public static void writeCsvCommon(byte[] data, HttpServletResponse response, String fileName) throws IOException {
        OutputStream outputStream = response.getOutputStream();
        try {
            response.reset();
            response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".csv");
            response.setContentType("application/force-download");
            outputStream.write(data);
            outputStream.flush();
        } catch (IOException e) {
            log.error("exportCsvFile.error={}", e.getMessage());
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }


    private static boolean fieldNotMatch(boolean strictMatchingHeader, Map<Integer, List<Field>> reflectionMap, int filedsLength) {
        return strictMatchingHeader && (reflectionMap.size() == 0 || reflectionMap.size() < filedsLength);
    }

    public static <T> List<T> readExcel(String path, Class<T> cls) {
        return readExcel(path, cls, false);
    }

    /**
     * 获取excel数据 将之转换成bean
     *
     * @param path                 路径
     * @param cls                  对象类型
     * @param <T>                  对象类型
     * @param strictMatchingHeader 表头是否需要完全匹配
     * @return
     */
    public static <T> List<T> readExcel(String path, Class<T> cls, boolean strictMatchingHeader) {
        List<T> dataList = new ArrayList<>();
        Workbook workbook = null;
        try {
            FileInputStream is = new FileInputStream(new File(path));
            if (path.endsWith("xlsx")) {
                workbook = new XSSFWorkbook(is);
            }
            if (path.endsWith("xls")) {
                workbook = WorkbookFactory.create(is);
            }
            if (workbook != null) {
                //类映射
                Map<String, List<Field>> classMap = new HashMap<>();
                Field[] fields = cls.getDeclaredFields();
                for (Field field : fields) {
                    ExcelColumn annotation = field.getAnnotation(ExcelColumn.class);
                    if (annotation != null) {
                        String value = annotation.value().toLowerCase();
                        if (!classMap.containsKey(value)) {
                            classMap.put(value, new ArrayList<>());
                        }
                        field.setAccessible(true);
                        classMap.get(value).add(field);
                    }
                }
                Map<Integer, List<Field>> reflectionMap = new HashMap<>();
                Sheet sheet = workbook.getSheetAt(0);
                AtomicInteger ai = new AtomicInteger();
                for (Row row : sheet) {
                    int i = ai.incrementAndGet();
                    if (i == 1) {
                        //首行  提取注解
                        for (Cell cell : row) {
                            int j = cell.getColumnIndex();
                            String cellValue = getStringValueFromCell(cell).toLowerCase();
                            if (classMap.containsKey(cellValue)) {
                                reflectionMap.put(j, classMap.get(cellValue));
                            }
                        }
                        //如果表头都不匹配则退出
                        if (fieldNotMatch(strictMatchingHeader, reflectionMap, classMap.size())) {
                            return dataList;
                        }
                    } else {
                        try {
                            T t = cls.newInstance();
                            for (Cell cell : row) {
                                int j = cell.getColumnIndex();
                                if (reflectionMap.containsKey(j)) {
                                    String cellValue = getStringValueFromCell(cell);
                                    List<Field> fieldList = reflectionMap.get(j);
                                    for (Field field : fieldList) {
                                        try {
                                            if (cell.getCellType().equals(CellType.NUMERIC)) {
                                                field.set(t, Integer.valueOf(cellValue));
                                            } else {
                                                field.set(t, cellValue);
                                            }
                                        } catch (Exception e) {
                                            log.error("excel.setField.error={}", e);
                                        }
                                    }
                                }
                            }
                            dataList.add(t);
                        } catch (Exception e) {
                            log.error("excel.addRow.error={}", e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("excel.io.error={}", e);
        } finally {
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (Exception e) {
                    log.error("excel.finally.error={}", e);
                }
            }
        }
        return dataList;
    }

    public static List<String> isLegalForHeader(String path) {
        List<String> titles = new ArrayList<>();
        Workbook workbook = null;
        try {
            FileInputStream is = new FileInputStream(new File(path));
            if (path.endsWith("xlsx")) {
                workbook = new XSSFWorkbook(is);
            }
            if (path.endsWith("xls")) {
                workbook = WorkbookFactory.create(is);
            }
            if (workbook != null) {
                Sheet sheet = workbook.getSheetAt(0);
                AtomicInteger ai = new AtomicInteger();
                for (Row row : sheet) {
                    int i = ai.incrementAndGet();
                    if (i == 1) {
                        for (Cell cell : row) {
                            String cellValue = getStringValueFromCell(cell).toLowerCase();
                            titles.add(cellValue);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("excel.io.error={}", e);
        } finally {
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (Exception e) {
                    log.error("excel.finally.error={}", e);
                }
            }
        }
        return titles;
    }


    /**
     * 获取Excel 单元格数据
     *
     * @param currentCell 单元格
     * @return
     */
    public static String getStringValueFromCell(Cell currentCell) {
        return DF.formatCellValue(currentCell);
    }


}
