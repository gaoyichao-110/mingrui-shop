package com.baidu.shop.web;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.Entity.UserEntity;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.business.UserOauthService;
import com.baidu.shop.config.JwtConfig;
import com.baidu.shop.dto.UserInfo;
import com.baidu.shop.status.HTTPStatus;
import com.baidu.shop.utils.CookieUtils;
import com.baidu.shop.utils.JwtUtils;
import com.baidu.shop.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @ClassName UserOauthController
 * @Description: TODO
 * @Author shenyaqi
 * @Date 2020/10/15
 * @Version V1.0
 **/
@RestController
@RequestMapping(value = "oauth")
public class UserOauthController extends BaseApiService {

    @Autowired
    private UserOauthService userOauthService;

    @Resource
    private JwtConfig jwtConfig;

    //用户登录验证
    @PostMapping(value = "login")
    public Result<JSONObject> oauthLogin(@RequestBody UserEntity userEntity
            , HttpServletRequest request , HttpServletResponse response){

        //获得用户传过来的用户名和密码去,调用方法去持久层查询
        String tocken = userOauthService.oauthLogin(userEntity,jwtConfig);

        //判断传过来的tocken是否为空,空的话直接返回错误
        if (StringUtil.isEmpty(tocken)) {
           return this.setResultError(HTTPStatus.ERROR,"用户名或者密码错误");
        }

        //将tocken放到Cookie中  将cook的name 和获得到的tocken,超时时间放进去
        CookieUtils.setCookie(request,response,jwtConfig.getCookieName(),tocken,jwtConfig.getCookieMaxAge(),true);
        return this.setResultSuccess();
    }

    //效验用户是否登录
    @GetMapping(value = "verify")   //@CookieValue(value = "MRSHOP_TOKEN") 从cookie中获取值value="cookie的属性名"
    public Result<UserInfo> verifyUser(@CookieValue(value = "MRSHOP_TOKEN") String token
                                        ,HttpServletRequest request,HttpServletResponse response){

        try {                           //获取token中的用户信息   token和公钥
            UserInfo userInfo = JwtUtils.getInfoFromToken(token,jwtConfig.getPublicKey());
            //将userInfo中的id和name返回到页面
            return this.setResultSuccess(userInfo);
        } catch (Exception e) {
            e.printStackTrace();
            return this.setResultError(403,"");
        }
    }

}
