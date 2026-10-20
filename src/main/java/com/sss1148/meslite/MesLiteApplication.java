package com.sss1148.meslite;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.sss1148.meslite.mapper")
public class MesLiteApplication {

    public static void main(String[] args) {
        SpringApplication.run(MesLiteApplication.class, args);
    }
}
