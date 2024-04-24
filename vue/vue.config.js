const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    port: 443
  },
  chainWebpack: config =>{
    config.plugin('html')
        .tap(args => {
          args[0].title = "彗星网盘";
          return args;
        })
  }
})
