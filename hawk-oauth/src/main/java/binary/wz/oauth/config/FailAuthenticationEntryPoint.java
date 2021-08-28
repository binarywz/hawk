package binary.wz.oauth.config;

import binary.wz.common.constant.ApiConstant;
import binary.wz.common.model.domain.ResultInfo;
import binary.wz.common.util.ResultInfoUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author binarywz
 * @date 2021/8/28 17:50
 * @description: 认证失败处理
 */
@Component
public class FailAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Resource
    private ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        // 返回 JSON
        response.setContentType("application/json;charset=utf-8");
        // 状态码 401
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        // 写出
        PrintWriter out = response.getWriter();
        String errorMessage = authException.getMessage();
        if (StringUtils.isBlank(errorMessage)) {
            errorMessage = "登录失效!";
        }
        ResultInfo result = ResultInfoUtil.buildError(ApiConstant.ERROR_CODE,
                errorMessage, request.getRequestURI());
        out.write(objectMapper.writeValueAsString(result));
        out.flush();
        out.close();
    }
}
