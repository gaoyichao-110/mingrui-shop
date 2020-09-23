package com.baidu.shop.service;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.SpecParamDTO;
import com.baidu.shop.dto.SpecificationDTO;
import com.baidu.shop.entity.SpecParamEntity;
import com.baidu.shop.entity.SpecificationEntity;
import com.baidu.shop.validate.group.MingruiOperation;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags="规格接口")
public interface SpecificationService {

//    接口文档注解
    @ApiOperation(value="通过条件查询规格组")
    @GetMapping(value="specgroup/listSpecification")
    public Result<List<SpecificationEntity>> listSpecification(SpecificationDTO specificationDTO);

    @ApiOperation(value = "给规格新增数据")
    @PostMapping(value="specgroup/save")
    public Result<JSONObject> save(@Validated({MingruiOperation.Add.class}) @RequestBody SpecificationDTO specificationDTO);

    @ApiOperation(value = "给规格修改数据")
    @PutMapping(value="specgroup/save")
    public Result<JSONObject> update(@Validated({MingruiOperation.Update.class}) @RequestBody SpecificationDTO specificationDTO);

    @ApiOperation(value = "给规格删除数据")
    @DeleteMapping(value="specgroup/delete")
    public Result<JSONObject> delete(Integer id);


    @ApiOperation(value="通过条件查询规格参数")
    @GetMapping(value="specparam/listSpecParam")
    public Result<List<SpecParamEntity>> listSpecParam(@SpringQueryMap SpecParamDTO specParamDTO);

    @ApiOperation(value="给规格上传参数")
    @PostMapping(value = "specparam/save")
    Result<JSONObject> save(@Validated({MingruiOperation.Add.class}) @RequestBody SpecParamDTO specParamDTO);

    @ApiOperation(value = "给规格参数修改数据")
    @PutMapping(value = "specparam/save")
    Result<JSONObject> update(@Validated({MingruiOperation.Update.class}) @RequestBody SpecParamDTO specParamDTO);


    @ApiOperation(value="删除规格中的数据")
    @DeleteMapping(value = "specparam/delete")
    Result<JSONObject> specDelete(Integer id);


}
