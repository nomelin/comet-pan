package top.nomelin.cometpan.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;

@SpringBootTest
class DiskServiceTest {
    private final DiskService diskService;
    @Autowired
    public DiskServiceTest(DiskService diskService) {
        this.diskService = diskService;
    }

    @Test
    void uploadAvatar() throws IOException {
// 从类路径中读取资源文件，这里假设文件名为 "avatar.jpg"
//        Resource resource = new ClassPathResource("test1.png");
//        diskService.uploadAvatar(resource, 2);
    }
}