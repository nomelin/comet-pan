<template>
  <div class="main-content">
    <el-card class="card">
      <div class="password">
        <el-button class="primary-button" style="margin-right: 10rem" type="primary" @click="updatePassword">修改密码
        </el-button>
      </div>
      <el-form class="form" :model="user" label-width="80px">
        <div style="margin: 15px; text-align: center">
          <el-upload
              ref="upload"
              class="avatar-uploader"
              :action="$baseUrl + '/avatar/'+user.id"
              :headers="{ token: user.token }"
              :show-file-list="false"
              :on-success="handleAvatarSuccess">
            <img :src="$baseUrl + '/avatar/'+user.id" class="avatar" alt="头像"/>
          </el-upload>
        </div>
        <el-form-item label="用户名" prop="username">
          <el-input class="input" v-model="user.userName" placeholder="用户名" disabled></el-input>
        </el-form-item>
        <el-form-item label="昵称" prop="name">
          <el-input class="input" v-model="user.name" maxlength="50" placeholder="昵称"></el-input>
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input class="input" v-model="user.phone" placeholder="电话"></el-input>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input class="input" v-model="user.email" placeholder="邮箱"></el-input>
        </el-form-item>
        <div class="btn-group">
          <el-button class="primary-button" type="primary" @click="update">保 存</el-button>
        </div>
      </el-form>
    </el-card>
    <el-dialog title="修改密码" :visible.sync="dialogVisible" width="30%" :close-on-click-modal="false"
               destroy-on-close class="dialog">
      <el-form :model="user" label-width="80px" style="padding-right: 20px" :rules="rules" ref="formRef">
        <el-form-item label="原始密码" prop="password">
          <el-input class="input" show-password v-model="user.oldPassword" placeholder="原始密码"></el-input>
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input class="input" show-password v-model="user.newPassword" placeholder="新密码"></el-input>
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input class="input" show-password v-model="user.confirmPassword" placeholder="确认密码"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="save">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import CryptoJS from 'crypto-js'

import {setItemWithExpiry} from "@/App"
import {getItemWithExpiry} from "@/App"
import {updateItemWithExpiry} from "@/App"

export default {
  data() {
    const validatePassword = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请确认密码'))
      } else if (value !== this.user.newPassword) {
        callback(new Error('确认密码错误'))
      } else {
        callback()
      }
    }
    return {
      user: getItemWithExpiry("user"),
      dialogVisible: false,

      rules: {
        oldPassword: [
          {required: true, message: '请输入原始密码', trigger: 'blur'},
        ],
        newPassword: [
          {required: true, message: '请输入新密码', trigger: 'blur'},
        ],
        confirmPassword: [
          {validator: validatePassword, required: true, trigger: 'blur'},
        ],
      }
    }
  },
  created() {

  },
  methods: {
    update() {
      // 保存当前的用户信息到数据库
      this.$request.put('/users', this.user).then(res => {
        if (res.code === '200') {
          // 成功更新
          this.$message.success('个人信息保存成功')
          // 更新浏览器缓存里的用户信息
          updateItemWithExpiry("user",this.user);

          // 触发父级的数据更新
          this.$emit('update:user')
        } else {
          this.$message.error(res.code + ": " + res.msg)
        }
      })
    },
    handleAvatarSuccess(response, file, fileList) {
      this.$message.success('头像上传成功')
      location.reload();
      return;
      if (response.code === '200') {
        this.$message.success('头像上传成功')
      } else {
        this.$message.error(response.code + ": " + response.msg)
      }
      setTimeout(() => {
        location.reload();
      }, 500);

    },
    // 修改密码
    updatePassword() {
      this.dialogVisible = true
    },
    save() {
      this.$refs.formRef.validate((valid) => {
        if (valid) {
          // 对密码进行哈希和加盐处理
          let saltedOldPassword = this.user.userName + this.user.oldPassword;
          let saltedNewPassword = this.user.userName + this.user.newPassword;
          // 使用哈希过的密码
          let oldPassword = CryptoJS.SHA256(saltedOldPassword).toString();
          let newPassword = CryptoJS.SHA256(saltedNewPassword).toString();
          this.$request.put('/updatePassword', {
            userName: this.user.userName,
            oldPassword: oldPassword,
            newPassword: newPassword
          }).then(res => {
            if (res.code === '200') {
              // 成功更新
              this.$message.success('修改密码成功')
              this.$request.delete("/logout").then(res => {
                if (res.code === '200') {
                  this.$message.success("请重新登录")
                  localStorage.removeItem("user");
                  this.$router.push("/login");
                } else {
                  this.$message.error(res.code + ": " + res.msg)
                }
              }).catch(error => {
                this.$message.error("退出失败")
              });
            } else {
              this.$message.error(res.msg)
            }
          })
        }
      })
    }
  }
}
</script>

<style scoped>
::v-deep .el-form-item__label {
  font-weight: bold;
}

/*/deep/ .el-upload {*/
/*  border-radius: 50%;*/
/*}*/


::v-deep .avatar-uploader .el-upload {
  border: 1px dashed #d9d9d9;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  border-radius: 30%;
}

::v-deep .avatar-uploader .el-upload:hover {
  border-color: #0d53ff;
}

/*.avatar-uploader-icon {*/
/*  font-size: 28px;*/
/*  color: #8c939d;*/
/*  width: 50%;*/
/*  height:50%;*/
/*  line-height: 120px;*/
/*  text-align: center;*/
/*  border-radius: 50%;*/
/*}*/

.avatar {
  width: 9rem;
  height: 9rem;
  display: block;
  border-radius: 20%;
}

.main-content {
  width: 95%;
  height: 95%;

}

.card {
  width: 80%;
  height: auto;
  padding-bottom: 3rem;
  box-shadow: 0 2rem 7rem rgba(217, 236, 255, 0.5);
  margin: 0 auto;
  border-radius: 5rem;
}

.password {
  text-align: right;
  width: 100%;
  height: 5vh;

}

.form {
  width: 70%;
  height: 100%;
  margin: 0 auto;
}

::v-deep .input .el-input__inner {
  width: 100%;
  text-align: left;
  border: 1px solid #d9d9d9;
  outline: none;
  font-weight: bold;
  font-size: 1rem;
  height: 3rem;
  border-radius: 0.5rem;
}

.btn-group {
  margin-top: 1rem;
  text-align: center;
}

.dialog-footer {
  text-align: center;
}

.dialog {
  border-radius: 0.5rem;
}

</style>