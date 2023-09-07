package com.example.dachuang.service;

import com.example.dachuang.core.ModelPLEDGE;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
    // 获取Spring Boot项目的根目录路径
    Path = System.getProperty("user.dir")+"\\rubbish"; // 获取当前工作目录，通常是项目根目录
    outPath=Path+"\\out.txt";
    // 获取文件名
    String fileName = file.getOriginalFilename();
    // 构建文件路径
    Path filePath1 = Paths.get(Path, fileName);
    // 将文件保存到服务器
    Files.write(filePath1, file.getBytes());
    // 返回文件的路径
    String filePath=filePath1.toString();

    if (fileName.endsWith(".dimacs")) {

        model=new ModelPLEDGE();
        model.loadFeatureModel(filePath, model.getFeatureModelFormat().DIMACS);

    } else if (fileName.endsWith(".splot")) {

        model=new ModelPLEDGE();
        model.loadFeatureModel(filePath, model.getFeatureModelFormat().SPLOT);

    } else {
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
    model.generateProducts();
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
