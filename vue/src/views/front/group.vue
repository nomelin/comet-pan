<template>
  <div class="main-container">
    <h1 style="text-align: center;
      color: #409eff;">正在开发中...</h1>
    <hr/>

  </div>
</template>

<script>
  import axios from "axios";

  export default {
    name: 'Group',
    data() {
      return {
        downloadProgress: 0,
      }
    },
    methods: {

    },


  }

</script>

<style scoped>
.main-container {
  /*border-radius: 50px;*/
  border-top-left-radius: 50px;
  background-color: #ffffff;
  height: 100%;
  width: 100%;
}
</style>
