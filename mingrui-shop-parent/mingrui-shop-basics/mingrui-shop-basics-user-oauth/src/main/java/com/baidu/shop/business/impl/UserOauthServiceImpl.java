package com.baidu.shop.business.impl;

import com.baidu.shop.Entity.UserEntity;
import com.baidu.shop.business.UserOauthService;
import com.baidu.shop.config.JwtConfig;
import com.baidu.shop.dto.UserInfo;
import com.baidu.shop.mapper.UserOauthMapper;
import com.baidu.shop.utils.BCryptUtil;
import com.baidu.shop.utils.BaiduBeanUtil;
import com.baidu.shop.utils.JwtUtils;
import org.aspectj.weaver.patterns.IToken;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.List;

/**
 * @ClassName UserOauthServiceImpl
 * @Description: TODO
 * @Author shenyaqi
 * @Date 2020/10/15
 * @Version V1.0
 **/
@Service
public class UserOauthServiceImpl implements UserOauthService {

    @Resource
    private UserOauthMapper userOauthMapper;

    @Override
    public String oauthLogin(UserEntity userEntity, JwtConfig jwtConfig) {

        //先构建一个条件查询,通过用户名去数据库查询这条信息
        Example example = new Example(UserEntity.class);
        example.createCriteria().andEqualTo("username",userEntity.getUsername());

        //查询出来的用户名放到list里边
        List<UserEntity> list = userOauthMapper.selectByExample(example);
        String tocken = null;
        //判断有没有查询出来信息,没有就完了.
        if(list.size() == 1){
            //有信息的话比较一下密码是否正确
            if (BCryptUtil.checkpw(userEntity.getPassword(),list.get(0).getPassword())) {
                try {              //创建tocken  获得UserInfo里的数据,里边有用户名和密码,还有私钥和超时的时间
                    tocken = JwtUtils.generateToken(BaiduBeanUtil.copyProperties(list.get(0), UserInfo.class),
                            jwtConfig.getPrivateKey(),jwtConfig.getExpire());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }


        return tocken;
    }
}
