import axios from 'axios'
import router from "@/router";

// 创建可一个新的axios对象
const request = axios.create({
    baseURL: process.env.VUE_APP_BASEURL,   // 后端的接口地址  ip:port
    timeout: 30000                          // 30s请求超时
})

// request 拦截器
// 可以自请求发送前对请求做一些处理
// 比如统一加token，对请求参数统一加密
request.interceptors.request.use(config => {
    config.headers['Content-Type'] = 'application/json;charset=utf-8';        // 设置请求头格式
    let user = JSON.parse(localStorage.getItem("user") || '{}')  // 获取缓存的用户信息
    config.headers['token'] = user.token  // 设置请求头
    return config
}, error => {
    console.error('请求错误: ' + error) // for debug
    return Promise.reject(error)
});

// response 拦截器
// 可以在接口响应后统一处理结果
request.interceptors.response.use(
    response => {
        // console.log("content-type: "+response.headers['content-type']);
        // 判断是否是文件下载请求，如果是，则直接返回配置对象
        if (response.headers['content-type'] === 'application/octet-stream'|| isFileDownloadRequest(response.config.url)) {
            return response;
        }
        let res = response.data;
        // 兼容服务端返回的字符串数据
        if (typeof res === 'string') {
            res = res ? JSON.parse(res) : res
        }
        if (res.code === '401' || res.code === '402' || res.code === '403' || res.code === '406'
            || res.code === '407' || res.code === '502'|| res.code === '504') {
            router.push('/login')
        }
        return res;
    },
    error => {
        console.error('响应错误: ' + error) // for debug
        return Promise.reject(error)
    }
)

// 判断是否是文件下载请求的辅助函数
function isFileDownloadRequest(url) {
    // 根据 URL 的特定条件判断是否是文件下载请求，您需要根据实际情况修改这里的判断逻辑
    // 比如判断 URL 是否以特定的文件扩展名结尾，或者是否包含特定的文件路径等
    return url.endsWith('.pdf') || url.includes('/download/');
}

export default request