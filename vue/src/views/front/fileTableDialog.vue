<template>
  <div>
    <!-- 高斯模糊背景 -->
    <div class="blur-background" v-if="dialogFilesVisible"></div>
    <el-dialog center class="dialog-files" :title="str" :visible.sync="dialogFilesVisible"
               :show-close="false" :close-on-click-modal="false" :close-on-press-escape="false">
      <el-button type="primary" plain @click="backNavigationTemp" icon="el-icon-back"
                 :disabled="cacheIndexTemp<=0"></el-button>
      <div class="path">
        <span>全部文件</span><span>{{ this.pathTemp }} 共{{ this.totalTemp }}</span>
      </div>
      <el-table class="table-files" :data="filteredData" style="cursor: pointer;" height="50vh"
                @row-click="handleFolderClickTemp" :default-sort="{prop: 'name', order: 'descending'}">>
        <el-table-column prop="folder" label="" width="50">
          <template v-slot="scope">
            <span>
                <img class="folder-icon" src="@/assets/imgs/folder.svg" alt="文件夹">
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="文件名称" min-width="200" class="name-column"
                         sortable :sort-orders="['ascending', 'descending']"></el-table-column>
      </el-table>
      <div slot="footer" class="dialog-footer">
        <el-button @click="handleCancel">取 消</el-button>
        <el-button v-if="type==='move'" type="primary" @click="handleMove">{{ str }}此文件夹</el-button>
        <el-button v-else-if="type==='copy'" type="primary" @click="handleCopy">{{ str }}此文件夹</el-button>
      </div>
    </el-dialog>
  </div>

</template>

<script>
export default {
  name: 'fileTableDialog',
  data() {
    return {
      tableDataTemp: [], // 弹出框里面显示的文件数据
      requestCacheTemp: [], // 初始化为空数组
      cacheIndexTemp: -1, // 缓存当前请求的索引
      maxCacheSize: 50, // 最大缓存大小
      pathTemp: "", // 弹出框里面显示的文件夹路径
      totalTemp: 0, // 弹出框里面显示的文件总数
      folderIdTemp: 0, // 当前的文件夹 ID
      user: JSON.parse(localStorage.getItem('user') || '{}'),
    }
  },
  props: {
    dialogFilesVisible: {
      type: Boolean,
      required: true
    },
    srcId: {
      type: Number,
      required: true
    },
    srcIds: {
      type: Array,
    },
    type: {
      type: String,//copy or move
      required: true
    },
  },
  computed: {
    filteredData() {
      // console.log(this.folderIdTemp)
      let data = this.tableDataTemp.filter(item => item.folder)
      this.totalTemp = data.length
      this.getPath(this.folderIdTemp)
      return data;
    },
    str() {
      if (this.type === 'copy') {
        return '复制到'
      } else {
        return '移动到'
      }
    }
  },
  mounted() {

  },
  watch: {
    // 监听 dialogFilesVisible 变化，如果变化为 true，则重新请求数据
    dialogFilesVisible: {
      immediate: true,
      handler(newVal, oldVal) {
        if (newVal && !oldVal) { // 只有在从 false 变为 true 时执行
          this.tableDataTemp = [] // 弹出框里面显示的文件数据
          this.requestCacheTemp = []// 初始化为空数组
          this.cacheIndexTemp = -1 // 缓存当前请求的索引
          this.pathTemp = ""// 弹出框里面显示的文件夹路径
          this.totalTemp = 0 // 弹出框里面显示的文件总数
          this.handleCacheAndGetFileRequest('/files')
          this.folderIdTemp = this.user.rootId
        }
      }
    }
  },
  methods: {
    backNavigationTemp() {
      // 判断是否有可后退的请求
      if (this.cacheIndexTemp > 0) {
        this.cacheIndexTemp--
        let folderId = this.requestCacheTemp[this.cacheIndexTemp].split('/')[3]
        if (!isNaN(folderId)) {
          this.folderIdTemp = folderId//如果是数字，则更新文件夹id
        } else {
          this.folderIdTemp = this.user.rootId//如果不是数字，则说明回到根目录
        }
        this.getFileRequest(this.requestCacheTemp[this.cacheIndexTemp])

      }
    },
    handleFolderClickTemp(row) {

      this.handleCacheAndGetFileRequest('/files/folder/' + row.id);
      this.folderIdTemp = row.id;
    },
    handleMove() {
      this.$emit('update:dialog-files-visible', false);
      // this.$request.post("/files/move/"+)
      console.log("移动:" + this.srcId + "->" + this.folderIdTemp)
      this.$request.post("/files/move/" + this.srcId + "/" + this.folderIdTemp).then(res => {
        if (res.code === '200') {
          this.$message.success("移动成功")
        } else {
          this.$message.error(res.code + ": " + res.msg)  // 弹出错误的信息
        }
      })
    },
    handleCopy() {
      this.$emit('update:dialog-files-visible', false);
      // this.$request.post("/files/move/"+)
      console.log("复制:" + this.srcId + "->" + this.folderIdTemp)
      this.$request.post("/files/copy/" + this.srcId + "/" + this.folderIdTemp).then(res => {
        if (res.code === '200') {
          this.$message.success("复制成功")
        } else {
          this.$message.error(res.code + ": " + res.msg)  // 弹出错误的信息
        }
      })
    },
    handleCancel() {
      this.$emit('update:dialog-files-visible', false);
    },
    getPath(id) {
      this.$request.get('/files/file/' + id).then(res => {
        if (res.code === '200') {
          this.pathTemp = res.data.path.replace(/\//g, '>'); // 使用正则表达式替换所有匹配到的 /
        } else {
          this.$message.error(res.code + ": " + res.msg)  // 弹出错误的信息
        }
      })
    },
    // reload() {
    //   this.getFileRequest(this.requestCacheTemp[this.cacheIndexTemp])
    // },
    handleCacheAndGetFileRequest(url) {
      // 用于封装getFileRequest和addToCache方法，统一处理
      // 丢弃索引之后的缓存(因为用户已经重新点击了新的路径)
      this.requestCacheTemp = this.requestCacheTemp.slice(0, this.cacheIndexTemp + 1)
      this.addToCache(url)
      this.getFileRequest(url)
    },
    addToCache(url) {
      // 将新的 URL 添加到缓存中
      this.requestCacheTemp.push(url)
      // 如果缓存大小超过限制，则丢弃最老的请求 URL
      if (this.requestCacheTemp.length > this.maxCacheSize) {
        this.requestCacheTemp.shift()
      }
      // 更新缓存索引
      this.cacheIndexTemp = this.requestCacheTemp.length - 1
    },
    getFileRequest(url) {
      this.$request.get(url).then(res => {
        if (res.code === '200') {
          this.tableDataTemp = res.data;
          this.totalTemp = res.data.length;
        } else {
          this.$message.error(res.code + ": " + res.msg)  // 弹出错误的信息
        }
      })
    },

  }
};
</script>

<style scoped>
.dialog-files {
  width: 100%;
  min-width: 800px;
  z-index: 999;
}

.blur-background {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.6); /* 半透明黑色背景 */
  z-index: 998; /* 确保遮罩层位于窗口下面 */
  backdrop-filter: blur(3px); /* 高斯模糊效果，可以根据需要调整模糊程度 */
}

.table-files {
  width: 100%;

}

.folder-icon {
  width: 100%;
}

.table-files {
  font-weight: bold;
  font-size: 13px;
}
</style>
