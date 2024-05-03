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
            <el-avatar v-if="isLogin" class="avatar" :src="$baseUrl+'/avatar/'+user.id" shape="circle"
                       fit="contain" size="large" alt=""></el-avatar>
            <el-avatar v-else class="avatar" :src="$baseUrl+'/avatar/'+0" shape="circle"
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

          <div class="operation" style="display: flex; justify-content: space-between; align-items: center;">
            <el-button class="primary-button" plain @click="mergeShare">
              保存到我的文件
            </el-button>

            <div style="display: flex; align-items: center; background-color: #f5f6f8;
            padding: 1rem 2rem;border-radius: 2rem;">
              <el-avatar class="avatar" :src="$baseUrl+'/avatar/'+shareUser.id"
                         shape="circle" fit="contain" size="large" alt=""></el-avatar>
              <div class="user-name" style="margin-left: 1rem;">
                <span>来自 [{{ shareUser.name }}] 的分享</span>
              </div>
            </div>
          </div>


          <div class="blank"></div>

          <div class="table">
            <!-- 使用 v-if 控制 el-skeleton 的显示与隐藏 -->
            <el-skeleton class="table-skeleton" :rows="10" animated v-if="loading"/>
            <el-table v-else :data="tableData" strip @selection-change="handleSelectionChange"
                      height="66vh" class="table-style" empty-text=""
                      :row-style="{height: '4rem'}"
                      :cell-style="{padding: '0'}"
                      ref="table" :default-sort="{prop: 'name', order: 'ascending'}"
            >
              <template v-if="!checked" slot="empty">
                <el-empty description=" ">
                  <p class="emptyText"><span
                      style='font-size: 18px;font-weight: bold'>这些分享文件已经被加密了,请输入访问码进行查看</span></p>
                </el-empty>
                <el-button type="primary" @click="dialogVisible = true" class="primary-button">输入访问码</el-button>
              </template>
              <template v-else slot="empty">
                <el-empty description=" ">
                  <p class="emptyText"><span style='font-size: 18px;font-weight: bold'>没有找到相关文件</span></p>
                </el-empty>
              </template>

              <el-table-column type="selection" min-width="30" align="center"></el-table-column>
              <el-table-column prop="folder" label="" width="60">
                <template v-slot="scope">
            <span>
              <FileIcon :file-type="scope.row.type" :isFolder="scope.row.folder"></FileIcon>
            </span>
                </template>
              </el-table-column>
              <el-table-column prop="name" label="文件名称" min-width="180" show-overflow-tooltip
                               sortable :sort-method="customSortMethod" :sort-orders="['ascending', 'descending']"
              >

              </el-table-column>


            </el-table>

            <el-dialog :visible.sync="dialogVisible" :close-on-click-modal="false" :close-on-press-escape="false"
                       :show-close="false" width="30%" center title="请输入访问密码">
              <el-input v-model="password" type="password" placeholder="四位访问码" maxlength="4"
                        minlength="4" @keyup.enter.native="checkCode"></el-input>
              <span slot="footer" class="dialog-footer">
                <el-button class="normal-button" type="primary" @click="checkCode">提交</el-button>
              </span>
            </el-dialog>

            <template>
              <div class="dialog-files">
                <file-table-dialog :dialog-files-visible.sync="dialogFilesVisible" :src-ids.sync="ids"
                                   :type.sync="type"/>
              </div>
            </template>


          </div>
        </div>
      </div>
    </div>
  </div>

</template>

<script>
import FileTableDialog from "@/views/front/fileTableDialog";
// import {fileExtensions} from "@/utils/const.js";
import FileIcon from "@/views/FileIcon";

export default {
  name: "publicShare",
  components: {FileTableDialog,FileIcon},

  data() {
    return {

      // extension: fileExtensions,

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

      checked: false,//是否验证过密码

      shareUser: null, //分享者信息

      dialogFilesVisible: false,

      type: 'mergeShare',

    }
  },
  created() {
    this.getFileRequest()
  },

  mounted() {
    if (this.isLogin) {
      console.log("login" + this.isLogin)
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
      return typeof this.user === 'object' && Object.keys(this.user).length > 0;
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
        this.checked = true;
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
          this.shareName = res.data.name
          this.getShareUser(res.data.userId)
          this.fileIds = res.data.fileIds.split(",").map(function (item) {
            return parseInt(item);
          });
          if (this.code === "") {
            this.checked = true;
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
    getShareUser(userId) {
      this.$request.get('/users/' + userId).then(res => {
        if (res.code === '200') {
          this.shareUser = res.data
        } else {
          this.$message.error(res.code + ":" + res.msg)  // 弹出错误的信息
        }
      }).catch(err => {
        console.log(err)
      })
    },
    mergeShare() {
      console.log("merge:" + JSON.stringify(this.ids))
      if (this.isLogin === false) {
        this.$message.info("请先登录")
        return
      }
      this.dialogFilesVisible = true;
    }

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
  margin-right: 2rem; /* 头像与用户名之间的间距 */
}

.user-name {
  font-size: 1rem; /* 可根据需要调整用户名的样式 */
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
  border-radius: 1rem;
  height: 70%;
  width: 80%;
  margin-left: 10%;
  margin-top: 10%;
}

.el-menu-item {
  /*padding-top: 20px; !* 增加上边距 *!*/
  margin-top: 1rem; /* 修正菜单项的高度 */
}

.el-menu-item:hover {
  padding-top: 2px; /* 增加上边距 */
  margin-top: 1rem; /* 修正菜单项的高度 */
}

.main-right {
  width: 100%;
  height: 100%;
  border-top-left-radius: 4rem;
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
  margin-top: 2rem;
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
  width: 50%;
  /*display: flex;*/
  /*align-items: center;*/
  padding-left: 3%;
}

.front-header-center {
  flex: 1;
}

.front-header-right {
  width: 30%;
  padding-right: 5%;
  text-align: right;
}

.words {
  font-weight: bold;
}

.user-space-info {
  width: 80%;
}

.user-space {
  display: inline-block; /* 将 span 元素设置为行内块级元素 */
  font-weight: bold;
  font-size: 0.8rem;
  width: 100%; /* 设置宽度为100% */
  text-align: right; /* 将文本内容右对齐 */
  box-sizing: border-box; /* 使用边框盒模型，确保宽度包含 padding 和 border */
}


.blank {
  height: 3%
}

.operation {
  /*position: absolute;*/
  width: 80%;
  margin-left: 3%;
}
</style>