package com.baidu.shop.service;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.Result;
import com.baidu.shop.document.GoodsDoc;
import com.baidu.shop.response.GoodsResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Api(tags = "es的接口")
public interface ShopElasticsearchService {

    @ApiOperation(value = "创建索引和映射信息")
    @GetMapping(value = "es/initGoodsData")
    Result<JSONObject> initGoodsData();

    @ApiOperation(value = "删除商品中的信息")
    @GetMapping(value = "es/clearGoodsEsData")
    Result<JSONObject> clearGoodsData();

    @ApiOperation(value = "查询search")
    @GetMapping(value = "es/search")
    GoodsResponse search(@RequestParam String search, @RequestParam Integer page,@RequestParam String filter);


}
