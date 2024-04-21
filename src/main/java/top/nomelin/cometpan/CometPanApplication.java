package top.nomelin.cometpan;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@MapperScan("top.nomelin.cometpan.dao")
@EnableTransactionManagement
@EnableScheduling
public class CometPanApplication {
    public static void main(String[] args) {
        SpringApplication.run(CometPanApplication.class, args);
    }

}
