package com.example.dachuang.service;

import com.example.dachuang.core.ModelPLEDGE;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author mmy
 * @data 2023/9/5 17:41
 * @description
 */
@Component
public class generateServiceImpl implements generateService{

    ModelPLEDGE model;
    String Path;
    String outPath;

@Override
    public void loadFeatureModel(MultipartFile file) throws Exception {
    System.out.println("进入loadFeatureModel");
    // 获取当前时间
    LocalDateTime currentTime = LocalDateTime.now();
    // 定义日期时间格式
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss");
    // 将日期时间对象转换为字符串
    String currentTimeStr = currentTime.format(formatter);


    // 获取Spring Boot项目的根目录路径
    Path = System.getProperty("user.dir")+File.separator+"rubbish"+File.separator+currentTimeStr;
    // 创建文件夹
    File folder = new File(Path);
    if (!folder.exists()) {
        boolean created = folder.mkdir();
        if (created) {
            System.out.println("文件夹创建成功！");
        } else {
            System.err.println("文件夹创建失败！");
        }
    } else {
        System.out.println("文件夹已经存在，需另创建？");
    }

    outPath=Path+File.separator+"out.txt";
    // 获取文件名
    String fileName = file.getOriginalFilename();
    // 构建文件路径
    Path filePath1 = Paths.get(Path, fileName);
    // 将文件保存到服务器
    Files.write(filePath1, file.getBytes());
    // 返回文件的路径
    String filePath=filePath1.toString();

    if (fileName.toLowerCase().endsWith(".dimacs")) {

        model=new ModelPLEDGE();
        System.out.println("进入.dimacs的loadFeatureModel");
        model.loadFeatureModel(filePath, model.getFeatureModelFormat().DIMACS);

    }
    else if (fileName.toLowerCase().endsWith("xml")) {

        model=new ModelPLEDGE();
        System.out.println("进入.splot的loadFeatureModel,xml格式");
        model.loadFeatureModel(filePath, model.getFeatureModelFormat().SPLOT);

    }
    else {
        System.out.println("传入文件类型错误");
    }

}

@Override
    public void setTimeNum(int num,long time){
    model.setNbProductsToGenerate(num);
    model.setGenerationTimeMSAllowed(time);
}

@Override
    public String generateProduct() throws Exception {
    model.generateProducts(0);
    return outPath;
}

@Override
    public String generateProduct(int type) throws Exception {
        model.generateProducts(type);
        return outPath;
    }


@Override
    public void showProduct(){

}

@Override
    public void downLoadProduct() throws Exception {
    model.saveProducts(outPath);

    }
}
