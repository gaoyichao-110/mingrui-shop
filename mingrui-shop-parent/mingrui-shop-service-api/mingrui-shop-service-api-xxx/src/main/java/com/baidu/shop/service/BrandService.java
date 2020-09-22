package com.baidu.shop.service;

import com.baidu.shop.base.Result;
import com.baidu.shop.dto.BrandDTO;
import com.baidu.shop.entity.BrandEntity;
import com.baidu.shop.validate.group.MingruiOperation;
import com.github.pagehelper.PageInfo;
import com.google.gson.JsonObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.ibatis.annotations.Delete;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "品牌接口")
public interface BrandService {

    @ApiOperation(value = "获取品牌的信息")
    @GetMapping(value = "brand/getBrandInfo")
    public Result<PageInfo<BrandEntity>> getBrandInfo(BrandDTO brandDTO);

    @ApiOperation(value = "增加品牌的信息")
    @PostMapping(value = "brand/save")
    public Result<JsonObject> save(@Validated({MingruiOperation.Add.class}) @RequestBody BrandDTO brandDTO);

    @ApiOperation(value = "修改品牌的信息")
    @PutMapping(value = "brand/save")
    public Result<JsonObject> update(@Validated({MingruiOperation.Update.class}) @RequestBody BrandDTO brandDTO);

    @ApiOperation(value = "通过id删除品牌信息")
    @DeleteMapping(value = "brand/delete")
    public Result<JsonObject> delete(Integer id);

    @ApiOperation(value="根据分类id查询出品牌信息")
    @GetMapping(value = "brand/getCategoryAndBrand")
    public Result<List<BrandEntity>> getCategoryAndBrand(Integer cid);

    @ApiOperation(value="根据分类id查询出品牌信息")
    @GetMapping(value = "brand/getBrandByIdList")
    Result<List<BrandEntity>> getBrandByIdList(@RequestParam String brandList1);
}