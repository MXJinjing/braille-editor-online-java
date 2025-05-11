package wang.jinjing.editor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync // 启用异步支持
public class AsyncConfig {

    @Bean(name = "customTaskExecutor") // 自定义线程池名称
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数（默认线程数）
        executor.setCorePoolSize(5);
        // 最大线程数（队列满后扩容的最大值）
        executor.setMaxPoolSize(10);
        // 队列容量（存放等待任务的队列大小）
        executor.setQueueCapacity(100);
        // 线程名前缀（方便日志跟踪）
        executor.setThreadNamePrefix("Async-Thread-");
        // 拒绝策略（队列满后的处理方式）
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 初始化
        executor.initialize();
        return executor;
    }
}