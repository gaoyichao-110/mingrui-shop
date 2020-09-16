package com.baidu.shop.service;

import com.baidu.shop.base.Result;
import com.google.gson.JsonObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;

@Api(tags = "es的接口")
public interface ShopElasticsearchService {

    @ApiOperation(value ="获取商品的信息")
    @GetMapping(value = "es/listGoods")
    Result<JsonObject> esGoods();
}
