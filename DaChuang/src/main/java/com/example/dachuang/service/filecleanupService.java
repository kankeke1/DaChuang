package com.example.dachuang.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

/**
 * @author mmy
 * @data 2023/10/7 17:07
 * @description 用来每天删除用户上传的文件
 */
@Service
public interface filecleanupService {
    // 获取上传文件目录路径
    static final String UPLOAD_DIR = System.getProperty("user.dir")+File.separator+"rubbish";
    //设置文件过期时间
    static final long FILE_EXPIRATION_TIME = 10 * 60 * 1000 ; // 一天的毫秒数24 * 60 * 60 * 1000

    public void cleanupExpiredFiles() throws IOException;

}
