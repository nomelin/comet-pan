package top.nomelin.cometpan.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.nomelin.cometpan.pojo.FileMeta;

@SpringBootTest
class FileServiceTest {
    private final FileService fileService;

    @Autowired
    FileServiceTest(FileService fileService) {
        this.fileService = fileService;
    }

    @Test
    void add() {

    }

    @Test
    void addRoot() {
        fileService.addRoot(3);
    }

    @Test
    void deleteById() {
    }

    @Test
    void testAdd() {
    }

    @Test
    void testDeleteById() {
    }

    @Test
    void deleteBatch() {
    }

    @Test
    void setDeleteById() {
    }

    @Test
    void setDeleteBatch() {
    }

    @Test
    void updateById() {
    }

    @Test
    void selectById() {
    }

    @Test
    void selectAll() {
    }

    @Test
    void selectPage() {
    }
}