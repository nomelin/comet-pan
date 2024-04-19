<template>
  <div>
    <uploader
        :autoStart="false"
        :options="options"
        :file-status-text="statusText"
        class="uploader-example"
        @file-complete="fileComplete"
        @complete="complete"
        @file-success="fileSuccess"
        @files-added="filesAdded"
    >
      <uploader-unsupport></uploader-unsupport>
      <uploader-drop>
        <p>将文件拖放到此处以上传</p>
        <uploader-btn>选择文件</uploader-btn>
        <uploader-btn :attrs="attrs">选择图片</uploader-btn>
        <uploader-btn :directory="true">选择文件夹</uploader-btn>
      </uploader-drop>
      <!-- <uploader-list></uploader-list> -->
      <uploader-files></uploader-files>
    </uploader>
    <br/>
    <el-button @click="allStart()" :disabled="disabled">全部开始</el-button>
    <el-button @click="allStop()" style="margin-left: 4px">全部暂停</el-button>
    <el-button @click="allRemove()" style="margin-left: 4px">全部移除</el-button>
  </div>
</template>

<script>
import SparkMD5 from "spark-md5";
// import {upload} from "@/api/user";
// import storage from "store";
// import { ACCESS_TOKEN } from '@/store/mutation-types'
export default {
  name: "Home",
  data() {
    return {
      skip: false,
      options: {
        target: "//localhost:12345/upload/chunk",
        // 开启服务端分片校验功能
        testChunks: true,
        parseTimeRemaining: function (timeRemaining, parsedTimeRemaining) {
          return parsedTimeRemaining
              .replace(/\syears?/, "年")
              .replace(/\days?/, "天")
              .replace(/\shours?/, "小时")
              .replace(/\sminutes?/, "分钟")
              .replace(/\sseconds?/, "秒");
        },
        // 服务器分片校验函数
        checkChunkUploadedByResponse: (chunk, message) => {
          const result = JSON.parse(message);
          if (result.data.skipUpload) {
            this.skip = true;
            return true;
          }
          return (result.data.uploaded || []).indexOf(chunk.offset + 1) >= 0;
        },
        // headers: {
        //   // 在header中添加的验证，请根据实际业务来
        //   "Access-Token": storage.get(ACCESS_TOKEN),
        // },
      },
      attrs: {
        accept: "image/*",
      },
      statusText: {
        success: "上传成功",
        error: "上传出错了",
        uploading: "上传中...",
        paused: "暂停中...",
        waiting: "等待中...",
        cmd5: "计算文件MD5中...",
      },
      fileList: [],
      disabled: true,
    };
  },
  watch: {
    fileList(o, n) {
      this.disabled = false;
    },
  },
  methods: {
    // fileSuccess(rootFile, file, response, chunk) {
    //   // console.log(rootFile);
    //   // console.log(file);
    //   // console.log(message);
    //   // console.log(chunk);
    //   const result = JSON.parse(response);
    //   console.log(result.success, this.skip);
    //
    //   if (result.success && !this.skip) {
    //     axios
    //         .post(
    //             "http://127.0.0.1:12345/upload/merge",
    //             {
    //               identifier: file.uniqueIdentifier,
    //               filename: file.name,
    //               totalChunks: chunk.offset,
    //             },
    //             // {
    //             //   headers: { "Access-Token": storage.get(ACCESS_TOKEN) }
    //             // }
    //         )
    //         .then((res) => {
    //           if (res.data.success) {
    //             console.log("上传成功");
    //           } else {
    //             console.log(res);
    //           }
    //         })
    //         .catch(function (error) {
    //           console.log(error);
    //         });
    //   } else {
    //     console.log("上传成功，不需要合并");
    //   }
    //   if (this.skip) {
    //     this.skip = false;
    //   }
    // },
    fileSuccess(rootFile, file, response, chunk) {
      // console.log(rootFile);
      // console.log(file);
      // console.log(message);
      // console.log(chunk);
      const result = JSON.parse(response);
      console.log(result.code, this.skip);
      const user = {
        identifier: file.uniqueIdentifier,
        filename: file.name,
        totalChunks: chunk.offset,
      }
      console.log(result.code === 200 && !this.skip)
      if (result.code === '200' && !this.skip) {
        console.log("开始合并文件");
        // upload(user).then((res) => {
        //   if (res.code === 200) {
        //     console.log("上传成功");
        //   } else {
        //     console.log(res);
        //   }
        // }).catch(function (error) {
        //   console.log(error);
        // });
      } else {
        console.log("上传成功，不需要合并");
      }
      if (this.skip) {
        this.skip = false;
      }
    },
    fileComplete(rootFile) {
      // 一个根文件（文件夹）成功上传完成。
      console.log("fileComplete", rootFile);
      // console.log("一个根文件（文件夹）成功上传完成。");
    },
    complete() {
      // 上传完毕。
      console.log("complete");
    },
    filesAdded(file, fileList, event) {
      // console.log(file);
      file.forEach((e) => {
        this.fileList.push(e);
        this.computeMD5(e);
      });
    },
    computeMD5(file) {
      let fileReader = new FileReader();
      let time = new Date().getTime();
      let blobSlice =
          File.prototype.slice ||
          File.prototype.mozSlice ||
          File.prototype.webkitSlice;
      let currentChunk = 0;
      const chunkSize = 1024 * 1024;
      let chunks = Math.ceil(file.size / chunkSize);
      console.log(`开始计算${file.name}的MD5，分片数：${chunks}`);
      let spark = new SparkMD5.ArrayBuffer();
      // 文件状态设为"计算MD5"
      file.cmd5 = true; //文件状态为“计算md5...”
      file.pause();
      loadNext();
      fileReader.onload = (e) => {
        spark.append(e.target.result);
        if (currentChunk < chunks) {
          currentChunk++;
          loadNext();
          // 实时展示MD5的计算进度
          console.log(
              `第${currentChunk}分片解析完成, 开始第${
                  currentChunk + 1
              } / ${chunks}分片解析`
          );
        } else {
          let md5 = spark.end();
          console.log(
              `MD5计算完毕：${file.name} \nMD5：${md5} \n分片：${chunks} 大小:${
                  file.size
              } 用时：${new Date().getTime() - time} ms`
          );
          spark.destroy(); //释放缓存
          file.uniqueIdentifier = md5; //将文件md5赋值给文件唯一标识
          file.cmd5 = false; //取消计算md5状态
          file.resume(); //开始上传
        }
      };
      fileReader.onerror = function () {
        this.$message.error(`文件${file.name}读取出错，请检查该文件`);
        file.cancel();
      };

      function loadNext() {
        let start = currentChunk * chunkSize;
        let end = start + chunkSize >= file.size ? file.size : start + chunkSize;
        fileReader.readAsArrayBuffer(blobSlice.call(file.file, start, end));
      }
    },
    allStart() {
      console.log(this.fileList);
      this.fileList.map((e) => {
        if (e.paused) {
          e.resume();
        }
      });
    },
    allStop() {
      console.log(this.fileList);
      this.fileList.map((e) => {
        if (!e.paused) {
          e.pause();
        }
      });
    },
    allRemove() {
      this.fileList.map((e) => {
        e.cancel();
      });
      this.fileList = [];
    },
  },
};
</script>

<style>
.uploader-example {
  width: 100%;
  padding: 15px;
  margin: 0px auto 0;
  font-size: 12px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.4);
}

.uploader-example .uploader-btn {
  margin-right: 4px;
}

.uploader-example .uploader-list {
  max-height: 440px;
  overflow: auto;
  overflow-x: hidden;
  overflow-y: auto;
}
</style>