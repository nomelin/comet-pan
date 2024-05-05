<template>
  <div class="main-container">
    <div class="blank"></div>
    <div class="operation">
      <el-button type="danger" class="normal-button" plain @click="delBatch">批量清空</el-button>
      <el-button type="primary" class="primary-button" plain @click="restoreBatch">批量还原</el-button>
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
                :default-sort="{prop: 'name', order: 'ascending'}"
                :row-style="{height: '4rem'}"
                :cell-style="{padding: '0'}"
                ref="table">
        <template slot="empty">
          <el-empty description=" ">
            <p class="emptyText"><span style='font-size: 1.2rem;font-weight: bold'>这里什么都没有</span></p>
          </el-empty>
        </template>
        <el-table-column type="selection" width="55" align="center"></el-table-column>
        <el-table-column prop="folder" label="" width="60">
          <template v-slot="scope">
            <span>
              <FileIcon :file-type="scope.row.type" :isFolder="scope.row.folder"></FileIcon>
            </span>
          </template>
        </el-table-column>
        <!--        <el-table-column prop="name" label="文件名称" sortable min-width="200" show-overflow-tooltip></el-table-column>-->
        <!--        <el-table-column label="是否文件夹">-->
        <!--          <template v-slot="scope">-->
        <!--            <span >-->
        <!--              <i v-if="scope.row.folder" class="el-icon-folder"></i>-->
        <!--              <span v-else>文件</span>-->
        <!--            </span>-->
        <!--          </template>-->
        <!--        </el-table-column>-->
        <el-table-column prop="name" label="文件名称" min-width="150" show-overflow-tooltip
                         sortable :sort-method="customSortMethod" :sort-orders="['ascending', 'descending']"
        >
          <template v-slot="scope">
            <template>
              <div class="name-container">
                <!-- 如果type为空，则只显示name -->
                <span v-if="!scope.row.type" v-html="highlightText(scope.row.name)"></span>
                <!-- 如果type不为空，则显示完整名称 -->
                <span v-else v-html="highlightText(scope.row.name + '.' + scope.row.type)"></span>

              </div>

            </template>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="文件类型"></el-table-column>
        <el-table-column prop="size" label="文件大小" :formatter="formatSize"></el-table-column>
        <el-table-column prop="path" label="文件路径" show-overflow-tooltip min-width="150"></el-table-column>
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
           @click="restore(CurrentRow.id)">还原
      </div>
      <div class="contextmenu__item"
           @click="del(CurrentRow.id)">彻底删除
      </div>
    </div>
  </div>
</template>

<script>
import FileIcon from "@/views/FileIcon";
import {setItemWithExpiry} from "@/App"
import {getItemWithExpiry} from "@/App"
import {updateItemWithExpiry} from "@/App"
export default {

  name: "trash",
  components: {FileIcon},
  data() {
    return {
      // extension: fileExtensions,

      tableData: [],  // 所有的数据
      total: 0,
      name: null,
      user: getItemWithExpiry("user"),
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
    highlightText() {
      // console.log(this.isSearch, this.searchText)
      return (name) => {
        return name;
      }
    }
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
        type: 'warning',
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
      }).catch(() => {
      });
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
      }).catch(() => {
      });
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
      let clientX = event.clientX
      let clientY = event.clientY
      let menuWidth = menu.offsetWidth
      let menuHeight = menu.offsetHeight
      if (menuWidth === 0) {
        menuWidth = 150
      }
      if (menuHeight === 0) {
        menuHeight = 250
      }
      let windowWidth = window.innerWidth
      let windowHeight = window.innerHeight
      console.log(clientX, clientY, menuWidth, menuHeight, windowWidth, windowHeight)
      if (clientX + menuWidth + 10 > windowWidth) {
        menu.style.left = clientX - menuWidth - 2 + 'px';
      } else {
        menu.style.left = clientX + 2 + 'px';
      }
      document.addEventListener('click', this.foo);
      if (clientY + menuHeight + 10 > windowHeight) {
        menu.style.top = clientY - menuHeight - 2 + 'px';
      } else {
        menu.style.top = clientY + 2 + 'px';
      }
    },
    formatSize(row, column) {
      // row 是当前行的数据对象
      // column 是当前列的属性配置对象
      // 调用过滤器来格式化文件大小
      return this.$options.filters.sizeFormat(row[column.property]);
    },
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

.name-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/*!* 设置 el-table 每一行的高度 *!*/
/*::v-deep .el-table .el-table__body .el-table__row {*/
/*  height: 2.5rem; !* 设置每一行的高度 *!*/
/*}*/
</style>
