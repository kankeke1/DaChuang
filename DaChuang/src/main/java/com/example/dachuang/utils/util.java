package com.example.dachuang.utils;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.sun.activation.registries.LogSupport.log;

/**
 * @Author WHJ
 * @Date 7/9/2023 16:48
 * @Version 1.0 （版本号）
 * @Description:
 */

public class util {
    public static String GetGenerateResult(String filepath){
        BufferedReader br ;
        String res="";
        try {
            br = new BufferedReader(new FileReader(filepath));
            String line;
            while((line = br.readLine())!=null){
                    res+=line;
                    res+="\n";
            }
            //res = res.substring(0, res.length() - 1)+'\n';
        } catch (IOException e) {
            e.printStackTrace();
            log(e.getMessage(),e);
        }
        log("read res succeed");
        return res;
    }

    public static String getCoverrageRate(String path,int Nwise,int productnum,int time){

        Path temppath = Paths.get(path);
        String fileName = temppath.getFileName().toString();
        Path parentPath = temppath.getParent();
        String parentPathStr=parentPath.toString();
        String productPath = parentPathStr + "/"+Nwise+"wise/"+productnum+"prods/"+time+"ms/"+"Coverage.0";

        return getString(productPath);
    }

    public static String getRuntime(String path,int Nwise,int productnum,int time){

        Path temppath = Paths.get(path);
        String fileName = temppath.getFileName().toString();
        Path parentPath = temppath.getParent();
        String parentPathStr=parentPath.toString();
        String productPath = parentPathStr + "/Samples/"  + productnum + "prods/"+time+"ms/" +"RUNTIME.0";

        return getString(productPath);
    }

    private static String getString(String productPath) {
        BufferedReader br ;
        String res="";
        try {
            br = new BufferedReader(new FileReader(productPath));
            res=br.readLine();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return res;
    }
}
