package com.example.dachuang.service;

import org.springframework.stereotype.Service;

/**
 * @author mmy
 * @data 2023/9/5 17:28
 * @description
 */
@Service
public interface generateService {

    //检测特征模型的格式，并载入特征模型(接收的文件类型暂未知，先空着了）
    public void loadFeatureModel();
    //设置时间，数量
    public void setTimeNum(int time,int num) ;

    //生产产品
    public void generateProduct();
    //把结果呈现出来
    public void showProduct();
    //给用户下载结果
    public void downLoadProduct();
}
