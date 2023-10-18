package com.example.dachuang.controller;

import com.example.dachuang.service.generateService;
import com.example.dachuang.utils.util;

import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;


import java.io.File;
import java.io.IOException;

import static com.sun.activation.registries.LogSupport.log;

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
    public String generateProduct(@RequestParam("file")MultipartFile file,@RequestParam("time") int time ,
                            @RequestParam("num") int num,@RequestParam(value = "type", defaultValue = "0") int type) throws Exception {
        //type=0为greedy，1为nearoptimal

        if(time<=0){
            return "时间参数设置错误：不能小于0";
        }
        if(file.isEmpty()){
            return "上传失败，请选择文件";
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
            log(e.getMessage(),e);
        }
        log("generate Product Succeed");
        generateService.downLoadProduct();

        return util.GetGenerateResult(res);
       
    }


    @RequestMapping("/generateModelXy")
    @ResponseBody
    public String generateProductXy(@RequestParam("file")MultipartFile file,
                                    @RequestParam("type")int type,@RequestParam("num") int num,
                                    @RequestParam("n-wise")int tstrength) throws Exception {
        //保存文件路径
    String path=generateService.loadFeatureModelXy(file);
    String res=generateService.useXy0(type,num,tstrength,path);
    return util.GetGenerateResult(res);
    }

}
