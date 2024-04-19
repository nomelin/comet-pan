<template>
  <div class="front-layout">
    <!--    <div class="front-notice"><i class="el-icon-bell" style="margin-right: 2px"></i>公告：{{ top }}</div>-->
    <!--头部-->
    <div class="front-header">
      <div class="front-header-left">
        <a href="/files">
          <img class="logo" src="@/assets/imgs/logo.svg" alt="logo">
        </a>
      </div>
      <div class="front-header-center">
        <!--        <el-breadcrumb separator-class="el-icon-arrow-right">
                  <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
                  <el-breadcrumb-item :to="{ path: $route.path }">{{ $route.meta.name }}</el-breadcrumb-item>
                </el-breadcrumb>-->
      </div>
      <div class="front-header-right">
        <!--        <div v-if="!user.userName">
                  <el-button @click="$router.push('/login')">登录</el-button>
                  <el-button @click="$router.push('/register')">注册</el-button>
                </div>
                <div v-if="user.userName">
                  <el-dropdown>
                    <div class="front-header-dropdown">
                      <img :src="$baseUrl+'/avatar/'+user.id" alt="">
                      <div style="margin-left: 10px; color: #151515;font-size: 14px;font-weight: bold;">
                        <span>{{ user.name }}</span>
        &lt;!&ndash;                <i class="el-icon-arrow-down" style="margin-left: 5px"></i>&ndash;&gt;
                      </div>
                    </div>
                    <el-dropdown-menu slot="dropdown">
                      <el-dropdown-item>
                        <div @click="$router.push('/person')">个人中心</div>
                      </el-dropdown-item>
                      <el-dropdown-item>
                        <div @click="logout">退出</div>
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </el-dropdown>
                </div>-->
      </div>
    </div>
    <!--主体-->
    <div class="main-body">
      <div class="main-left">
        <div class="main-left-upper">
          <div class="user-info" @click="goToUserProfile">
            <el-avatar class="avatar" :src="$baseUrl+'/avatar/'+user.id" shape="circle"
                       fit="contain" size="large" alt=""></el-avatar>
            <div class="user-name">
              <span>{{ user.name }}</span>
            </div>
          </div>
          <el-progress
              :text-inside="false" :show-text=false :stroke-width="8" :percentage="percentage" :color="progressColor"
              class="progress"></el-progress>
          <div class="user-space-info">
            <span class="user-space"> {{
                usedSpace| sizeFormat
              }} / {{ totalSpace | sizeFormat }}</span>
          </div>
        </div>
        <el-menu text-color="#565757" active-text-color="#0d53ff" router class="el-menu" :default-active="$route.path">
          <el-menu-item class="el-menu-item" index="/files">
            <i class="el-icon-folder-opened"></i>
            <span class="words" slot="title">全部文件</span>
          </el-menu-item>
          <el-menu-item class="el-menu-item" index="/upload">
            <i class="el-icon-upload"></i>
            <span class="words" slot="title">上传文件</span>
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
      </div>
      <div class="main-right">
        <router-view @update:user="updateUser"/>
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
      notice: [],
      user: JSON.parse(localStorage.getItem("user") || '{}'),

      totalSpace: 1024 * 1024 * 1024, // 1GB 总空间
      usedSpace: 0, // 从后端接口获取的已用空间（以字节为单位）
    }
  },

  mounted() {
    // this.loadNotice()
    this.getUsedSpace()
  },
  updated() {
    this.getUsedSpace()
  },
  computed: {
    // 计算已用空间占比
    percentage() {
      return (this.usedSpace / this.totalSpace) * 100;
    },
    // 根据占比确定进度条颜色
    progressColor() {
      if (this.percentage >= 90) {
        return '#f22121'; // 大于等于 90% 显示红色
      } else if (this.percentage >= 75) {
        return '#f15f0c'; // 大于等于 75% 显示橙色
      } else if (this.percentage >= 50) {
        return '#f3f125'; // 大于等于 50% 显示黄色
      } else {
        return '#4deb10'; // 小于 50% 显示绿色
      }
    },
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
    /*    loadNotice() {
          this.$request.get('/notice/selectAll').then(res => {
            this.notice = res.data
            let i = 0
            if (this.notice && this.notice.length) {
              this.top = this.notice[0].content
              setInterval(() => {
                this.top = this.notice[i].content
                i++
                if (i === this.notice.length) {
                  i = 0
                }
              }, 2500)
            }
          })
        },*/
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
}

.front-layout {
  background: #f5f6f7;
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
  background: #f5f6f7;
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


</style>