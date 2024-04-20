package top.nomelin.cometpan;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import top.nomelin.cometpan.util.Util;

@SpringBootTest
public class UtilTest {


    @Test
    public void test() {
        String str = "changelog_FR.txt";
        String filename=Util.removeType(str);
        String type=Util.getType(str);
        System.out.println(filename);
        System.out.println(type);

        System.out.println(Util.getFullName(filename,type));
    }
}
