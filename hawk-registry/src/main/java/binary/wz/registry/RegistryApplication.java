package binary.wz.registry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * @Author cuiwz
 * @Date 2021/8/8 0:08
 * @Description:
 */
@EnableEurekaServer // 激活 Eureka Server 注册中心组件
@SpringBootApplication
public class RegistryApplication {

    public static void main(String[] args) {
        SpringApplication.run(RegistryApplication.class, args);
    }

}
