package com.baidu.shop.feign;

import com.baidu.shop.service.GoodsService;
import org.springframework.cloud.openfeign.FeignClient;


import java.util.List;

@FeignClient(value = "xxx-service")
public interface GoodsFeign extends GoodsService {
}
