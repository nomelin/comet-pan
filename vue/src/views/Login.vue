<template>
  <div class="container">
    <div class="header">
      <img src="@/assets/imgs/logo.svg" alt="" class="logo">
    </div>

    <div class="main-content">
      <div class="login-box">
        <div class="login-form">
          <div class="title">欢 迎 使 用</div>
          <div class="blank"></div>
          <div class="slide-verify-window" v-if="slideVerifyShow">
            <slide-verify :l="60"
                          :r="10"
                          :w="500"
                          :h="300"
                          :accuracy="30"
                          :imgs="imgs"
                          slider-text=""
                          @success="onSuccess"
                          ref="slideVerifyRef"
            ></slide-verify>
          </div>
          <!-- 滑块遮罩层 -->
          <div class="mask" @click="closeSlideVerify" v-if="slideVerifyShow"></div>
          <el-form :model="form" :rules="rules" ref="formRef">
            <el-form-item prop="username">
              <el-input class="input-field" size="medium" prefix-icon="el-icon-s-custom" placeholder="用户名"
                        maxlength="30"
                        v-model="form.username"></el-input>
            </el-form-item>
            <el-form-item prop="password">
              <el-input class="input-field" size="medium" prefix-icon="el-icon-lock" placeholder="密码" show-password
                        maxlength="30"
                        v-model="form.password"></el-input>
            </el-form-item>
            <el-form-item>
              <el-button size="medium" class="login-button" @click="login">
                <span class="button-text">登 录</span>
              </el-button>
            </el-form-item>
            <div class="register-link">
              <span>还没有账号？请<a href="/register">注册</a></span>
            </div>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "Login",
  data() {
    return {
      form: {},
      imgs: [
        require('@/assets/imgs/slide1.jpg'),
        require('@/assets/imgs/slide2.jpg'),
        require('@/assets/imgs/slide3.jpg'),
        require('@/assets/imgs/slide4.jpg'),
        require('@/assets/imgs/slide5.jpg'),

      ],
      slideVerifyShow: false,
      rules: {
        username: [
          {required: true, message: '请输入用户名', trigger: 'blur'},
        ],
        password: [
          {required: true, message: '请输入密码', trigger: 'blur'},
        ]
      }
    }
  },
  methods: {
    login() {
      this.$refs['formRef'].validate((valid) => {
        if (valid) {
          this.slideVerifyShow = true;
          // 验证通过

        }
      })
    },
    onSuccess() {
      this.$request.post('/login', this.form).then(res => {
        if (res.code === '200') {
          localStorage.setItem("user", JSON.stringify(res.data));
          this.$message.success('登录成功');
          setTimeout(() => {
            if (res.data.role === 1) {
              this.$router.push('/home');
            } else if (res.data.role === 2) {
              this.$router.push('/front/home');
            }
          }, 100);
        } else {
          this.$message.error(res.code + ": " + res.msg);
          this.slideVerifyShow = false;
        }
      })
    },
    closeSlideVerify() {
      this.slideVerifyShow = false;
    },
  }
}
</script>

<style scoped>
.container {
  height: 100vh;
  overflow: hidden;
  display: flex;
  align-items: center;
  background-color: #f3f6fe;
}

.header {
  height: 80px;
  position: fixed;
  top: 0;
  display: flex;
  align-items: center;
  padding-left: 20px;
}
.slide-verify-window {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 999;
}
.mask {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.6); /* 半透明黑色背景 */
  z-index: 998; /* 确保遮罩层位于滑块窗口下面 */
  backdrop-filter: blur(3px); /* 高斯模糊效果，可以根据需要调整模糊程度 */
}
.logo {
  width: 160px;
  margin-top: 20px; /* 上边距 */
  margin-left: 20px; /* 左边距 */
}

.title {
  color: #151515;
  font-size: 24px;
  font-weight: bold;
  margin-left: 10px;
}

.main-content {
  flex: 1;
  display: flex;
  justify-content: center;
}

.blank {
  height: 40px;
}

.login-box {
  width: 50vh;
  max-width: 500px;
  padding: 60px 20px;
  box-shadow: 0 20px 80px #e6e2ff;
  background-color: white;
  border-radius: 50px;
  height: 60vh;
  max-height: 800px;
  overflow: auto;
}

.login-form {
  text-align: center;
  font-size: 30px;
  font-weight: bold;
  margin-bottom: 30px;
  color: #242830;
}

.input-field {
  /*height: 60px;*/
}

.login-button {
  width: 100%;
  background-color: #0d53ff;
  height: 60px;
  border-radius: 20px;
  color: white;
}

.button-text {
  font-size: 22px;
  font-weight: bold;
}

.register-link {
  display: flex;
  justify-content: flex-end;
  /*align-items: center;*/
  /*text-align: right;*/
}

.register-link span {
  color: #909399;
  font-size: 15px;

}

.register-link a {
  font-size: 15px;
  color: #2a60c9;
  font-weight: bold; /* 设置字体粗细 */
}
</style>
