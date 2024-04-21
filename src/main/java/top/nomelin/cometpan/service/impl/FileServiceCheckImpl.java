//package top.nomelin.cometpan.service.impl;
//
//import cn.hutool.core.util.StrUtil;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import top.nomelin.cometpan.pojo.FileMeta;
//import top.nomelin.cometpan.service.FileService;
//import top.nomelin.cometpan.service.FileServiceCheck;
//import top.nomelin.cometpan.util.Util;
//
//import java.util.List;
//
//@Service
//public class FileServiceCheckImpl implements FileServiceCheck {
//
//    private FileService fileService;
//
//    @Autowired
//    public void setFileService(FileService fileService) {
//        this.fileService = fileService;
//    }
//
//    /**
//     * 检查同名文件或文件夹,没有重名，则返回原名字，有重名，则加上(1)或(2)等后缀
//     *
//     * @param fileName       文件名或文件夹名（不包括后缀）
//     * @param parentFolderId 父文件夹ID
//     * @param isFolder       是否为文件夹
//     * @return 修改后的名字，加上(1)或(2)等后缀
//     */
//    @Override
//    @Transactional
//    public String checkSameNameAndUpdate(String fileName, Integer parentFolderId, boolean isFolder) {
//        List<FileMeta> fileMetas = fileService.selectAllByParentFolderId(parentFolderId);
//        int num = 0;
//        boolean hasSameName = false;
//        // 找到同名文件或文件夹
//        for (FileMeta fileMeta : fileMetas) {
//            if (fileMeta.getName().equals(fileName) && fileMeta.getFolder().equals(isFolder)) {
//                hasSameName = true;
//            }
//            // 同名文件或文件夹
//            if (fileMeta.getName().startsWith(fileName) && fileMeta.getFolder().equals(isFolder)) {
//                String ends = fileMeta.getName().substring(fileName.length());
//                // 如果字符串不为空，判断是否有(1)或(2)等后缀
//                if (!StrUtil.isEmpty(ends) && ends.charAt(0) == '(' && ends.charAt(ends.length() - 1) == ')') {
//                    ends = ends.substring(1, ends.length() - 1);
//                    //如果是数字，则取最大值
//                    if (Util.isNumber(ends)) {
//                        num = Math.max(num, Integer.parseInt(ends));
//                    }
//                }
//            }
//        }
//        if (!hasSameName) {
//            return fileName;// 没有同名文件或文件夹
//        }
//        if (num > 0) {
//            return fileName + "(" + (num + 1) + ")";// 有多次同名文件或文件夹，后缀数字增加
//        } else {
//            return fileName + "(1)";// 仅有同名文件或文件夹加上(1)
//        }
//
//    }
//
//}
