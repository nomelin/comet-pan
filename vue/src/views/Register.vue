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
          <el-form :model="form" :rules="rules" ref="formRef">
            <el-form-item prop="username">
              <el-input class="input-field" size="medium" prefix-icon="el-icon-s-custom" placeholder="请输入账号"
                        v-model="form.username"></el-input>
            </el-form-item>
            <el-form-item prop="password">
              <el-input class="input-field" size="medium" prefix-icon="el-icon-lock" placeholder="请输入密码"
                        show-password v-model="form.password"></el-input>
            </el-form-item>
            <el-form-item prop="confirmPass">
              <el-input class="input-field" size="medium" prefix-icon="el-icon-lock" placeholder="请确认密码"
                        show-password v-model="form.confirmPass"></el-input>
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
      rules: {
        username: [
          {required: true, message: '请输入账号', trigger: 'blur'},
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
          this.$request.post('/register', this.form).then(res => {
            if (res.code === '200') {
              this.$router.push('/login')  // 跳转登录页面
              this.$message.success('注册成功，请登录')
            } else {
              this.$message.error(res.code + ": " + res.msg)
            }
          })
        }
      })
    }
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

.register-box {
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

.register-form {
  text-align: center;
  font-size: 30px;
  font-weight: bold;
  margin-bottom: 30px;
  color: #242830;
}

.input-field {
  /*height: 60px;*/
}

.register-button {
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

.login-link {
  display: flex;
  justify-content: flex-end;
  /*align-items: center;*/
  /*text-align: right;*/
}

.login-link span {
  color: #909399;
  font-size: 15px;

}

.login-link a {
  font-size: 15px;
  color: #2a60c9;
  font-weight: bold;
}

</style>