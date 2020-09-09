package com.baidu.shop.service;

import com.baidu.shop.base.Result;
import com.baidu.shop.dto.SkuDTO;
import com.baidu.shop.dto.SpuDTO;
import com.baidu.shop.entity.SpuDetailEntity;
import com.baidu.shop.entity.SpuEntity;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageInfo;
import com.google.gson.JsonObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "商品接口")
public interface GoodsService {

    @ApiOperation(value="获取spu信息")
    @GetMapping(value = "goods/getSpuIno")
    public Result<PageInfo<SpuEntity>> getSpuInfo(SpuDTO spuDTO);

    @ApiOperation(value = "给spu新增信息")
    @PostMapping(value = "goods/saveSpu")
    Result<JsonObject> saveSpu(@RequestBody SpuDTO spuDTO);

    @ApiOperation(value = "获取spu详情信息")
    @GetMapping(value = "goods/getSpuDetailBydSpu")
    public Result<SpuDetailEntity> getSpuDetailBydSpu(Integer spuId);



    @ApiOperation(value = "获取sku的信息")
    @GetMapping(value="goods/getSkuBySpuID")
    Result<List<SkuDTO>> getSkuBySpuID(Integer spuId);

    @ApiOperation(value = "给spu修改信息")
    @PutMapping(value = "goods/saveSpu")
    Result<JsonObject> editSpu(@RequestBody SpuDTO spuDTO);

    @ApiOperation(value = "删除商品信息")
    @DeleteMapping(value = "goods/delete")
    Result<JsonObject> delete(Integer spuId);

}
