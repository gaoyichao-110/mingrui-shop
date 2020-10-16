package com.baidu.shop.impl;


import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.constant.MrShopConstant;
import com.baidu.shop.constant.UserConstant;
import com.baidu.shop.dto.UserDTO;
import com.baidu.shop.entity.UserEntity;
import com.baidu.shop.mapper.UserMapper;
import com.baidu.shop.redisRepository.RedisRepository;
import com.baidu.shop.service.UserService;
import com.baidu.shop.status.HTTPStatus;
import com.baidu.shop.utils.BCryptUtil;
import com.baidu.shop.utils.BaiduBeanUtil;

import com.baidu.shop.utils.LuosimaoDuanxinUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @ClassName UserServiceImpl
 * @Description: TODO
 * @Author shenyaqi
 * @Date 2020/10/13
 * @Version V1.0
 **/
@RestController
@Slf4j
public class UserServiceImpl extends BaseApiService implements UserService {

    @Resource
    private UserMapper userMapper;

    @Autowired
    private RedisRepository redisRepository;

    //用户注册方法
    @Override
    public Result<JSONObject> register(UserDTO userDTO) {

        UserEntity userEntity = BaiduBeanUtil.copyProperties(userDTO, UserEntity.class);
        //给密码加密
        userEntity.setPassword(BCryptUtil.hashpw(userEntity.getPassword(),BCryptUtil.gensalt()));
        //里边有创建日期
        userEntity.setCreated(new Date());
        //userEntity里边有数据之后新增到数据库
        userMapper.insertSelective(userEntity);
        return this.setResultSuccess();
    }

    //效验用户名和手机号是否为空
    @Override
    public Result<List<UserEntity>> checkUserNameOrPhone(String value, Integer type) {
        Example example = new Example(UserEntity.class);
        Example.Criteria criteria = example.createCriteria();
        //首先先判断用户名和手机号是否为空
        if(type != null && value != null){
            //再判断传过来的是用户名或者是手机号,判断出来之后去数据库查询
            if(type == UserConstant.USER_TYPE_USERNAME){
                criteria.andEqualTo("username",value);
            }else if(type == UserConstant.USER_TYPE_PHONE){
                criteria.andEqualTo("phone",value);
            }
        }
        List<UserEntity> userEntities = userMapper.selectByExample(example);

        return this.setResultSuccess(userEntities);
    }

    //想手机号发送验证吗
    @Override
    public Result<JSONObject> sendValidCode(UserDTO userDTO) {

        //随机生成验证码
        String code = (int)((Math.random() * 9 + 1) * 100000) + "";

        log.debug("向手机号码:{} 发送验证码:{}",userDTO.getPhone(),code);
        //通过封装的redisRepository里操作数据类型的方法将验证码发送到redis库
        redisRepository.set(MrShopConstant.USER_PHONE_CODE_PRE + userDTO.getPhone(),code);
        //通过expic这个方法让这个验证码120秒后失效
        redisRepository.expire(MrShopConstant.USER_PHONE_CODE_PRE + userDTO.getPhone(),120);
        //两个螺丝帽方法,一个是发送短信服务,一个是发送语音服务
//        LuosimaoDuanxinUtil.sendSpeak(userDTO.getPhone(),code);
        return this.setResultSuccess();
    }

    @Override
    public Result<JSONObject> checkValidCode(String phone, String validcode) {

        //传来了手机号还有验证码,先将手机号传到redis库去查询该手机号刚刚生成的验证码
        String redisValidCode = redisRepository.get(MrShopConstant.USER_PHONE_CODE_PRE + phone);
        //将传到后台的验证码与redis库里的验证吗比较是否一致
        if(!validcode.equals(redisValidCode)){
            return this.setResultError(HTTPStatus.PARAMS_CODE_ERROR,"输入的验证码不正确");
        }
        return this.setResultSuccess();
    }
}
