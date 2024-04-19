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


      <el-button class="normal-button" plain style="margin-left: 10px" @click="reset">重置</el-button>
      <!--      <img class="little-icon" src="@/assets/imgs/folder-add.svg" alt="">-->
      <el-button class="primary-button" type="primary" plain style="margin-left: 10px" @click="addFolder">
        + 新建文件夹
      </el-button>
      <el-button class="normal-button" type="danger" plain @click="delBatch">删除</el-button>

      <span style="color: #909399;margin-left: 10px ;font-size: 14px ; font-weight: bold">按钮仍在开发中</span>
    </div>
    <div class="blank"></div>
    <div class="backAndForward">
      <el-button type="primary" plain @click="backNavigation" icon="el-icon-back"
                 circle :disabled="cacheIndex <= 0"></el-button>
      <el-button type="primary" plain @click="forwardNavigation" icon="el-icon-right"
                 circle :disabled="cacheIndex >= requestCache.length - 1"></el-button>
      <div class="path">
        <span>全部文件</span><span>{{ this.path }} 共{{ this.total }}</span>
      </div>
    </div>

    <div class="table">
      <!-- 使用 v-if 控制 el-skeleton 的显示与隐藏 -->
      <el-skeleton class="table-skeleton" :rows="10" animated v-if="loading"/>
      <el-table v-else :data="filteredData" strip @selection-change="handleSelectionChange"
                height="66vh" class="table-style" empty-text="" @row-contextmenu="rightClick"
                ref="table" :default-sort="{prop: 'name', order: 'ascending'}">
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

        <el-table-column type="selection" min-width="30" align="center"></el-table-column>
        <!--        <el-table-column prop="id" label="序号" width="70" align="center" sortable></el-table-column>-->
        <el-table-column prop="folder" label="" width="60">
          <template v-slot="scope">
            <span @click="handleFolderClick(scope.row)" style="cursor: pointer;">
              <i v-if="scope.row.folder">
                <img class="folder-icon" src="@/assets/imgs/folder.svg" alt="文件夹">
              </i>
              <span v-else>
                <img class="folder-icon" src="@/assets/imgs/文件.svg" alt="文件">
              </span>
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="文件名称" min-width="200" show-overflow-tooltip
                         sortable :sort-method="customSortMethod" :sort-orders="['ascending', 'descending']">
          <template slot-scope="scope">
            <template v-if="!scope.row.isEditing">
              <!-- 如果type为空，则只显示name -->
              <span v-if="!scope.row.type" v-html="highlightText(scope.row.name)"
                    @click="handleFolderClick(scope.row)" style="cursor: pointer;"></span>
              <!-- 如果type不为空，则显示完整名称 -->
              <span v-else v-html="highlightText(scope.row.name + '.' + scope.row.type)"
                    @click="handleFolderClick(scope.row)" style="cursor: pointer;"></span>
            </template>
            <template v-else>
              <div style="display: flex; align-items: center;">
                <el-input class="rename-input" v-if="!scope.row.type" v-model="editedName"
                          @keyup.enter.native="saveRename(scope.row)"
                          @blur="cancelRename(scope.row)"></el-input>
                <el-input class="rename-input" v-else v-model="editedName" @keyup.enter.native="saveRename(scope.row)"
                          @blur="cancelRename(scope.row)"></el-input>
                <!-- 取消按钮 -->
                <el-button @click="cancelRename(scope.row)" size="mini" style="margin-left: 5px;">取消</el-button>
              </div>
            </template>
          </template>
        </el-table-column>

        <el-table-column prop="path" label="文件路径" show-overflow-tooltip></el-table-column>
        <el-table-column prop="type" label="文件类型"></el-table-column>
        <el-table-column prop="size" label="文件大小" :formatter="formatSize"
                         sortable :sort-method="customSortMethod" :sort-orders="['ascending', 'descending']">
        </el-table-column>
        <el-table-column prop="createTime" :formatter="formatTime" label="创建时间"></el-table-column>
        <el-table-column prop="updateTime" :formatter="formatTime" label="修改时间"
                         sortable :sort-method="customSortMethod" :sort-orders="['ascending', 'descending']">
          <!--                    <template slot-scope="scope">
                                <span v-if="scope.row.updateTime != null">
                                  {{ scope.row.updateTime | formatTime }}
                                </span>
                              </template>-->
        </el-table-column>
      </el-table>
    </div>
    <div id="contextmenu"
         v-show="menuVisible"
         class="menu">
      <div class="contextmenu__item"
           @click="rename(CurrentRow)">重命名
      </div>
      <div class="contextmenu__item"
           @click="del(CurrentRow.id)">删除
      </div>
      <div class="contextmenu__item"
           @click="move(CurrentRow.id)">移动到
      </div>
    </div>
    <template>
      <div class="dialog-files">
        <file-table-dialog :dialog-files-visible.sync="dialogFilesVisible" :src-id.sync="srcId"/>
        <!-- .sync 是 Vue.js 中的一种特殊语法，用于实现子组件和父组件之间双向绑定数据的功能。
        它可以简化父子组件之间的通信，特别是用于修改父组件中的 prop 数据。-->
      </div>
    </template>

  </div>
</template>

<script>
import FileTableDialog from "@/views/front/fileTableDialog";

export default {
  name: "DiskFiles",
  components: {FileTableDialog},
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
      maxCacheSize: 50, // 最大缓存大小

      isSearch: false,//当前是否处于查询模式
      searchText: "", // 搜索的关键字

      loading: true, // 控制加载动画的显示与隐藏
      folderId: -1, // 当前的文件夹的id
      path: "", // 当前路径

      menuVisible: false, // 右键菜单是否显示

      editedName: '',// 编辑文件的名称

      dialogFilesVisible: false, // 打开文件夹选择框的Dialog
      srcId: -1, // 移动的源文件夹id


    }
  },
  mounted() {
  },
  created() {
    this.load(1)
    this.folderId = this.user.rootId
  },
  computed: {
    // 计算属性，过滤出属性 delete 为 false 的数据,data变化以后自动更新
    filteredData() {
      // console.log(this.folderId)
      // let data = this.tableData.filter(item => !item.delete)
      let data = this.tableData
      this.total = data.length
      this.getPath(this.folderId)
      return data;
    },
    // 计算属性，根据 isSearch 属性和 searchText 动态生成高亮文本
    highlightText() {
      // console.log(this.isSearch, this.searchText)
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
  watch: {
    dialogFilesVisible(newVal, oldVal) {
      if (oldVal && !newVal) {
        // 当 dialogFilesVisible 从 true 变为 false 时执行的逻辑
        // console.log("dialogFilesVisible 从 true 变为 false")
        // 刷新当前页面
        setTimeout(() => {
          location.reload();
        }, 200); // 延迟200毫秒
      }
    }
  },
  methods: {
    customSortMethod(a, b) {
      let table = this.$refs.table
      // 获取排序状态对象
      let sortState = table.store.states;
      // 获取当前的排序字段
      let currentSortProp = sortState.sortProp;
      // 获取当前的排序顺序
      let currentSortOrder = sortState.sortOrder;
      // console.log(currentSortProp, currentSortOrder)
      if (a.folder === b.folder) {
        // 如果两个值相等，按照要排序的字段排序
        if (currentSortProp === 'name') {
          return a.name.localeCompare(b.name);
        } else if (currentSortProp === 'updateTime') {
          return a.updateTime - b.updateTime
        } else if (currentSortProp === 'size') {
          return a.size - b.size
        }
      } else if (a.folder) {
        if (currentSortOrder === 'ascending') {
          return -1;
        } else if (currentSortOrder === 'descending') {
          return 1;
        }
      } else if (b.folder) {
        if (currentSortOrder === 'ascending') {
          return 1;
        } else if (currentSortOrder === 'descending') {
          return -1;
        }
      }
    },
    del(id) {   // 单个删除
      this.$confirm('确定要删除选中的文件吗？', '确认删除', {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning',
        center: true
      }).then(response => {
        this.$request.delete('/files/' + id).then(res => {
          if (res.code === '200') {   // 表示操作成功
            this.$message.success('删除成功')
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
      this.$confirm('确定要删除选中的文件吗？', '确认删除', {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning',
        center: true
      }).then(response => {
        this.$request.delete('/files', {data: this.ids}).then(res => {
          if (res.code === '200') {   // 表示操作成功
            this.$message.success('删除成功')
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
      this.folderId = this.user.rootId
    },
    selectAll(pageNum) {
      this.folderId = this.user.rootId

      this.isSearch = true;
      this.searchText = this.name;
      //根据条件查询所有数据
      if (pageNum) this.pageNum = pageNum
      this.loading = true;
      this.requestCache = [];
      this.cacheIndex = 0;
      if (this.name === null || this.name === "") {
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
        }
      })
    },
    getPath(id) {
      this.$request.get('/files/file/' + id).then(res => {
        if (res.code === '200') {
          this.path = res.data.path.replace(/\//g, '>'); // 使用正则表达式替换所有匹配到的 /
        } else {
          this.$message.error(res.code + ": " + res.msg)  // 弹出错误的信息
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
        let folderId = this.requestCache[this.cacheIndex].split('/')[3]
        if (!isNaN(folderId)) {
          this.folderId = folderId//如果是数字，则更新文件夹id
        } else {
          this.folderId = this.user.rootId//如果不是数字，则说明回到根目录
        }
        this.getFileRequest(this.requestCache[this.cacheIndex])

      }
    },
    forwardNavigation() {
      // 判断是否有可前进的请求
      if (this.cacheIndex < this.requestCache.length - 1) {
        this.cacheIndex++
        let folderId = this.requestCache[this.cacheIndex].split('/')[3]
        if (!isNaN(folderId)) {
          this.folderId = folderId//如果是数字，则更新文件夹id
        } else {
          this.folderId = this.user.rootId//如果不是数字，则说明回到根目录
        }
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
    // 右键菜单
    rightClick(row, column, event) {
      // 判断当前行是否已经被选中
      //let isSelected = this.$refs.table.selection.includes(row);
      // 如果当前行未被选中，则添加到选中行列表中
      this.$refs.table.clearSelection();//清空选中行列表
      //if (!isSelected) {
      this.$refs.table.toggleRowSelection(row);
      //}
      this.testModeCode = row.testModeCode
      this.menuVisible = false // 先把模态框关死，目的是 第二次或者第n次右键鼠标的时候 它默认的是true
      this.menuVisible = true // 显示模态窗口，跳出自定义菜单栏
      event.preventDefault() //关闭浏览器右键默认事件
      this.CurrentRow = row
      var menu = document.querySelector('.menu')
      this.styleMenu(menu)
    },
    foo() {
      // 取消鼠标监听事件 菜单栏
      this.menuVisible = false
      document.removeEventListener('click', this.foo) // 关掉监听，
    },
    styleMenu(menu) {
      if (event.clientX > 1800) {
        menu.style.left = event.clientX - 100 + 'px'
      } else {
        menu.style.left = event.clientX + 1 + 'px'
      }
      document.addEventListener('click', this.foo) // 给整个document新增监听鼠标事件，点击任何位置执行foo方法
      if (event.clientY > 700) {
        menu.style.top = event.clientY - 30 + 'px'
      } else {
        menu.style.top = event.clientY - 10 + 'px'
      }
    },
    formatSize(row, column) {
      // row 是当前行的数据对象
      // column 是当前列的属性配置对象
      // 调用过滤器来格式化文件大小
      return this.$options.filters.sizeFormat(row[column.property], 1);
    },
    formatTime(row, column) {
      return this.$options.filters.formatTime(row[column.property]);
    },
    // 重命名操作
    rename(row) {
      // 遍历数据源中的每一行，将其 isEditing 属性设为 false
      this.tableData.forEach(item => {
        this.$set(item, 'isEditing', false);
      });
      // 将当前行设置为正在编辑状态
      this.$set(row, 'isEditing', true);
      this.editedName = row.name + (row.type ? '.' + row.type : '');
    },
    // 保存重命名后的名称
    saveRename(row) {
      console.log(row.name, row.type)
      console.log(this.editedName)
      const [name, type] = this.editedName.split('.');
      row.name = name;
      row.type = type;
      // 发送后端接口请求保存重命名后的名称
      let newName = row.name;
      if (!row.folder) {
        newName = newName + "." + row.type;
      }
      this.$request.put('/files/rename', {id: row.id, name: newName}).then(res => {
        if (res.code === '200') {
          this.$message.success('重命名成功')
          this.reload()
        } else {
          this.$message.error(res.code + ": " + res.msg)  // 弹出错误的信息
        }
      })
      this.$set(row, 'isEditing', false);
      this.reload();
    },
    // 取消重命名操作
    cancelRename(row) {
      if (row.isEditing) {
        // 取消编辑状态，恢复原始名称
        this.$set(row, 'isEditing', false);
        this.$message.info('取消重命名')
        this.reload();
      }
    },
    move(id) {
      this.srcId = id
      this.dialogFilesVisible = true
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
  height: 75%;
}

/* 设置 el-table 每一行的高度 */
::v-deep .el-table .el-table__body .el-table__row {
  height: 60px; /* 设置每一行的高度 */
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
  width: 80%;
  display: flex;
  justify-content: flex-start; /* 靠左对齐 */
}


.path {
  font-weight: bold;
  font-size: 16px;
  color: #999999;
  margin-left: 3%;
  display: flex;
  align-items: center; /* 垂直居中 */
  /*text-align: left;*/
  /*display: flex;*/
  height: 5%;
  /*text-align: center;*/
  /*background-color: #f5f6f7;*/
  /*border-radius: 5px;*/
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
  font-weight: bold;
  font-size: 14px;
}

/*右键菜单*/
.contextmenu__item {
  display: block;
  line-height: 34px;
  text-align: center;
}

/*分割线*/
.contextmenu__item:not(:last-child) {
  border-bottom: 0px solid #00ffff;
}

.menu {
  position: absolute;
  background-color: #fff;
  width: 10%;
  /*height: 106px;*/
  font-size: 14px;
  font-weight: bold;
  color: #52565e;
  border-radius: 10px;
  -webkit-box-sizing: border-box;
  box-sizing: border-box;
  border: 1px solid #DCDFE6;
  /*box-shadow: 0 6px 12px rgba(0, 0, 0, 0.175);*/
  white-space: nowrap;
  z-index: 1000;
}

.contextmenu__item:hover {
  cursor: pointer;
  background: #e6f1ff;
  border-color: #e6f1ff;
  /*color: #52565e;*/
}

::v-deep .rename-input .el-input__inner {
  overflow: visible;
  width: 100%;
  text-align: left;
  border: 0 !important;
  outline: none;
  font-size: 15px;
  font-weight: bold;
}

.dialog-files {
  z-index: 999;
}

.folder-icon {
  width: 100%;
}

.little-icon {
  width: 50px;
  vertical-align: middle;
}


.primary-button {
  background-color: #0d53ff;
  color: #fff;
  border-radius: 10px;
  font-weight: bold;
  font-size: 16px;
  width: 120px;
  height: 40px;
}

.normal-button {
  background-color: #ffffff;
  color: #606266;
  border-radius: 10px;
  font-weight: bold;
  font-size: 16px;
  width: 80px;
  height: 40px;
}
</style>
