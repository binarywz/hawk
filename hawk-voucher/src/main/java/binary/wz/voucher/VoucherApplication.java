package binary.wz.voucher;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author binarywz
 * @date 2021/9/1 22:16
 * @description:
 */
@MapperScan("binary.wz.voucher.mapper")
@SpringBootApplication
public class VoucherApplication {

    public static void main(String[] args) {
        SpringApplication.run(VoucherApplication.class, args);
    }

}
