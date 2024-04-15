<template>
  <div class="main-container">
    <div class="blank"></div>
    <div class="operation">
      <el-input class="search-input" placeholder="搜索全部文件" style="width: 200px" v-model="name"
                @keyup.enter.native="selectAll(1)">
        <template #suffix>
          <i class="el-icon-search" @click="selectAll(1)"
             style="cursor: pointer; font-size: 25px; color:#909399; transform: translateY(9px);"></i>
        </template>
      </el-input>


      <el-button type="warning" plain style="margin-left: 10px" @click="reset">重置</el-button>
      <el-button type="primary" plain style="margin-left: 10px" @click="addFolder">新建文件夹</el-button>
      <el-button type="danger" plain @click="delBatch">批量删除</el-button>
    </div>
    <div class="blank"></div>
    <div class="backAndForward">
      <el-button type="primary" plain @click="backNavigation" icon="el-icon-back"
                 circle :disabled="cacheIndex <= 0"></el-button>
      <el-button type="primary" plain @click="forwardNavigation" icon="el-icon-right"
                 circle :disabled="cacheIndex >= requestCache.length - 1"></el-button>
    </div>
    <div class="table">
      <!-- 使用 v-if 控制 el-skeleton 的显示与隐藏 -->
      <el-skeleton class="table-skeleton" :rows="10" animated v-if="loading"/>
      <el-table v-else :data="filteredData" strip @selection-change="handleSelectionChange"
                height="70vh" class="table-style" empty-text="">
        <template v-if="!isSearch" slot="empty">
          <el-empty description=" ">
            <p class="emptyText"><span style='font-size: 18px;font-weight: bold'>这里还没有文件哦, 赶快上传吧</span></p>
          </el-empty>
          <el-button type="primary" @click="handleAdd" style="margin-bottom: 35px">上传文件</el-button>
        </template>
        <template v-else slot="empty">
          <el-empty description=" ">
            <p class="emptyText"><span style='font-size: 18px;font-weight: bold'>没有找到相关文件</span></p>
          </el-empty>
        </template>
        <el-table-column type="selection" width="55" align="center"></el-table-column>
        <el-table-column prop="id" label="序号" width="70" align="center" sortable></el-table-column>
        <el-table-column prop="name" label="文件名称" sortable>
          <template slot-scope="scope">
            <span v-html="highlightText(scope.row.name)"></span>
          </template>
        </el-table-column>
        <el-table-column label="是否文件夹">
          <template v-slot="scope">
            <span @click="handleFolderClick(scope.row)" style="cursor: pointer;">
              <i v-if="scope.row.folder" class="el-icon-folder"></i>
              <span v-else>文件</span>
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="path" label="文件路径" show-overflow-tooltip></el-table-column>
        <el-table-column prop="type" label="文件类型"></el-table-column>
        <el-table-column prop="size" label="文件大小"></el-table-column>
        <el-table-column label="创建时间">
          <template slot-scope="scope">
            <span v-if="scope.row.createTime != null">
            	{{ formatTime(scope.row.createTime) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="修改时间">
          <template slot-scope="scope">
            <span v-if="scope.row.updateTime != null">
            	{{ formatTime(scope.row.updateTime) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="delete" label="是否删除">
          <template v-slot="scope">
            <span v-if="scope.row.delete">是</span>
            <span v-else>否</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="180">
          <template v-slot="scope">
            <el-button size="mini" type="danger" plain @click="del(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
            v-if="false"
            background
            @current-change="handleCurrentChange"
            @size-change="handleSizeChange"
            :current-page="pageNum"
            :page-sizes="[5, 10, 20]"
            :page-size="pageSize"
            layout="total, prev, pager, next, sizes, jumper"
            :total="total">
        </el-pagination>
      </div>
    </div>


  </div>
</template>

<script>
export default {
  name: "DiskFiles",
  data() {
    return {
      tableData: [],  // 所有的数据
      pageNum: 1,   // 当前的页码
      pageSize: 10,  // 每页显示的个数
      total: 0,
      name: null,
      user: JSON.parse(localStorage.getItem('user') || '{}'),
      ids: [],

      requestCache: [], // 存储发送的请求 URL
      cacheIndex: -1,  // 缓存数组的当前索引
      maxCacheSize: 10, // 最大缓存大小

      isSearch: false,//当前是否处于查询模式
      searchText: "", // 搜索的关键字

      loading: true, // 控制加载动画的显示与隐藏
      folderId: 0, // 当前的文件夹的id
    }
  },
  created() {
    this.load(1)
  },
  computed: {
    // 计算属性，过滤出属性 delete 为 false 的数据
    filteredData() {
      return this.tableData.filter(item => !item.delete);
    },
    // 计算属性，根据 isSearch 属性和 searchText 动态生成高亮文本
    highlightText() {
      console.log(this.isSearch, this.searchText)
      return (name) => {
        if (this.isSearch && this.searchText) {
          // 使用正则表达式替换匹配的文本为带有样式的高亮文本
          return name.replace(new RegExp(this.searchText, 'gi'), match => {
            return `<span class="highlight">${match}</span>`;
          });
        } else {
          // 如果不是搜索状态，直接返回原始文本
          return name;
        }
      };
    },
  },
  methods: {
    del(id) {   // 单个删除
      this.$confirm('您确定删除吗？', '确认删除', {type: "warning"}).then(response => {
        this.$request.delete('/files/' + id).then(res => {
          if (res.code === '200') {   // 表示操作成功
            this.$message.success('操作成功')
            this.reload()
          } else {
            this.$message.error(res.msg)  // 弹出错误的信息
          }
        })
      }).catch(() => {
      })
    },
    handleSelectionChange(rows) {   // 当前选中的所有的行数据
      this.ids = rows.map(v => v.id)   //  [1,2]
    },
    delBatch() {   // 批量删除
      if (!this.ids.length) {
        this.$message.warning('请选择数据')
        return
      }
      this.$confirm('您确定要删除选中的所有文件吗？', '确认删除', {type: "warning"}).then(response => {
        this.$request.delete('files', {data: this.ids}).then(res => {
          if (res.code === '200') {   // 表示操作成功
            this.$message.success('操作成功')
            this.reload()
          } else {
            this.$message.error(res.msg)  // 弹出错误的信息
          }
        })
      }).catch(() => {
      })
    },
    load(pageNum) {  // 分页查询
      if (pageNum) this.pageNum = pageNum
      this.handleCacheAndGetFileRequest('/files')
    },
    selectAll(pageNum) {
      this.isSearch = true;
      this.searchText = this.name;
      //根据条件查询所有数据
      if (pageNum) this.pageNum = pageNum
      this.loading = true;
      this.requestCache = [];
      this.cacheIndex = 0;
      if (this.name === "") {

        this.getFileRequest("/files")
        return;
      }
      // this.requestCache = this.requestCache.slice(0, this.cacheIndex + 1)
      this.$request.get("/files/all", {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          name: this.name,
        }
      }).then(res => {
        this.loading = false;
        if (res.code !== '200') {
          this.$message.error(res.code + ":" + res.msg)  // 弹出错误的信息
        } else {
          this.tableData = res.data
          this.total = res.data.length
        }
      })
    },
    reset() {
      this.name = null
      this.isSearch = false;
      this.searchText = "";
      this.load(1)
    },
    reload() {
      this.getFileRequest(this.requestCache[this.cacheIndex])
    },
    // handleCurrentChange(pageNum) {
    //   if (this.isSearch) {
    //     this.selectAll(pageNum)
    //   } else {
    //     // this.load(pageNum)
    //   }
    //
    // },
    // handleSizeChange(pageSize) {
    //   this.pageSize = pageSize;
    //   if (this.isSearch) {
    //     this.selectAll(1)
    //   } else {
    //     // this.load(1)
    //   }
    // },
    formatTime(timestamp) {
      let date = new Date(parseInt(timestamp));
      // 获取年、月、日、时、分
      let year = date.getFullYear();
      let month = (date.getMonth() + 1).toString().padStart(2, '0');
      let day = date.getDate().toString().padStart(2, '0');
      let hour = date.getHours().toString().padStart(2, '0');
      let minute = date.getMinutes().toString().padStart(2, '0');

      // 拼接成你需要的格式，比如：YYYY-MM-DD HH:mm
      return `${year}-${month}-${day} ${hour}:${minute}`;
    },
    handleCacheAndGetFileRequest(url) {
      // 用于封装getFileRequest和addToCache方法，统一处理
      // 丢弃索引之后的缓存(因为用户已经重新点击了新的路径)
      this.requestCache = this.requestCache.slice(0, this.cacheIndex + 1)
      this.isSearch = false;
      this.addToCache(url)
      this.getFileRequest(url)
    },
    getFileRequest(url) {// 发送请求,获取数据。不要直接调用此方法，调用handleCacheAndGetFileRequest方法
      //前进和后退可以直接调用此方法。
      this.loading = true;
      this.$request.get(url, {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
        }
      }).then(res => {
        this.loading = false;
        if (res.code !== '200') {
          this.$message.error(res.code + ":" + res.msg)  // 弹出错误的信息
        } else {
          this.tableData = res.data
          this.total = res.data.length
        }
      })
    },
    addToCache(url) {
      // 将新的 URL 添加到缓存中
      this.requestCache.push(url)
      // 如果缓存大小超过限制，则丢弃最老的请求 URL
      if (this.requestCache.length > this.maxCacheSize) {
        this.requestCache.shift()
      }
      // 更新缓存索引
      this.cacheIndex = this.requestCache.length - 1
    },
    handleFolderClick(row) {
      if (row.folder) {
        this.handleCacheAndGetFileRequest('/files/folder/' + row.id)
        this.folderId = row.id
      }
    },
    backNavigation() {
      // 判断是否有可后退的请求
      if (this.cacheIndex > 0) {
        this.cacheIndex--
        this.getFileRequest(this.requestCache[this.cacheIndex])
      }
    },
    forwardNavigation() {
      // 判断是否有可前进的请求
      if (this.cacheIndex < this.requestCache.length - 1) {
        this.cacheIndex++
        this.getFileRequest(this.requestCache[this.cacheIndex])
      }
    },
    addFolder() {
      this.$request({
        url: '/files/folder',
        method: 'POST',
        data: {name: '新建文件夹', folderId: this.folderId}
      }).then(res => {
        if (res.code !== '200') {
          this.$message.error(res.code + ":" + res.msg)  // 弹出错误的信息
        } else {
          this.reload()
        }
      })
    },
    handleAdd() {

    },
  }
}
</script>

<style scoped>
.main-container {
  border-radius: 50px;
  background-color: #ffffff;
  height: 100%;
  width: 100%;
}

.table {
  background-color: #ffffff;
  height: 80%;
}

.blank {
  height: 3%
}

.operation {
  /*position: absolute;*/
  margin-left: 3%;
}

.backAndForward {
  margin-left: 5%;
}

::v-deep .search-input .el-input__inner {
  width: 100%;
  height: 5vh;
  background-color: #EBEEF5;
  text-align: center;
  border: 0 !important;
  outline: none;
  font-weight: bold;
  font-size: 14px;
  border-radius: 15px;
}

.table-skeleton {
  width: 80%;
  margin-left: 10%;
  margin-top: 5%;
}

::v-deep .highlight {
  /*background-color: yellow;*/
  color: #0d53ff;
  font-weight: bold;
  font-size: 15px;
}

.table-style {
  /*font-weight: bold;*/
  font-size: 14px;
}
</style>
