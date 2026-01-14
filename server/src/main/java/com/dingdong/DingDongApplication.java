package com.dingdong;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.dingdong.mapper")
public class DingDongApplication {

    public static void main(String[] args) {
        SpringApplication.run(DingDongApplication.class, args);
    }

}
