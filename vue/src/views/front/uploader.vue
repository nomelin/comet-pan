<template>
  <div class="uploader-container">
    <div class="uploader-title"></div>
    <uploader
        :autoStart="false"
        :options="options"
        :file-status-text="statusText"
        class="uploader"
        @file-complete="fileComplete"
        @complete="complete"
        @file-success="fileSuccess"
        @files-added="filesAdded"
    >
      <uploader-unsupport></uploader-unsupport>
      <uploader-drop class="uploader-drop">
        <span class="drop-text"><i class="el-icon-circle-plus"></i> 将文件拖到此处以上传</span>
        <!--        <uploader-btn :directory="true">选择文件夹</uploader-btn>-->
      </uploader-drop>
      <div class="uploader-btns">
        <uploader-btn class="uploader-btn" :single="false">选择文件</uploader-btn>
        <uploader-btn class="uploader-btn" :attrs="attrs" :single="true">选择图片</uploader-btn>
      </div>
      <uploader-list class="uploader-list">
        <uploader-files class="uploader-files"></uploader-files>
      </uploader-list>

    </uploader>
    <span v-if="disabled" class="uploader-tip">正在处理文件，请不要点击上传，稍加等候...</span>
    <div class="uploader-footer">
      <el-button class="primary-button" @click="allStart()" :disabled="disabled">全部开始</el-button>
      <el-button class="normal-button" @click="allStop()">全部暂停</el-button>
      <el-button class="normal-button" @click="allRemove()">全部移除</el-button>
    </div>
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
        chunkSize: 1024 * 1024 * 10, // 10MB
        target: process.env.VUE_APP_BASEURL+"/upload/chunk",
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
          if (result.code !== '200') {
            this.$message.error(result.code + "：" + result.msg);
            return false;
          }
          console.log(result);
          if (result.data.skipUpload && chunk.offset === 0) {
            this.skip = true;
            // this.$request.post()
            // console.log("信息：" + message)
            // console.log("chunk1：" + chunk.file.id)
            // console.log("chunk2：" + chunk.file.name)
            // console.log("chunk3：" + chunk.file.size)
            //发送秒传请求,只在第一个分片发送一次.
            let target = this.fileTarget.find(item => item.id === chunk.file.id)
            this.$request.post("/upload/instant", {
              filename: chunk.file.name,
              totalSize: chunk.file.size,
              targetFolderId: target.srcId,
              diskId: result.data.diskId,
            }).then(res => {
              if (res.code === '200') {
                this.$message.success("[ 秒传 ] 成功:" + chunk.file.name);
              } else {
                this.$message.error("[ 秒传 ] 失败" + res.code + "：" + res.msg + "，文件名：" + chunk.file.name);
              }
            }).catch(err => {
              console.log(err)
            })
            return true;
          } else if (result.data.skipUpload) {
            return true;
          }
          return (result.data.uploaded || []).indexOf(chunk.offset + 1) >= 0;
        },
        headers: {
          // 在header中添加的验证
          "token": localStorage.getItem("user") ? JSON.parse(localStorage.getItem("user")).token : "none",
        },
        simultaneousUploads: 3, // 并发上传的最大数量
      },
      attrs: {
        accept: "image/*",
      },
      statusText: {
        success: "上传成功",
        error: "上传出错了",
        uploading: "正在上传...",
        paused: "暂停中...",
        waiting: "等待中...",
        cmd5: "计算文件MD5中...",
      },
      fileList: [],// 上传的文件列表
      // disabled: true,

      fileTarget: [],// 上传文件目标文件夹id，{id:xxx,srcId:xxx}

      computingMd5: [], // 正在计算md5的文件列表,还有计算的文件时，禁止上传
    };
  },
  props: {
    // uploaderVisible: {
    //   type: Boolean,
    //   required: true
    // },
    srcId: {
      type: Number,
      required: true
    }
  },
  watch: {
    // fileList(o, n) {
    //   this.disabled = false;
    // },
  },
  computed: {
    disabled() {
      return this.computingMd5.length > 0 && this.fileList.length > 0;
    },
  },

  methods: {
    fileSuccess(rootFile, file, response, chunk) {
      // console.log(rootFile);
      // console.log(file);
      console.log(chunk);
      const result = JSON.parse(response);
      console.log(result);
      // console.log("合并：file name=[" + file.name + "]src id=[" + this.srcId + "]")
      let target = this.fileTarget.find(item => item.id === rootFile.id)
      console.log("上传目的文件夹：" + target.id + "，目标目录: " + target.srcId)
      const merge = {
        identifier: file.uniqueIdentifier,
        filename: file.name,
        totalChunks: chunk.offset,
        targetFolderId: target.srcId,
      }
      if (merge.totalChunks === 0 && result.data.skipUpload === false && result.data.uploaded.length === file.chunks.length) {
        //这是因为分片全部上传以后由于网络中断，所以前端重新请求，
        // 后端返回了已经上传的分片，前端发现分片已经全部上传，就直接调用合并请求，导致chunk不是最后一个分片数据。
        // 由于chunk.offset为0，导致无法合并
        //这里直接获取了file.chunks.length.
        //merge.totalChunks === 0 代表由于某种问题，导致这里获取不到最后一个分片
        //也可能是总共只有一个分片，那正常情况也是0，但是不影响。
        //skipUpload === false说明文件没有真正合并，还是缓存中
        //result.data.uploaded.length === file.chunks.length代表分片全部上传成功
        merge.totalChunks = file.chunks.length - 1;
      }
      if (result.code === '200' && !this.skip) {
        console.log("开始合并文件");
        this.$request.post("/upload/merge", merge).then((res) => {
          if (res.code === '200') {
            this.$message.success("上传成功:" + file.name);
          } else {
            this.$message.error(res.code + "：" + res.msg + "，文件名：" + file.name);
            return false;
          }
        }).catch(function (error) {
          console.log(error);
        });
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
      // this.disabled = true;
      // console.log(file);
      file.forEach((e) => {
        console.log("id:" + e.id + ",srcId:" + this.srcId);
        this.fileTarget.push({"id": e.id, "srcId": this.srcId});
        this.fileList.push(e);
        this.addToSet(e.id)
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
      const chunkSize = this.options.chunkSize;
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
          // file.resume(); //开始上传,
          // 计算完md5后，也是暂停
          // this.disabled = false;
          setTimeout(() => {
            this.removeFromSet(file.id);
          }, 500);//延时是为了保证一定处理好了.
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
      // console.log(this.fileList);
      this.fileList.map((e) => {
        if (e.paused) {
          e.resume();
        }
      });
    },
    allStop() {
      // console.log(this.fileList);
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
    // 向数组尾部添加元素
    addToSet(element) {
      if (!this.computingMd5.includes(element)) {
        this.computingMd5.push(element);
      }
    },
    // 从数组中删除指定元素
    removeFromSet(element) {
      const index = this.computingMd5.indexOf(element);
      if (index !== -1) {
        this.computingMd5.splice(index, 1);
      }
    },
  },
};
</script>

<style>
.uploader-container {
  width: 90%;
  height: 95%;
  margin-left: 5%;
  /*border-radius: 20px;*/
  background-color: #ffffff;
}

.uploader-title {
  height: 10%;
}

.uploader {
  width: 100%;
  font-size: 1rem;
  background-color: #ffffff;

}

.uploader-drop {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 10vh;
  width: 80%;
  margin-left: 10%;

}

.drop-text {
  font-size: 1.1rem;
  color: #606266;
  font-weight: bold;
}

.uploader-btns {
  display: flex;
  justify-content: center;
  align-items: center;
}

.uploader-btn {
  margin-top: 5%;
  margin-right: 5%;
}

.uploader-list {
  background-color: #ffffff;
  overflow-y: auto; /* 垂直方向超出时显示滚动条 */
  max-height: 50vh; /* 设置容器的最大高度，超过此高度时将显示滚动条 */

}

.uploader-files {
  margin: 2%;
  padding: 2%;
}

.uploader-footer {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 5%;
}

.uploader-tip {
  font-size: 1.1rem;
  color: #606266;
  font-weight: bold;
  display: inline-block;
  text-align: center;
  width: 100%; /* 如果需要让span占据其父元素的宽度，可以设置为100% */
}
</style>