<template>
  <div class="main-container">
    <div class="blank"></div>
    <div class="operation">
      <el-button type="warning" class="normal-button" plain @click="delBatch">取消分享</el-button>
      <!--      <el-button type="primary" class="primary-button" plain @click="restoreBatch">批量还原</el-button>-->
      <!--      <el-button type="danger" plain @click="delAll">清空全部</el-button>-->
    </div>
    <div class="blank"></div>
    <div class="path">
      <span>我的分享></span><span>共{{ this.total }}</span>
    </div>
    <div class="table">
      <!-- 使用 v-if 控制 el-skeleton 的显示与隐藏 -->
      <el-skeleton class="table-skeleton" :rows="10" animated v-if="loading"/>
      <el-table v-else :data="computedData" strip @selection-change="handleSelectionChange"
                height="70vh" class="table-style" empty-text="" @row-contextmenu="rightClick"
                ref="table">
        <template slot="empty">
          <el-empty description=" ">
            <p class="emptyText"><span style='font-size: 18px;font-weight: bold'>还没有分享的文件,快去分享吧！</span></p>
          </el-empty>
        </template>
        <el-table-column type="selection" width="55" align="center"></el-table-column>
        <el-table-column label="" width="60">
          <img class="folder-icon" src="@/assets/imgs/folder.svg" alt="文件夹">
        </el-table-column>
        <el-table-column prop="name" label="分享名称" show-overflow-tooltip></el-table-column>
        <el-table-column prop="path" label="分享链接" show-overflow-tooltip min-width="200"></el-table-column>
        <el-table-column prop="days" label="剩余时间" show-overflow-tooltip></el-table-column>
        <el-table-column prop="code" label="访问密码" show-overflow-tooltip></el-table-column>
        <el-table-column prop="count" label="访问次数" show-overflow-tooltip></el-table-column>

      </el-table>
    </div>
    <div id="contextmenu"
         v-show="menuVisible"
         class="menu">
      <div class="contextmenu__item"
           @click="del(CurrentRow.id)">取消分享
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
  computed: {
    computedData() {
      return this.tableData.map(item => {
        // 计算天数
        const shareTime = new Date(item.shareTime);
        const endTime = item.endTime === "-1" ? null : new Date(item.endTime); // 如果是永久则设为null
        const days = endTime ? Math.ceil((endTime - shareTime) / (1000 * 3600 * 24)) +'天后' : "永久";

        // 设置访问密码
        const code = item.code.trim() ? item.code.trim() : "无访问密码";

        // 返回处理后的对象
        return {
          ...item,
          days: days,
          code: code
        };
      });
    },
  },

  methods: {
    del(id) {   // 单个删除
      this.$confirm('确定要取消分享吗?', '取消分享', {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning',
        center: true
      }).then(response => {
        this.$request.delete('/share/' + id).then(res => {
          if (res.code === '200') {   // 表示操作成功
            this.$message.success('已取消分享!')
            this.load()
          } else {
            this.$message.error(res.code + ":" + res.msg)  // 弹出错误的信息
          }
        })
      }).catch(() => {
      });
    },
    delBatch() {   // 批量删除
      if (!this.ids.length) {
        this.$message.warning('请选择数据')
        return
      }
      this.$confirm('确定要取消分享这些文件吗？', '取消分享', {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning',
        center: true
      }).then(response => {
        this.$request.delete('/share', {data: this.ids}).then(res => {
          if (res.code === '200') {   // 表示操作成功
            this.$message.success('已取消分享！')
            this.load()
          } else {
            this.$message.error(res.code + ": " + res.msg)  // 弹出错误的信息
          }
        })
      }).catch(() => {
      });
    },
    handleSelectionChange(rows) {   // 当前选中的所有的行数据
      this.ids = rows.map(v => v.id)   //  [1,2]
    },
    load() {
      this.loading = true;
      this.$request.get("/share/user/" + this.user.id).then(res => {
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
    // formatSize(row, column) {
    //   // row 是当前行的数据对象
    //   // column 是当前列的属性配置对象
    //   // 调用过滤器来格式化文件大小
    //   return this.$options.filters.sizeFormat(row[column.property]);
    // },
  }
}
</script>

<style scoped>
.main-container {
  /*border-radius: 50px;*/
  border-top-left-radius: 50px;
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
  font-size: 13px;
}

/*右键菜单*/
.contextmenu__item {
  display: block;
  line-height: 35px;
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
  width: 120px;
  height: 40px;
}

.name-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.opt-container {
  color: #606266;
  font-size: 16px;
  font-weight: bold;
}
</style>
