package com.baidu.shop.business;


import com.baidu.shop.Entity.UserEntity;
import com.baidu.shop.config.JwtConfig;

public interface UserOauthService {


    String oauthLogin(UserEntity userEntity, JwtConfig jwtConfig);
}
