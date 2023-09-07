package com.example.dachuang.utils;

import lombok.extern.log4j.Log4j;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import static com.sun.activation.registries.LogSupport.log;

/**
 * @Author WHJ
 * @Date 7/9/2023 16:48
 * @Version 1.0 （版本号）
 * @Description:
 */
@Log4j
public class util {
    public static String GetGenerateResult(String filepath){
        BufferedReader br ;
        String res="";
        try {
            br = new BufferedReader(new FileReader(filepath));
            String line;
            while((line = br.readLine())!=null){
                    res+=line;
                    res+="+";
            }
        } catch (IOException e) {
            e.printStackTrace();
            log(e.getMessage(),e);
        }
        log("read res succeed");
        return res;
    }
}
