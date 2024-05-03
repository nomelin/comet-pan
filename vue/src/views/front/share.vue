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
                :row-style="{height: '4rem'}"
                :cell-style="{padding: '0'}"
                ref="table">
        <template slot="empty">
          <el-empty description=" ">
            <p class="emptyText"><span style='font-size: 1.2rem;font-weight: bold'>还没有分享的文件,快去分享吧！</span></p>
          </el-empty>
        </template>
        <el-table-column type="selection" width="55" align="center"></el-table-column>
        <el-table-column label="" width="60">
          <img class="folder-icon" src="@/assets/imgs/分享.svg" alt="文件夹">
        </el-table-column>
        <el-table-column prop="name" label="分享名称" show-overflow-tooltip></el-table-column>
        <el-table-column  label="分享链接" show-overflow-tooltip min-width="400">
          <template v-slot="scope">
            <!-- 使用自定义渲染函数，将路径加上固定前缀 -->
            <span>{{ 'https://pan.nomelin.top/share/' + scope.row.path }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="days" label="剩余时间" show-overflow-tooltip></el-table-column>
        <el-table-column prop="code" label="访问密码" show-overflow-tooltip></el-table-column>
        <el-table-column prop="count" label="访问次数" show-overflow-tooltip></el-table-column>

      </el-table>
    </div>
    <div id="contextmenu"
         v-show="menuVisible"
         class="menu">
      <div class="contextmenu__item"
           @click="copyToClipboard(CurrentRow)">复制分享链接
      </div>
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
    this.$request.delete("/share/clean/" + this.user.id).then(res => {
      if (res.code !== '200') {
        this.$message.error(res.code + ":" + res.msg)  // 弹出错误的信息
      }
    })
    // this.folderId = this.user.rootId
    this.load()
  },
  computed: {
    computedData() {
      return this.tableData.map(item => {
        // 计算天数
        const shareTime = new Date(parseInt(item.shareTime));

        const endTime = item.endTime === "-1" ? null : new Date(parseInt(item.endTime)); // 如果是永久则设为null
        console.log(shareTime + ' ' + endTime)
        const days = endTime ? Math.floor((endTime - shareTime) / (1000 * 3600 * 24)) + '天后' : "永久";

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
    copyToClipboard(row) {
      navigator.clipboard.writeText('https://pan.nomelin.top/share/' + row.path);
      this.$message.success('复制成功,快分享给其它人吧！')
    }
  }
}
</script>

<style scoped>

.blank {
  height: 3%
}

.operation {
  /*position: absolute;*/
  margin-left: 3%;
}

::v-deep .highlight {
  /*background-color: yellow;*/
  color: #0d53ff;
  font-weight: bold;
  font-size: 15px;
}
.folder-icon {
  width: 100%;
}
</style>
