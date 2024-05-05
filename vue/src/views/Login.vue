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
              <div class="custom-input">
                <el-input class="input-field" size="medium" prefix-icon="el-icon-s-custom" placeholder="用户名"
                          maxlength="30"
                          v-model="form.username"></el-input>
              </div>
            </el-form-item>
            <el-form-item prop="password">
              <div class="custom-input">
                <el-input class="input-field" size="medium" prefix-icon="el-icon-lock" placeholder="密码" show-password
                          maxlength="30"
                          v-model="form.password"></el-input>
              </div>
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
          <div class="temp">
            测试账号用户名: 123, 密码: 123
          </div>
        </div>

      </div>
    </div>
  </div>
</template>

<script>
import CryptoJS from 'crypto-js'
import {setItemWithExpiry} from "@/App"
import {getItemWithExpiry} from "@/App"
import {updateItemWithExpiry} from "@/App"

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
      // 对密码进行哈希和加盐处理
      let saltedPassword = this.form.username + this.form.password;
      // 使用哈希过的密码
      let hashedPassword = CryptoJS.SHA256(saltedPassword).toString();
      console.log("hashedPassword: " + hashedPassword)
      this.$request.post('/login', {
        username: this.form.username,
        password: hashedPassword
      }).then(res => {
        if (res.code === '200') {
          console.log("data: " + JSON.stringify(res.data))
          // localStorage.setItem("user", JSON.stringify(res.data));
          setItemWithExpiry('user', res.data, 1000 * 60 * 60 * 2); // 设置 token 过期时间为2小时
          this.$message.success('登录成功');
          setTimeout(() => {
            //以下是为了实现跳转回登录前的页面,比如分享页面
            // 获取 URL 参数中的当前页面路径
            const urlParams = new URLSearchParams(window.location.search);
            const redirect = urlParams.get('redirect');
            // 在用户登录成功后，跳转回之前的页面
            if (redirect) {
              window.location.href = decodeURIComponent(redirect);
            }

            if (res.data.role === 1) {
              this.$router.push('/manager/home');
            } else if (res.data.role === 2) {
              this.$router.push('/files');
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
.temp {
  font-size: 1.2rem;
  margin-top: 1.5rem;
  padding-top: 0.5rem;
  padding-bottom: 0.5rem;
  background-color: #f3f6fe;
  color: #707375;
  border-radius: 0.5rem;
}

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
  height: 1rem;
}

.login-box {
  width: 50vh;
  max-width: 500px;
  padding: 3rem 1.5rem;
  box-shadow: 0 1.5rem 6rem #e6e2ff;
  background-color: white;
  border-radius: 4rem;
  height: 60vh;
  max-height: 800px;
  min-height: 300px;
  overflow: auto;
}

.login-form {
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

.custom-input {
  position: relative;
  padding: 0.8rem; /* 输入框内边距 */
  border: 2px solid #dcdfe6; /* 自定义输入框的边框 */
  border-radius: 1.2rem; /* 自定义输入框的边框圆角 */
  height: 4rem; /* 自定义输入框的高度 */
  min-height: 50px;
}


.login-button {
  width: 100%;
  background-color: #0d53ff;
  height: 4rem;
  border-radius: 1.2rem;
  color: white;
}

.button-text {
  font-size: 1.2rem;
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
  font-size: 0.95rem;

}

.register-link a {
  font-size: 0.95rem;
  color: #2a60c9;
  font-weight: bold;
}
</style>
