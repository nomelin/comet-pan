<template>
  <div class="front-layout">
<!--    <div class="front-notice"><i class="el-icon-bell" style="margin-right: 2px"></i>公告：{{ top }}</div>-->
    <!--头部-->
    <div class="front-header">
      <div class="front-header-left">
        <a href="/front/home">
          <img class="logo" src="@/assets/imgs/logo.svg" alt="logo">
        </a>
      </div>
      <div class="front-header-center">
        <el-breadcrumb separator-class="el-icon-arrow-right">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item :to="{ path: $route.path }">{{ $route.meta.name }}</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="front-header-right">
        <div v-if="!user.userName">
          <el-button @click="$router.push('/login')">登录</el-button>
          <el-button @click="$router.push('/register')">注册</el-button>
        </div>
        <div v-else>
          <el-dropdown>
            <div class="front-header-dropdown">
              <img :src="$baseUrl+'/avatar/'+user.id" alt="">
              <div style="margin-left: 10px; color: #151515;font-size: 14px;font-weight: bold;">
                <span>{{ user.name }}</span><i class="el-icon-arrow-down" style="margin-left: 5px"></i>
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
        </div>
      </div>
    </div>
    <!--主体-->
    <div class="main-body">
      <div class="main-left">
        <el-menu :default-openeds="['info', 'user']" router style="border: none" :default-active="$route.path">
          <el-menu-item index="/files">
            <i class="el-icon-s-home"></i>
            <span slot="title">全部文件</span>
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
    }
  },

  mounted() {
    // this.loadNotice()
  },
  methods: {
    loadNotice() {
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
    },
    updateUser() {
      this.user = JSON.parse(localStorage.getItem('user') || '{}')   // 重新获取下用户的最新信息
    },
    // 退出登录
    logout() {
      localStorage.removeItem("user");
      this.$router.push("/login");
    },
  }

}
</script>

<style scoped>
.front-layout{
  /*display: flex;*/
  height: 100vh;
}
.main-body {
  display: flex;
  /*height: 100%;*/
  height: 85vh;
  /*flex-grow: 1;*/
}

.main-left {
  width: 15vw;
}

.main-right {
  width: 100%;
}
.front-layout{
  background: #f5f6f7;
}
.front-notice {
  height: 5vh;
  padding: 5px 20px;
  color: #666;
  font-size: 12px
}
.logo {
  width: 150px;
  margin-top: 20px;
}
.front-header {
  display: flex;
  height: 10vh;
  line-height: 60px;
  border-bottom: 1px solid #eee;
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
.el-menu.el-menu--horizontal {
  border: none !important;
  height: 80px;
}
</style>