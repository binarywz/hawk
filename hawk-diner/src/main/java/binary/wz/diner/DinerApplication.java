package binary.wz.diner;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author binarywz
 * @date 2021/8/21 22:28
 * @description:
 */
@MapperScan("binary.wz.diner.mapper")
@SpringBootApplication
public class DinerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DinerApplication.class, args);
    }

}
