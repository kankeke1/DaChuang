package com.example.dachuang.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author mmy
 * @data 2023/10/7 17:12
 * @description
 */
@Component
public class filecleanupServiceImpl implements filecleanupService{
    @Override
    @Scheduled(fixedRate = 3600000) // 每小时运行一次3600000
    public void cleanupExpiredFiles() throws IOException {
        File baseDirectory = new File(UPLOAD_DIR);
        File[] folders = baseDirectory.listFiles();

        if (folders != null) {
            long currentTime = System.currentTimeMillis();
            for (File folder : folders) {
                if (folder.isDirectory()) {
                    long lastModifiedTime = folder.lastModified();
                    if (currentTime - lastModifiedTime > FILE_EXPIRATION_TIME) {
                        deleteFolder(folder); // 删除过期文件夹
                        System.out.println("删除过期文件夹");
                    }
                }
            }
        }
    }

    private void deleteFolder(File folder) throws IOException {
        Path folderPath = folder.toPath();
        Files.walk(folderPath)
                .sorted((a, b) -> -a.compareTo(b)) // 以递归方式深度优先删除
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }
}
