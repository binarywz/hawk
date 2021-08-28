package binary.wz.gateway.handler;

import binary.wz.common.constant.ApiConstant;
import binary.wz.common.model.domain.ResultInfo;
import binary.wz.common.util.ResultInfoUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.nio.charset.Charset;

/**
 * @author binarywz
 * @date 2021/8/28 22:52
 * @description: 认证/授权失败统一处理
 */
@Component
public class AuthExceptionHandler {
    @Resource
    private ObjectMapper objectMapper;

    public Mono<Void> writeError(ServerWebExchange exchange, String error) {
        ServerHttpResponse response = exchange.getResponse();
        ServerHttpRequest request = exchange.getRequest();
        response.setStatusCode(HttpStatus.OK);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        ResultInfo resultInfo = ResultInfoUtil.buildError(ApiConstant.NO_LOGIN_CODE, ApiConstant.NO_LOGIN_MESSAGE, request.getURI().getPath());
        String resultInfoJson = null;
        DataBuffer buffer = null;
        try {
            resultInfoJson = objectMapper.writeValueAsString(resultInfo);
            buffer =  response.bufferFactory().wrap(resultInfoJson.getBytes(Charset.forName("UTF-8")));
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
        }

        return response.writeWith(Mono.just(buffer));
    }
}
