<template>
  <div class="main-container">
    <div class="blank"></div>
    <div class="operation">
      <el-button type="danger" plain @click="delBatch">批量清空</el-button>
      <el-button type="warning" plain @click="restoreBatch">批量还原</el-button>
      <!--      <el-button type="danger" plain @click="delAll">清空全部</el-button>-->
    </div>
    <div class="blank"></div>
    <div class="path">
      <span>回收站></span><span>共{{ this.total }}</span>
    </div>
    <div class="table">
      <!-- 使用 v-if 控制 el-skeleton 的显示与隐藏 -->
      <el-skeleton class="table-skeleton" :rows="10" animated v-if="loading"/>
      <el-table v-else :data="tableData" strip @selection-change="handleSelectionChange"
                height="70vh" class="table-style" empty-text="" @row-contextmenu="rightClick"
                ref="table">
        <template slot="empty">
          <el-empty description=" ">
            <p class="emptyText"><span style='font-size: 18px;font-weight: bold'>这里什么都没有</span></p>
          </el-empty>
        </template>
        <el-table-column type="selection" width="55" align="center"></el-table-column>
        <el-table-column prop="name" label="文件名称" sortable></el-table-column>
        <el-table-column label="是否文件夹">
          <template v-slot="scope">
            <span >
              <i v-if="scope.row.folder" class="el-icon-folder"></i>
              <span v-else>文件</span>
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="文件类型"></el-table-column>
        <el-table-column prop="size" label="文件大小" :formatter="formatSize"></el-table-column>
        <el-table-column prop="path" label="文件路径" show-overflow-tooltip></el-table-column>
        <el-table-column label="创建时间">
          <template v-slot="scope">
            <span v-if="scope.row.createTime != null">
            	{{ scope.row.createTime | formatTime }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="修改时间">
          <template
              v-slot="scope">
            <span v-if="scope.row.updateTime != null">
            	{{ scope.row.updateTime | formatTime }}
            </span>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <div id="contextmenu"
         v-show="menuVisible"
         class="menu">
      <div class="contextmenu__item"
           @click="del(CurrentRow.id)">彻底删除
      </div>
      <div class="contextmenu__item"
           @click="restore(CurrentRow.id)">还原
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "trash",
  data() {
    return {
      tableData: [],  // 所有的数据
      total: 0,
      name: null,
      user: JSON.parse(localStorage.getItem('user') || '{}'),
      ids: [],

      loading: true, // 控制加载动画的显示与隐藏

      menuVisible: false, // 右键菜单是否显示
    }
  },
  created() {
    // this.folderId = this.user.rootId
    this.load()
  },
  // computed: {
  //   // 计算属性，data变化以后自动更新
  //   filteredData() {
  //     let data = this.tableData
  //     this.total = data.length
  //     this.getPath(this.folderId)
  //     return data;
  //   },
  // },
  methods: {
    del(id) {   // 单个删除
      this.$confirm('确定要彻底删除选中的文件吗？\n此操作无法撤销!', '确认删除', {
        confirmButtonText: '彻底删除',
        cancelButtonText: '取消',
        type: 'error',
        center: true
      }).then(response => {
        this.$request.delete('/files/completely/' + id).then(res => {
          if (res.code === '200') {   // 表示操作成功
            this.$message.success('已彻底删除！')
            this.load()
          } else {
            this.$message.error(res.code + ":" + res.msg)  // 弹出错误的信息
          }
        })
      })
    },
    delBatch() {   // 批量删除
      if (!this.ids.length) {
        this.$message.warning('请选择数据')
        return
      }
      this.$confirm('确定要彻底删除选中的文件吗？\n此操作无法撤销!', '确认删除', {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning',
        center: true
      }).then(response => {
        this.$request.delete('/files/completely/batch', {data: this.ids}).then(res => {
          if (res.code === '200') {   // 表示操作成功
            this.$message.success('已彻底删除！')
            this.load()
          } else {
            this.$message.error(res.code + ": " + res.msg)  // 弹出错误的信息
          }
        })
      })
    },
    restore(id) {   // 单个还原
      this.$request.put('/files/restore/' + id).then(res => {
        if (res.code === '200') {   // 表示操作成功
          this.$message.success('还原成功')
          this.load()
        } else {
          this.$message.error(res.code + ":" + res.msg)  // 弹出错误的信息
        }
      })
    },
    restoreBatch() {   // 批量还原
      if (!this.ids.length) {
        this.$message.warning('请选择数据')
        return
      }
      //注意：这里后面的参数是data，不是config，delete和put的请求参数不同！！！
      this.$request.put('/files/restore/batch', this.ids).then(res => {
        if (res.code === '200') {   // 表示操作成功
          this.$message.success('操作成功')
          this.load()
        } else {
          this.$message.error(res.code + ": " + res.msg)  // 弹出错误的信息
        }
      })
    },
    handleSelectionChange(rows) {   // 当前选中的所有的行数据
      this.ids = rows.map(v => v.id)   //  [1,2]
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
    load() {
      this.loading = true;
      this.$request.get("/files/trash").then(res => {
        this.loading = false;
        if (res.code !== '200') {
          this.$message.error(res.code + ":" + res.msg)  // 弹出错误的信息
        } else {
          this.tableData = res.data
          this.total = res.data.length
        }
      })
    },
    // 右键菜单
    rightClick(row, column, event) {
      // 判断当前行是否已经被选中
      let isSelected = this.$refs.table.selection.includes(row);
      // 如果当前行未被选中，则添加到选中行列表中
      if (!isSelected) {
        this.$refs.table.toggleRowSelection(row);
      }
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
      return this.$options.filters.sizeFormat(row[column.property]);
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

.el-table .el-table__row--selected {
  background-color: blue; /* 修改被选中行的底色为蓝色 */
  color: white; /* 修改被选中行的文字颜色为白色 */
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
  /*font-weight: bold;*/
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
</style>
