package com.example.dachuang;

import com.example.dachuang.service.generateService;
import com.example.dachuang.service.generateServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@SpringBootTest
class DaChuangApplicationTests {

	@Test
	void contextLoads() {
		String projectRoot = System.getProperty("user.dir");
		System.out.println(projectRoot);
	}

	@Test
	void shengcheng() throws Exception {
		// 本地文件路径
		String filePath = "D:/Desktop/ecos-icsell.dimacs";

		// 读取本地文件内容到字节数组
		byte[] fileContent = Files.readAllBytes(Paths.get(filePath));

		// 创建一个MockMultipartFile对象，模拟文件上传
		MultipartFile multipartFile = new MockMultipartFile("file", "file.dimacs", "text/plain", fileContent);

		generateService generate= new generateServiceImpl();
		// 现在，你可以使用multipartFile来模拟处理上传的文件
		generate.loadFeatureModel(multipartFile);
		generate.setTimeNum(20,200);
		generate.generateProduct();
//		generate.downLoadProduct();


	}

}
