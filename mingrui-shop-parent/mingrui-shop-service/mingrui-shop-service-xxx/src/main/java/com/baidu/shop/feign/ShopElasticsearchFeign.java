package com.baidu.shop.feign;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(value = "search-server",contextId = "ShopElasticsearchFeign")
public interface ShopElasticsearchFeign {
    @GetMapping(value = "es/initGoodsData")
    Result<JSONObject> initGoodsData();

    @GetMapping(value = "es/clearGoodsEsData")
    Result<JSONObject> clearGoodsData();
}
