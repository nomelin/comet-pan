package top.nomelin.cometpan.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import top.nomelin.cometpan.interceptor.AdminInterceptor;
import top.nomelin.cometpan.interceptor.DebugInterceptor;
import top.nomelin.cometpan.interceptor.TokenInterceptor;
import top.nomelin.cometpan.interceptor.UserInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {


    private final TokenInterceptor tokenInterceptor;
    private final AdminInterceptor adminInterceptor;
    private final UserInterceptor userInterceptor;
    private final DebugInterceptor debugInterceptor;

    public WebConfig(TokenInterceptor tokenInterceptor, AdminInterceptor adminInterceptor, UserInterceptor userInterceptor, DebugInterceptor debugInterceptor) {
        this.tokenInterceptor = tokenInterceptor;
        this.adminInterceptor = adminInterceptor;
        this.userInterceptor = userInterceptor;
        this.debugInterceptor = debugInterceptor;
    }

    // 加自定义拦截器,设置拦截规则
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(debugInterceptor).addPathPatterns("/**");
        registry.addInterceptor(tokenInterceptor).addPathPatterns("/**")
                .excludePathPatterns("/")
                .excludePathPatterns("/login")
                .excludePathPatterns("/register")
                .excludePathPatterns("/avatar/**")
                .excludePathPatterns("/users/{id}")
                .excludePathPatterns("/valid/*")
                .excludePathPatterns("/share/*")
                .excludePathPatterns("/download/**")// 下载文件,到时候请关闭
                .excludePathPatterns("/files/share/batch");
//                .excludePathPatterns("/upload/**");
        registry.addInterceptor(adminInterceptor).addPathPatterns("/users/**")
                .excludePathPatterns("/users/{id}")
        ;// 管理后台

        registry.addInterceptor(userInterceptor).addPathPatterns("/files/**")// 用户文件空间
                .excludePathPatterns("/files/share/batch");
    }
}
