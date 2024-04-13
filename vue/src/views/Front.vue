<template>
  <div class="front-layout">
    <div class="front-notice"><i class="el-icon-bell" style="margin-right: 2px"></i>公告：{{ top }}</div>
    <!--头部-->
    <div class="front-header">
      <div class="front-header-left">
        <a href="/front/home">
          <img class="logo" src="@/assets/imgs/logo.svg" alt="logo" >
        </a>
      </div>
      <div class="front-header-center">

      </div>
      <div class="front-header-right">
        <div v-if="!user.userName">
          <el-button @click="$router.push('/login')">登录</el-button>
          <el-button @click="$router.push('/register')">注册</el-button>
        </div>
        <div v-else>
          <el-dropdown>
            <div class="front-header-dropdown">
              <img :src="user.avatar" alt="">
              <div style="margin-left: 10px; color: #66ccff">
                <span>{{ user.name }}</span><i class="el-icon-arrow-down" style="margin-left: 5px"></i>
              </div>
            </div>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item>
                <div @click="$router.push('/front/person')">个人中心</div>
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
      <router-view ref="child" @update:user="updateUser" />
    </div>
  </div>

</template>

<script>

export default {
  name: "FrontLayout",

  data () {
    return {
      top: '',
      notice: [],
      user: JSON.parse(localStorage.getItem("user") || '{}'),
    }
  },

  mounted() {
    this.loadNotice()
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
@import "@/assets/css/front.css";
.front-layout {
  bakground-color: #66ccff;
}
</style>