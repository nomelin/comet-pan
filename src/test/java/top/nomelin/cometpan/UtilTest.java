package top.nomelin.cometpan;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import top.nomelin.cometpan.pojo.Account;

import java.util.TreeMap;

import static top.nomelin.cometpan.util.Util.parse;

@SpringBootTest
public class UtilTest {


    @Test
    public void test() {
//        System.out.println(Util.getArrayInt("1,2,3,2,43"));
//        System.out.println(Util.getArrayInt("[1,2,-3,2,43]"));

        String elString = "#account.id";
        String elString2 = "#account.password";
        String elString3 = "#test";

        TreeMap<String, Object> map = new TreeMap<>();
        Account account = new Account();
        account.setPassword("123456");
        account.setId(3);
        account.setUserName("test");
        map.put("account", account);
        map.put("test", 123);

        String val = parse(elString, map);
        String val2 = parse(elString2, map);
        String val3 = parse(elString3, map);

        System.out.println(val);
        System.out.println(val2);
        System.out.println(val3);

    }
}
