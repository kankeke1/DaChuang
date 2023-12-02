package com.example.dachuang.controller;

import com.example.dachuang.core.ModelPLEDGE;
import com.example.dachuang.service.generateService;
import com.example.dachuang.utils.util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @Author WHJ
 * @Date 6/9/2023 23:45
 * @Version 1.0 （版本号）
 * @Description:用于生成的控制器
 */

@Controller
public class generateController {
    @Autowired
    generateService generateService;
    @RequestMapping("/generateModel")
    @ResponseBody
    public String[] generateProduct(@RequestParam("file")MultipartFile file,@RequestParam("time") int time ,
                            @RequestParam("num") int num,@RequestParam(value = "type", defaultValue = "0") int type) throws Exception {
        //type=0为greedy，1为nearoptimal

        if(time<=0){
            return new String[]{"时间参数设置错误：不能小于0"};
        }
        if(file.isEmpty()){
            return new String[]{"上传失败，请选择文件"};
        }
        try {
            generateService.loadFeatureModel(file);
        } catch (Exception e) {
                e.printStackTrace();

        }
        System.out.println("loadFeatureModel succeed");
        String res="";
        generateService.setTimeNum(num,time);
        try {
           res = generateService.generateProduct(type);
        } catch (Exception e) {
            e.printStackTrace();
           // log(e.getMessage(),e);
        }
        System.out.println("generate Product Succeed");
        ModelPLEDGE mod= generateService.downLoadProduct();

//        //覆盖率
//        String[] Coverage=new String[1];
//        Coverage[0] = "#";
//        getCoverage(Coverage,mod);
//        String[] ret = getCoverageRet(Coverage[0]);
//        return new String[]{util.GetGenerateResult(res),ret[0],ret[1],ret[2]};
        //1.产品（有模型有数字）  4.覆盖率（生成失败返回#）
        return new String[]{util.GetGenerateResult(res),"","","#"};
    }
    private String[] getCoverageRet(String cov){
        String[] Coverage=new String[3];
        if(cov.equals('#')) {
            return new String[]{"#"};
        }
        String trim1 = cov.substring(StringUtils.indexOf(cov,":")+1);
        String pairsOfModels = trim1.trim();
        pairsOfModels = pairsOfModels.substring(0,StringUtils.indexOf(pairsOfModels,"\n")==-1?0:StringUtils.indexOf(pairsOfModels,"\n"));
//        System.out.println(pairsOfModels);

        String trim2 = trim1.substring(StringUtils.indexOf(trim1,":")+1);
        String pairsOfPros = trim2.trim();
        pairsOfPros = pairsOfPros.substring(0,StringUtils.indexOf(pairsOfPros,"\n")==-1?0:StringUtils.indexOf(pairsOfPros,"\n"));
        String trim3 = trim2.substring(StringUtils.indexOf(trim2,":")+1);
        String Coverage_trim = trim3.trim();
        Coverage[0] =pairsOfModels;
        Coverage[1] = pairsOfPros;
        Coverage[2] = Coverage_trim;
        return Coverage;
    }
    private String getCoverage(String[] Coverage,ModelPLEDGE mod) {
        Thread thread = new Thread(() -> {
            try {
              Coverage[0] =  mod.getPairwiseCoverage();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
        long maxExecutionTime = 5000*2;//5 s
        long startTime = System.currentTimeMillis();
        while (true) {
            if (System.currentTimeMillis() - startTime > maxExecutionTime) {
                // 如果执行时间超过5秒，中断线程
                thread.interrupt();
                break;
            }
            if(!Coverage[0].equals("#")) {
                thread.interrupt();
                break;
            }
        }
        return Coverage[0];
    }

    @RequestMapping("/generateModelXy")
    @ResponseBody
    public String[] generateProductXy(@RequestParam("file") MultipartFile file,
                                               @RequestParam("type")int type, @RequestParam("time") int time ,@RequestParam("num") int num,
                                               @RequestParam("n-wise")int tstrength) throws Exception {
        System.out.println("进入ns算法");
        type=3;tstrength=2;//这里先默认使用ns

        //保存文件路径
        String path=generateService.loadFeatureModelXy(file);
        Path path1 = Paths.get(path);
        // 获取文件的上级目录
        Path parentDirectory = path1.getParent();
        // 获取上级目录的名称
        String lastFolder = parentDirectory.getFileName().toString();

    String res=generateService.useXy0(type,num,tstrength,path,lastFolder,time);
    String strres= util.GetGenerateResultXy(res);
    String[] arr=new String[4];
    arr[0]=strres;//返回产品（全是数字的）
    arr[1]=util.getCoverrageRate(path,tstrength,num,time);//返回覆盖率（形如99.987）
    arr[2]=util.getRuntime(path,tstrength,num,time);//返回运行时间单位为毫秒（形如50000）
    arr[3]=lastFolder;//一个字符串，用来调用downloadfile接口

    return arr;

    }

    @GetMapping("/downloadFile")
    @ResponseBody
    public ResponseEntity<Resource> downloadFile(@RequestParam("filename")String filename) {
        System.out.println("进入返回压缩包接口");
        // 指定文件路径
        String filePath = System.getProperty("user.dir")+File.separator+"rubbish/"+filename+".zip";

        try {
            // 使用Spring的Resource来封装文件
            Resource resource = new UrlResource("file:" + filePath);

            // 设置响应头，包括文件名
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + resource.getFilename());

            //返回文件
            System.out.println("返回压缩包");
            // 返回文件资源
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }
}









