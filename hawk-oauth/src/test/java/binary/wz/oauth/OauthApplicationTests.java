package binary.wz.oauth;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import javax.annotation.Resource;

/**
 * @author binarywz
 * @date 2021/9/4 11:37
 * @description:
 */
@SpringBootTest
@AutoConfigureMockMvc
public class OauthApplicationTests {

    @Resource
    protected MockMvc mockMvc;

}
