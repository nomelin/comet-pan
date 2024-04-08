package top.nomelin.cometpan;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("top.nomelin.cometpan.dao")
public class CometpanApplication {
    public static void main(String[] args) {
        SpringApplication.run(CometpanApplication.class, args);
    }

}
