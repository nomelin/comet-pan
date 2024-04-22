<template>
  <div class="front-layout">
    <div class="front-header">
      <div class="front-header-left">
        <a href="/files">
          <img class="logo" src="@/assets/imgs/logo.svg" alt="logo">
        </a>
      </div>
      <div class="front-header-center">

      </div>
      <div class="front-header-right">

      </div>
    </div>
    <!--主体-->
    <div class="main-body">
      <div class="main-left">
        <div class="main-left-upper" @click="login">
          <div class="user-info">
            <el-avatar class="avatar" :src="$baseUrl+'/avatar/'+user.id" shape="circle"
                       fit="contain" size="large" alt=""></el-avatar>
            <div class="user-name">
              <span v-if="isLogin">{{ user.name }}</span>
              <span v-else>点此登录</span>
            </div>

          </div>
          <el-progress v-if="isLogin"
                       :text-inside="false" :show-text=false :stroke-width="8" :percentage="percentage"
                       :color="progressColor"
                       class="progress"></el-progress>
          <div class="user-space-info" v-if="isLogin">
            <span class="user-space"> {{
                usedSpace| sizeFormat
              }} / {{ totalSpace | sizeFormat }}</span>
          </div>
        </div>
        <el-menu v-if="isLogin" text-color="#565757" active-text-color="#0d53ff" router class="el-menu"
                 :default-active="$route.path">
          <el-menu-item class="el-menu-item" index="/files">
            <i class="el-icon-folder-opened"></i>
            <span class="words" slot="title">全部文件</span>
          </el-menu-item>
          <!--          <el-menu-item class="el-menu-item" index="/upload">-->
          <!--            <i class="el-icon-upload"></i>-->
          <!--            <span class="words" slot="title">上传文件</span>-->
          <!--          </el-menu-item>-->
          <el-menu-item class="el-menu-item" index="/share">
            <i class="el-icon-share"></i>
            <span class="words" slot="title">我的分享</span>
          </el-menu-item>

          <el-menu-item class="el-menu-item" index="/person">
            <i class="el-icon-user-solid"></i>
            <span class="words" slot="title">个人中心</span>
          </el-menu-item>

          <el-menu-item class="el-menu-item" index="/trash">
            <i class="el-icon-delete-solid"></i>
            <span class="words" slot="title">回收站</span>
          </el-menu-item>
          <el-menu-item class="el-menu-item" @click="logout">
            <i class="el-icon-switch-button"></i>
            <span class="words" slot="title">退出登录</span>
          </el-menu-item>
        </el-menu>
        <el-menu v-else text-color="#565757" active-text-color="#0d53ff" router class="el-menu">
        </el-menu>
      </div>


      <div class="main-right">
        <div class="main-container">

          <div class="blank"></div>

          <div class="operation">
            <el-button class="normal-button"  plain style="margin-left: 10px" @click="">
              <i class="el-icon-refresh"></i> 重置
            </el-button>
          </div>

          <div class="blank"></div>

          <div class="table">
            <!-- 使用 v-if 控制 el-skeleton 的显示与隐藏 -->
            <el-skeleton class="table-skeleton" :rows="10" animated v-if="loading"/>
            <el-table v-else :data="tableData" strip @selection-change="handleSelectionChange"
                      height="66vh" class="table-style" empty-text=""
                      ref="table" :default-sort="{prop: 'name', order: 'ascending'}"
            >

              <el-table-column type="selection" min-width="30" align="center"></el-table-column>
              <el-table-column prop="folder" label="" width="60">
                <template v-slot="scope">
            <span style="cursor: pointer;">
              <i v-if="scope.row.folder">
                <img class="folder-icon" src="@/assets/imgs/folder.svg" alt="文件夹">
              </i>
              <span v-else>
                <img class="folder-icon" src="@/assets/imgs/文件.svg" alt="文件">
              </span>
            </span>
                </template>
              </el-table-column>
              <el-table-column prop="name" label="文件名称" min-width="180" show-overflow-tooltip
                               sortable :sort-method="customSortMethod" :sort-orders="['ascending', 'descending']"
              >

              </el-table-column>
            </el-table>

            <el-dialog :visible.sync="dialogVisible" :close-on-click-modal="false" :close-on-press-escape="false"
                       title="请输入访问密码">
              <el-input v-model="password" type="password" placeholder="四位访问码"></el-input>
              <span slot="footer" class="dialog-footer">
        <el-button @click="checkCode">提交</el-button>
      </span>
            </el-dialog>
          </div>
        </div>
      </div>
    </div>
  </div>

</template>

<script>

export default {
  name: "FrontLayout",

  data() {
    return {
      top: '',
      user: JSON.parse(localStorage.getItem("user") || '{}'),

      totalSpace: 1024 * 1024 * 1024, // 1GB 总空间
      usedSpace: 0, // 从后端接口获取的已用空间（以字节为单位）
      tableData: [],  // 所有的数据
      total: 0,
      ids: [], // 选中的行的 id
      dialogVisible: false,
      password: '',
      loading: false,
      code: '',

    }
  },
  created() {
    this.getFileRequest()
  },

  mounted() {
    if (this.isLogin) {
      this.getUsedSpace()
    }
  },
  updated() {
    if (this.isLogin) {
      this.getUsedSpace()
    }
  },
  computed: {
    isLogin() {
      return this.user.id && this.user.id !== 'null' && this.user.id !== 'undefined';
    },
    // 计算已用空间占比
    percentage() {
      return (this.usedSpace / this.totalSpace) * 100;
    },
    // 根据占比确定进度条颜色
    progressColor() {
      if (this.percentage >= 90) {
        return '#cb2b3d'; // 大于等于 90% 显示红色
      } else if (this.percentage >= 75) {
        return '#d76a24'; // 大于等于 75% 显示橙色
      } else if (this.percentage >= 50) {
        return '#ffd100'; // 大于等于 50% 显示黄色
      } else {
        return '#149958'; // 小于 50% 显示绿色
      }
    },
    // 计算属性，过滤出属性 delete 为 false 的数据,data变化以后自动更新

  },
  methods: {
    getUsedSpace() {
      this.$request.get('/files/space').then(res => {
        if (res.code === '200') {
          this.usedSpace = res.data
        } else {
          this.$message.error(res.code + ": " + res.msg)
        }
      })
    },
    updateUser() {
      this.user = JSON.parse(localStorage.getItem('user') || '{}')   // 重新获取下用户的最新信息
    },
    // 退出登录
    logout() {
      this.$confirm('确定要退出吗？', '确认退出', {
        confirmButtonText: '退出',
        cancelButtonText: '取消',
        type: 'success',
        center: true
      }).then(response => {
        localStorage.removeItem("user");
        this.$router.push("/login");
      }).catch(error => {
        // 用户点击了取消按钮，可以在这里处理取消事件，比如关闭对话框
      });
    },
    goToUserProfile() {
      this.$router.push('/person');
    },
    login() {
      if (this.isLogin) {
        return;
      }
      // 假设当前页面路径为 currentPath
      const currentPath = window.location.href;
      // 重定向到登录页面，并附加当前页面路径作为参数
      window.location.href = `/login?redirect=${encodeURIComponent(currentPath)}`;
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
    handleSelectionChange(rows) {   // 当前选中的所有的行数据
      this.ids = rows.map(v => v.id)   //  [1,2]
    },
    checkCode() {
      if (this.password === this.code) {
        this.dialogVisible = false;
        this.getFiles()
      } else {
        this.$message.error("访问码错误")
      }
    },
    getFileRequest() {
      this.loading = true;
      // 获取当前 URL 的路径部分
      const pathname = window.location.pathname;
// 使用字符串的 split 方法分割路径，并取最后一部分
      const parts = pathname.split('/');
      const lastPart = parts[parts.length - 1];
// 如果想要去掉可能存在的查询参数部分，可以继续使用 split 方法再次分割
      const finalPart = lastPart.split('?')[0];
// 最后的字符串即为所需的部分
      console.log(finalPart);
      this.$request.get('/share/' + finalPart,
      ).then(res => {
        this.loading = false;
        if (res.code !== '200') {
          this.$message.error(res.code + ":" + res.msg)  // 弹出错误的信息
        } else {
          this.code = res.data.code
          this.fileIds = res.data.fileIds.split(",").map(function (item) {
            return parseInt(item);
          });
          if (this.code === "") {
            this.getFiles()
          } else {
            this.dialogVisible = true;
          }
        }
      })
    },
    getFiles() {
      console.log("get:" + JSON.stringify(this.fileIds))

      this.$request.post('/files/share/batch', JSON.stringify(this.fileIds)).then(res => {
        if (res.code !== '200') {
          this.$message.error(res.code + ":" + res.msg)  // 弹出错误的信息
        } else {
          this.tableData = res.data
          this.total = res.data.length
        }
      })
    },

  }

}
</script>

<style scoped>
.front-layout {
  /*display: flex;*/
  height: 100vh;
}

.main-body {
  display: flex;
  /*height: 100%;*/
  height: 90vh;
  /*flex-grow: 1;*/
}


.main-left {
  width: 18vw;
  height: 100%;
}

.main-left-upper {
  background-color: #ffffff;
  border: none;
  border-radius: 20px;
  height: 15%;
  width: 80%;
  margin-left: 10%;
  margin-top: 10%;
  /*position: relative; !* 设置相对定位，为了让进度条容器相对于该 div 定位 *!*/
}

.user-info {
  display: flex; /* 使用 Flexbox 布局 */
  align-items: center; /* 垂直方向上居中对齐 */
  /*position: absolute; !* 设置绝对定位 *!*/
  padding-left: 10%;
  padding-top: 10%;
}

.avatar {
  margin-right: 20px; /* 头像与用户名之间的间距 */
}

.user-name {
  font-size: 16px; /* 可根据需要调整用户名的样式 */
  font-weight: bold; /* 加粗 */
  color: #333333; /* 字体颜色 */
}

.progress {
  /*position: absolute; !* 设置绝对定位 *!*/
  margin-top: 10%;
  padding-left: 5%;
  width: 90%;
}

.el-menu {
  border: none;
  border-radius: 20px;
  height: 70%;
  width: 80%;
  margin-left: 10%;
  margin-top: 10%;
}

.el-menu-item {
  /*padding-top: 20px; !* 增加上边距 *!*/
  margin-top: 10px; /* 修正菜单项的高度 */
}

.el-menu-item:hover {
  padding-top: 2px; /* 增加上边距 */
  margin-top: 10px; /* 修正菜单项的高度 */
}

.main-right {
  width: 100%;
  height: 100%;
  border-top-left-radius: 50px;
}

.front-layout {
  background: #ebeef5;
}

/*.front-notice {*/
/*  height: 0;*/
/*  padding: 5px 20px;*/
/*  color: #666;*/
/*  font-size: 12px*/
/*}*/
.logo {
  width: 10vw;
  margin-top: 30px;
}

.front-header {
  display: flex;
  height: 10vh;
  width: 100vw;
  /*line-height: 60px;*/
  /*border-bottom: 1px solid #eee;*/
  /*background: #f5f6f7;*/
}

.front-header-left {
  width: 400px;
  /*display: flex;*/
  /*align-items: center;*/
  padding-left: 30px;
}

.front-header-dropdown img {
  width: 40px;
  height: 40px;
  border-radius: 50%
}

.front-header-center {
  flex: 1;
}

.front-header-right {
  width: 200px;
  padding-right: 20px;
  text-align: right;
}

.front-header-dropdown {
  display: flex;
  align-items: center;
  justify-content: right;
}

.el-dropdown-menu {
  width: 100px !important;
  text-align: center !important;
}

/*页面具体样式自定义*/
.main-content {
  width: 100%;
  margin: 5px auto;
}

/* ElementUI 样式覆盖 */
/*.el-menu.el-menu--horizontal {*/
/*  border: none !important;*/
/*  height: 80px;*/
/*  border-radius: 10px;*/
/*}*/


.words {
  font-weight: bold;
}

/*::v-deep .el-menu-item .is-active {*/
/*  background-color: #3370ff !important;*/
/*  color: #fff;*/
/*}*/
.user-space-info {
  width: 80%;
}

.user-space {
  display: inline-block; /* 将 span 元素设置为行内块级元素 */
  font-weight: bold;
  font-size: 12px;
  width: 100%; /* 设置宽度为100% */
  text-align: right; /* 将文本内容右对齐 */
  box-sizing: border-box; /* 使用边框盒模型，确保宽度包含 padding 和 border */
}

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
  width: 130px;
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

.divider {
  height: 0;
}

</style>