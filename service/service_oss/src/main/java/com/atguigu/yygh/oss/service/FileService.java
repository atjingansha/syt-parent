package com.atguigu.yygh.oss.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author WangJin
 * @create 2022-07-02 9:30
 */
public interface FileService {
    String upload(MultipartFile file);

}
