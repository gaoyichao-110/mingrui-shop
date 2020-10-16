package com.baidu.shop.service;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.Result;
import com.baidu.shop.entity.CategoryEntity;
import com.baidu.shop.validate.group.MingruiOperation;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "商品分类接口")
public interface CategoryService {
    @ApiOperation(value = "通过查询商品分类")
    @GetMapping(value = "category/list")
    public Result<List<CategoryEntity>> getCategoryByPid(Integer pid);

    @ApiOperation(value = "新增商品分类")
    @PostMapping(value = "category/save")
    public Result<JSONObject> saveCategorByPid(@Validated({MingruiOperation.Add.class})@RequestBody CategoryEntity entity);

    @ApiOperation(value = "新增商品分类")
    @PutMapping(value = "category/update")
    public Result<JSONObject> updateCategorBypid(@Validated({MingruiOperation.Update.class})@RequestBody CategoryEntity entity);


    @ApiOperation(value = "删除商品分类")
    @DeleteMapping(value = "category/delete")
    public Result<JSONObject> deleteCategorBypid(Integer id);

    @ApiOperation(value = "通过品牌id查询出来包含分类的信息")
    @GetMapping(value = "category/getByBrand")
    public Result<List<CategoryEntity>> getByBrand(@RequestParam Integer brandId);

    @ApiOperation(value = "通过品牌id集合查询出来包含分类的信息")
    @GetMapping(value = "category/getCategoryByIdList")
    Result<List<CategoryEntity>> getCategoryByIdList(@RequestParam @SpringQueryMap String cidStr);
}
