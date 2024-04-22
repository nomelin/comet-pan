package top.nomelin.cometpan;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import top.nomelin.cometpan.util.Util;

import java.util.List;
import java.util.UUID;

import static top.nomelin.cometpan.util.Util.calculateRemainingDays;

@SpringBootTest
public class UtilTest {


    @Test
    public void test() {
        System.out.println(Util.getArrayInt("1,2,3,2,43"));
        System.out.println(Util.getArrayInt("[1,2,-3,2,43]"));
    }
}
