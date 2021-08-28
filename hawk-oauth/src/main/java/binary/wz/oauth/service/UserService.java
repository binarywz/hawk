package binary.wz.oauth.service;

import binary.wz.common.model.domain.SignInIdentity;
import binary.wz.common.model.pojo.Diner;
import binary.wz.common.util.AssertUtils;
import binary.wz.oauth.mapper.DinerMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author binarywz
 * @date 2021/8/22 16:41
 * @description: 登录校验
 */
@Service
public class UserService implements UserDetailsService {

    @Resource
    private DinerMapper dinerMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AssertUtils.isNotEmpty(username, "请输入用户名");
        Diner diner = dinerMapper.selectByAccountInfo(username);
        if (diner == null) {
            throw new UsernameNotFoundException("用户不存在，请重新输入");
        }
        // 初始化登录认证对象
        SignInIdentity signInIdentity = new SignInIdentity();
        // 拷贝属性
        BeanUtils.copyProperties(diner, signInIdentity);
        return signInIdentity;
    }
}
