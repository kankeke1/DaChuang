package com.example.dachuang.service;

import com.example.dachuang.core.ModelPLEDGE;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import spl.SPL;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
    Path = System.getProperty("user.dir")+File.separator+"rubbish"+File.separator+Random()+currentTimeStr;

    // 创建文件夹
    File folder = new File(Path);
//    System.out.println("canwrite?:"+(folder.canWrite()?"yes":"no"));
    if (!folder.exists()) {
        boolean created = folder.mkdirs();
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
//    else if (fileName.toLowerCase().endsWith("xml")) {
//
//        model=new ModelPLEDGE();
//        System.out.println("进入.splot的loadFeatureModel,xml格式");
//        model.loadFeatureModel(filePath, model.getFeatureModelFormat().SPLOT);
//
//    }
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
    public ModelPLEDGE downLoadProduct() throws Exception {
    model.saveProducts(outPath);
    return model;

    }


@Override
    public String loadFeatureModelXy(MultipartFile file) throws Exception{
    System.out.println("进入loadFeatureModelXy");
    // 获取当前时间
    LocalDateTime currentTime = LocalDateTime.now();
    // 定义日期时间格式
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss");
    // 将日期时间对象转换为字符串
    String currentTimeStr = currentTime.format(formatter);
    // 获取Spring Boot项目的根目录路径
    Path = System.getProperty("user.dir")+File.separator+"rubbish"+File.separator+Random()+currentTimeStr;
    // 创建文件夹
    File folder = new File(Path);
    System.out.println("canwrite?:"+(folder.canWrite()?"yes":"no"));
    if (!folder.exists()) {
        boolean created = folder.mkdirs();
        if (created) {
            System.out.println("文件夹创建成功！");
        } else {
            System.err.println("文件夹创建失败！");
        }
    } else {
        System.out.println("文件夹已经存在，需另创建？");
    }
    // 获取文件名
    String fileName = file.getOriginalFilename();
    // 构建文件路径
    Path filePath1 = Paths.get(Path,fileName);
    // 将文件保存到服务器
    Files.write(filePath1, file.getBytes());
    // 返回文件的路径
    String filePath=filePath1.toString();

    return filePath;
    }

@Override
    public String useXy0(int type,int Prodsnum,int tstrength,String path ,String pathLastName,int time) throws Exception {

    Path temppath = Paths.get(path);
    String fileName = temppath.getFileName().toString();
    Path parentPath = temppath.getParent();
    String parentPathStr=parentPath.toString();

    System.out.println("path为："+parentPathStr);
    System.out.println("filename为："+fileName);

    boolean isDimacs=true;
    if (fileName.toLowerCase().endsWith("xml")) {
        isDimacs=false;
    }
    //生成产品
    SPL.generateXy(isDimacs,type,Prodsnum,tstrength,parentPathStr,fileName,time);
    System.out.println("产品生成完成");

    //产品压缩
    String sourceFolder = parentPathStr; // 要压缩的文件夹
    String zipFilePath = System.getProperty("user.dir")+File.separator+"rubbish/"+pathLastName+  ".zip"; // 压缩文件的输出路径
    try {
        FileOutputStream fos = new FileOutputStream(zipFilePath);
        ZipOutputStream zos = new ZipOutputStream(fos);
        zipFolder(sourceFolder, sourceFolder, zos);
        zos.close();
    } catch (IOException e) {
        e.printStackTrace();
    }

        String productPath = parentPathStr + "/Samples/"  + Prodsnum + "prods/"+time+"ms/" +"Products.0";
        System.out.println("返回的产品路径："+productPath);
        return productPath;


    }

@Override
    public void zipFolder(String sourceFolder, String basePath, ZipOutputStream zos) throws IOException {
    File folder = new File(sourceFolder);
    for (File file : folder.listFiles()) {
        if (file.isDirectory()) {
            zipFolder(file.getAbsolutePath(), basePath, zos);
        } else {
            String relativePath = file.getAbsolutePath().substring(basePath.length() + 1);
            ZipEntry zipEntry = new ZipEntry(relativePath);
            zos.putNextEntry(zipEntry);
            byte[] buffer = new byte[1024];
            int length;
            FileInputStream fis = new FileInputStream(file);
            while ((length = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, length);
            }
            fis.close();
            zos.closeEntry();
        }
    }
}
    private String Random(){
    StringBuilder salt =new StringBuilder();
    Random random = new Random();
        for(int i=0;i<8;i++) {
            int num = random.nextInt(11);
            salt.append(num);
        }
        return salt.toString();
    }
}
