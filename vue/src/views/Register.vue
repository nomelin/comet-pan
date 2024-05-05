<template>
  <div class="container">
    <div class="header">
      <img src="@/assets/imgs/logo.svg" alt="" class="logo">
    </div>
    <div class="main-content">
      <div class="register-box">
        <div class="register-form">
          <div class="title">欢 迎 注 册</div>
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
              <div class="custom-input">
                <el-input class="input-field" size="medium" prefix-icon="el-icon-s-custom" placeholder="请输入用户名"
                          v-model="form.username" @blur="checkUsername" maxlength="30"></el-input>
              </div>
            </el-form-item>
            <el-form-item prop="password">
              <div class="custom-input">
                <el-input class="input-field" size="medium" prefix-icon="el-icon-lock" placeholder="请输入密码"
                          show-password v-model="form.password" maxlength="30"></el-input>
              </div>
            </el-form-item>
            <el-form-item prop="confirmPass">
              <div class="custom-input">
                <el-input class="input-field" size="medium" prefix-icon="el-icon-lock" placeholder="请确认密码"
                          show-password v-model="form.confirmPass" maxlength="30"></el-input>
              </div>
            </el-form-item>
            <el-form-item>
              <el-button class="register-button" type="primary" @click="register">
                <span class="button-text">注 册</span>
              </el-button>
            </el-form-item>
            <div class="login-link">
              <span>已有账号？请 </span><a href="/login">登录</a>

            </div>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import CryptoJS from 'crypto-js'

export default {
  name: "Register",
  data() {
    const validatePassword = (rule, confirmPass, callback) => {
      if (confirmPass === '') {
        callback(new Error('请确认密码'))
      } else if (confirmPass !== this.form.password) {
        callback(new Error('两次输入的密码不一致'))
      } else {
        callback()
      }
    }
    return {
      form: {},
      imgs: [
        require('@/assets/imgs/slide1.jpg'),
        require('@/assets/imgs/slide2.jpg'),
        require('@/assets/imgs/slide3.jpg'),
        require('@/assets/imgs/slide4.jpg'),
        require('@/assets/imgs/slide5.jpg'),
        require('@/assets/imgs/slide6.jpg'),
        require('@/assets/imgs/slide7.jpg'),
        require('@/assets/imgs/slide8.jpg'),
        require('@/assets/imgs/slide9.jpg'),
        require('@/assets/imgs/slide10.jpg'),

      ],
      slideVerifyShow: false,
      rules: {
        username: [
          {required: true, message: '请输入用户名', trigger: 'blur'},
        ],
        password: [
          {required: true, message: '请输入密码', trigger: 'blur'},
        ],
        confirmPass: [
          {validator: validatePassword, trigger: 'blur'}
        ]
      }
    }
  },
  created() {

  },
  methods: {
    register() {
      this.$refs['formRef'].validate((valid) => {
        if (valid) {
          // 验证通过
          this.slideVerifyShow = true;
        }
      })
    },
    checkUsername() {
      // 发送请求检查用户名
      this.$request.get(`/valid/${this.form.username}`).then(res => {
        if (res.code === '200') {
          let isValid = res.data;
          if (!isValid) {
            this.$message.error("用户名已被注册,请更换")// 用户名已被注册，请用户更换
          }
        } else {
          this.$message.error(res.code + ": " + res.msg)
        }
      });
    },
    onSuccess() {
      // 对密码进行哈希和加盐处理
      let saltedPassword = this.form.username + this.form.password;
      // 使用哈希过的密码
      let hashedPassword = CryptoJS.SHA256(saltedPassword).toString();
      console.log("hashedPassword: "+hashedPassword)
      this.$request.post('/register', {
        username: this.form.username,
        password: hashedPassword
      }).then(res => {
        if (res.code === '200') {
          this.$router.push('/login')  // 跳转登录页面
          this.$message.success('注册成功，请登录')
        } else {
          this.slideVerifyShow = false;
          this.$message.error(res.code + ": " + res.msg)
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
  height: 10%;
  position: fixed;
  top: 0;
  display: flex;
  align-items: center;
  padding-left: 2%;
}

.logo {
  width: 60%;
  margin-top: 5%; /* 上边距 */
  margin-left: 5%; /* 左边距 */
}

.title {
  color: #151515;
  font-size: 1.8rem;
  font-weight: bold;
  margin-left: 1rem;
}

.main-content {
  flex: 1;
  display: flex;
  justify-content: center;
}

.blank {
  height:1rem;
}

.custom-input {
  position: relative;
  padding: 0.8rem; /* 输入框内边距 */
  border: 2px solid #dcdfe6; /* 自定义输入框的边框 */
  border-radius: 1.2rem; /* 自定义输入框的边框圆角 */
  height: 4rem; /* 自定义输入框的高度 */
  min-height: 50px;

}

.register-box {
  width: 50vh;
  max-width: 500px;
  padding: 3rem 1.5rem;
  box-shadow: 0 1.5rem 6rem #e6e2ff;
  background-color: white;
  border-radius: 4rem;
  height: 60vh;
  max-height: 800px;
  overflow: auto;
  min-height: 400px;

}

.register-form {
  text-align: center;
  font-size: 1.8rem;
  font-weight: bold;
  margin-bottom: 2rem;
  color: #242830;
}

::v-deep .input-field .el-input__inner {
  width: 100%;
  text-align: left;
  border: 0 !important;
  outline: none;
  font-weight: bold;
  font-size: 1rem;
}

.register-button {
  width: 100%;
  background-color: #0d53ff;
  height: 4rem;
  border-radius:1.2rem;
  color: white;
}

.button-text {
  font-size:1.2rem;
  font-weight: bold;
}

.login-link {
  display: flex;
  justify-content: flex-end;
  /*align-items: center;*/
  /*text-align: right;*/
}

.login-link span {
  color: #909399;
  font-size: 0.95rem;

}

.login-link a {
  font-size: 0.95rem;
  color: #2a60c9;
  font-weight: bold;
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

</style>