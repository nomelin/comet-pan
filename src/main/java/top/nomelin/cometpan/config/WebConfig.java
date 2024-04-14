package top.nomelin.cometpan.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import top.nomelin.cometpan.interceptor.AdminInterceptor;
import top.nomelin.cometpan.interceptor.TokenInterceptor;
import top.nomelin.cometpan.interceptor.UserInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {


    private final TokenInterceptor tokenInterceptor;
    private final AdminInterceptor adminInterceptor;
    private final UserInterceptor userInterceptor;

    public WebConfig(TokenInterceptor tokenInterceptor, AdminInterceptor adminInterceptor, UserInterceptor userInterceptor) {
        this.tokenInterceptor = tokenInterceptor;
        this.adminInterceptor = adminInterceptor;
        this.userInterceptor = userInterceptor;
    }

    // 加自定义拦截器,设置拦截规则
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tokenInterceptor).addPathPatterns("/**")
                .excludePathPatterns("/")
                .excludePathPatterns("/login")
                .excludePathPatterns("/register")
                .excludePathPatterns("/avatar/**");
        registry.addInterceptor(adminInterceptor).addPathPatterns("/users/**");// 管理后台，除了更新用户的接口
        registry.addInterceptor(userInterceptor).addPathPatterns("/files/**");// 用户文件空间
    }
}
