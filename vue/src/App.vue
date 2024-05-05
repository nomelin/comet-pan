<template>
  <div id="app">
    <router-view/>
  </div>
</template>

<script>
import Vue from "vue";

export default {
  name: 'App'
}
//全局注册自定义size数字过滤器，按照B, KB，M，G进行格式化
Vue.filter('sizeFormat', function (value, decimals = 2) {
  if (typeof value !== 'number') {
    return value;
  }
  const units = ['B', 'KB', 'M', 'G'];
  let unitIndex = 0;
  while (value >= 1024 && unitIndex < units.length - 1) {
    value /= 1024;
    unitIndex++;
  }
  //这个正则表达式是按3位插入分隔符
  return value.toFixed(decimals).replace(/\B(?=(\d{3})+(?!\d))/g, ',') + ' ' + units[unitIndex];
});
//全局注册自定义时间格式化过滤器，将ms时间戳转换为年-月-日 时:分的格式
Vue.filter('formatTime', function (timestamp) {
  let date = new Date(parseInt(timestamp));
  let year = date.getFullYear();
  let month = (date.getMonth() + 1).toString().padStart(2, '0');
  let day = date.getDate().toString().padStart(2, '0');
  let hour = date.getHours().toString().padStart(2, '0');
  let minute = date.getMinutes().toString().padStart(2, '0');
  return `${year}-${month}-${day} ${hour}:${minute}`;
});


// 动态计算根元素的字体大小
function setRootFontSize() {
  const baseFontSize = 16; // 默认字体大小
  const screenWidth = window.innerWidth; // 获取屏幕宽度
  // console.log(screenWidth);
  let fontSize = screenWidth / 1920 * baseFontSize * 1.1; // 根据屏幕宽度计算字体大小
  if (fontSize < 10) {
    fontSize = 10; // 最小字体大小为10px
  }
  document.documentElement.style.setProperty('--base-font-size', `${fontSize}px`); // 设置根元素的字体大小
}

// 初始化时设置根元素的字体大小
setRootFontSize();

// 在窗口大小变化时重新设置根元素的字体大小
window.addEventListener('resize', setRootFontSize);


// export function downloadFile(onDownloadProgress, fileName, diskId) {
//   return axios({
//     baseURL: process.env.VUE_APP_BASEURL,
//     url: `/download/test/${diskId}`,
//     method: 'get',
//     responseType: 'blob',
//     onDownloadProgress
//   }).then(response => {
//     const url = window.URL.createObjectURL(new Blob([response.data]))
//     const link = document.createElement('a')
//     link.href = url
//     link.setAttribute('download', fileName) // 这里的 'file' 应该是你的文件名
//     document.body.appendChild(link)
//     link.click()
//     link.remove()
//   })
// }
export function downloadFile( diskId, fileId) {
  window.location.href = `${process.env.VUE_APP_BASEURL}/download?diskId=${diskId}&fileId=${fileId}`
}

// 存储数据和时间戳
//ttl是过期时间，单位是毫秒
export function setItemWithExpiry(key, value, ttl) {
  const now = new Date()
  // `item`是我们要在localStorage中存储的对象
  const item = {
    value: value,
    expiry: now.getTime() + ttl,
  }
  localStorage.setItem(key, JSON.stringify(item))
}

// 获取数据并检查是否过期
export function getItemWithExpiry(key) {
  const itemStr = localStorage.getItem(key)
  // 如果数据不存在，返回空对象
  if (!itemStr) {
    return {}
  }
  const item = JSON.parse(itemStr)
  const now = new Date()
  // 比较当前时间和存储的时间戳
  if (now.getTime() > item.expiry) {
    // 如果过期了，删除数据并返回null
    localStorage.removeItem(key)
    return {}
  }
  return item.value
}

// 更新数据但不更改过期时间
export function updateItemWithExpiry(key, newValue) {
  const itemStr = localStorage.getItem(key)
  // 如果数据不存在，返回空对象
  if (!itemStr) {
    return {}
  }
  const item = JSON.parse(itemStr)
  // 更新数据的值
  item.value = newValue
  // 将更新后的数据重新存储
  localStorage.setItem(key, JSON.stringify(item))
}

</script>
<style>
:root {
  /* 默认字体大小 */
  --base-font-size: 16px;
}

html {
  /* 使用 CSS 变量设置根元素字体大小 */
  font-size: var(--base-font-size);
}


/*以下的样式是为了解决弹出框导致页面元素发生偏移的问题。*/
body {

  /*这段代码是为了防止页面出现水平滚动条*/
  padding-right: 0 !important;
}

.modal-open {
  overflow-y: scroll;
  padding-right: 0 !important
}


</style>

