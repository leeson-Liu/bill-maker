//package com.bill.maker.utils;
//
//import com.paasoo.web.common.upload.FileUploadPathResolver;
//import com.paasoo.web.common.upload.StorageService;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.fileupload.FileItem;
//import org.apache.commons.fileupload.disk.DiskFileItemFactory;
//import org.apache.commons.io.FilenameUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import org.springframework.util.FileCopyUtils;
//import org.springframework.web.multipart.MultipartFile;
//import org.springframework.web.multipart.commons.CommonsMultipartFile;
//
//import javax.activation.MimetypesFileTypeMap;
//import java.io.ByteArrayInputStream;
//import java.io.File;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//
///**
// *
// */
//@Slf4j
//@Component
//public class FileUtils {
//
//    @Autowired
//    private StorageService storageService;
//    @Autowired
//    private FileUploadPathResolver fileUploadPathResolver;
//
//    /**
//     * File转MultipartFile
//     */
//    public static MultipartFile toMultipartFile(String fieldName, File file) throws Exception{
//        DiskFileItemFactory diskFileItemFactory = new DiskFileItemFactory();
//        String contentType = new MimetypesFileTypeMap().getContentType(file);
//        FileItem fileItem = diskFileItemFactory.createItem(fieldName, contentType, false, file.getName());
//        InputStream inputStream = new ByteArrayInputStream(FileCopyUtils.copyToByteArray(file));
//        OutputStream outputStream = fileItem.getOutputStream();
//        FileCopyUtils.copy(inputStream, outputStream);
//        return new CommonsMultipartFile(fileItem);
//    }
//
//    /**
//     * File转MultipartFile
//     */
//    public static MultipartFile toMultipartFile(String fieldName, String fileName, byte[] fileByteArray) throws Exception {
//        DiskFileItemFactory diskFileItemFactory = new DiskFileItemFactory();
//        String contentType = new MimetypesFileTypeMap().getContentType(fileName);
//        FileItem fileItem = diskFileItemFactory.createItem(fieldName, contentType, false, fileName);
//        InputStream inputStream = new ByteArrayInputStream(fileByteArray);
//        OutputStream outputStream = fileItem.getOutputStream();
//        FileCopyUtils.copy(inputStream, outputStream);
//        return new CommonsMultipartFile(fileItem);
//    }
//
//    /**
//     * MultipartFile转File
//     */
//    public File convert(MultipartFile file, Long currencyCpId) throws IOException {
//        String originalFilename = FilenameUtils.getName(file.getOriginalFilename());
//        String relativePath = fileUploadPathResolver.resolveRelativePath("license", String.valueOf(currencyCpId), originalFilename);
//        storageService.store(relativePath, file);
//        String imgUrl = fileUploadPathResolver.getVisitFileUrl(relativePath);
//        return new File(imgUrl);
//    }
//}
