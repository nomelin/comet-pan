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
        long currentTime = System.currentTimeMillis();
        long endTime = 1713867943000L;

        long remainingDays = calculateRemainingDays(currentTime, endTime);
        System.out.println("Remaining days: " + remainingDays);
    }
}
