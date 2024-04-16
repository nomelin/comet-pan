package top.nomelin.cometpan.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.nomelin.cometpan.pojo.FileMeta;

import java.util.ArrayList;
import java.util.List;

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
        //fileService.addRoot(3);
    }

    @Test
    void deleteById() {

    }

    @Test
    void deleteBatch() {
//        List<Integer> ids = new ArrayList<>();
//        ids.add(29);
//        ids.add(28);
//        ids.add(27);
//        ids.add(31);
//        ids.add(32);
//        fileService.deleteBatch(ids);
    }

    @Test
    void deleteNode() {
        //fileService.setDeleteNode(29);
        //fileService.cancelDeleteNode(29);
        //fileService.deleteNode(29);
    }
    @Test
    void updateTime () {
        //fileService.updateTimeById(36);
    }

    @Test
    void setDeleteById() {
    }

    @Test
    void setDeleteBatch() {
        List<Integer> ids = new ArrayList<>();
        ids.add(31);
        ids.add(32);
        fileService.setDeleteBatch(ids);
    }

    @Test
    void updateById() {
    }

    @Test
    void selectById() {
        System.out.println(fileService.selectById(5));
    }

    @Test
    void selectAll() {
        FileMeta fileMeta = new FileMeta();
        fileMeta.setUserId(3);
        fileService.selectAll(fileMeta).forEach(System.out::println);
    }

    @Test
    void selectPage() {
    }

    @Test
    void updateSzieById() {
    }


    @Test
    void addFolder() {
        //fileService.addFolder("测试", 5);
    }

    @Test
    void addFile() {
        fileService.addFile("一个文本文件", 108, 3000 ,"txt");
    }


    @Test
    void selectByParentFolderId() {
        fileService.selectByParentFolderId(5).forEach(System.out::println);
    }

}