package com.baidu.shop.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.*;
import com.baidu.shop.entity.*;
import com.baidu.shop.feign.BrandFeign;
import com.baidu.shop.feign.CategoryFeign;
import com.baidu.shop.feign.GoodsFeign;
import com.baidu.shop.feign.SpecificationFeign;
import com.baidu.shop.service.PageService;

import com.baidu.shop.service.TemplateService;
import com.baidu.shop.utils.BaiduBeanUtil;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ClassName TemplateServiceImpl
 * @Description: TODO
 * @Author shenyaqi
 * @Date 2020/9/25
 * @Version V1.0
 **/
@RestController
public class TemplateServiceImpl extends BaseApiService implements TemplateService {

    @Resource
    private GoodsFeign goodsFeign;

    @Resource
    private BrandFeign brandFeign;

    @Resource
    private CategoryFeign categoryFeign;

    @Resource
    private SpecificationFeign specificationFeign;

    @Value(value = "${mrshop.static.html.path}")
    private String staticHTMLPath;

    //注入静态化模版
    @Autowired
    private TemplateEngine templateEngine;

    @Override
    public Result<JSONObject> delHTMLBySpuId(Integer spuId) {
        File file = new File(staticHTMLPath + File.separator + spuId + ".html");
        if(!file.delete()){
            return this.setResultError("文件删除失败");
        }

        return this.setResultSuccess();
    }

    @Override
    public Result<JSONObject> createStaticHTMLTemplate(Integer spuId) {

        //获取spu的详细信息放到map中
        Map<String, Object> map = this.getPageBySpuId(spuId);

        //创建模板引擎上下文
        Context context = new Context();

        //将所有准备的数据放到模板中
        context.setVariables(map);

        //创建文件 param1:文件路径 param2:文件名称
        File file = new File(staticHTMLPath, spuId + ".html");
        //构建文件输出流
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(file, "UTF-8");
            //根据模板生成静态文件
            //param1:模板名称 params2:模板上下文[上下文中包含了需要填充的数据],文件输出流
            templateEngine.process("item",context,writer);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }finally {
            writer.close();
        }


        return this.setResultSuccess();
    }

    @Override
    public Result<JSONObject> initStaticHTMLTemplate() {

        //先查询出来spu的信息,等下放到html文件里
        Result<List<SpuDTO>> spuInfoResult = goodsFeign.getSpuInfo(new SpuDTO());

        if(spuInfoResult.getCode() == 200){
            List<SpuDTO> data = spuInfoResult.getData();
            data.stream().forEach(spuDTO -> {
                //将spu的信息放到静态页面里
                this.createStaticHTMLTemplate(spuDTO.getId());
            });
        }
        return this.setResultSuccess();
    }




    //获取spu的详细信息
    public Map<String, Object> getPageBySpuId(Integer spuId) {
        Map<String, Object> map = new HashMap<>();

        //要通过spuId去查询一下商品
        SpuDTO spuDTO = new SpuDTO();
        spuDTO.setId(spuId);
        //查询出商品信息
        Result<List<SpuDTO>> spuInfo = goodsFeign.getSpuInfo(spuDTO);
        if(spuInfo.getCode() == 200){
            if(spuInfo.getData().size() == 1){
                SpuDTO spuInfo1 = spuInfo.getData().get(0);
                map.put("spuInfo",spuInfo1);

                //查询出detail信息商品的详细信息
                Result<SpuDetailEntity> spuDetailResult = goodsFeign.getSpuDetailBydSpu(spuId);
                if (spuDetailResult.getCode() == 200) {
                    SpuDetailEntity data = spuDetailResult.getData();
                    map.put("spuDetailInfo",data);
                }


                //通过spuId去查询出来品牌的信息
                BrandDTO brandDTO = new BrandDTO();
                brandDTO.setId(spuInfo1.getBrandId());
                Result<PageInfo<BrandEntity>> brandInfo = brandFeign.getBrandInfo(brandDTO);
                if(brandInfo.getCode() == 200){
                    PageInfo<BrandEntity> data = brandInfo.getData();

                    List<BrandEntity> brandList = data.getList();

                    if(brandList.size() == 1){
                        map.put("brandInfo",brandList.get(0));
                    }
                }

                //查询出商品的详情信息
                String cidStr = String.join(",", Arrays.asList(spuInfo1.getCid1() + "", spuInfo1.getCid2() + "", spuInfo1.getCid3() + ""));
                Result<List<CategoryEntity>> cateResult = categoryFeign.getCategoryByIdList(cidStr);
                if(cateResult.getCode() == 200){
                    List<CategoryEntity> data = cateResult.getData();
                    map.put("cateList",data);
                }

                //查询出特有的规格参数
                SpecParamDTO specParamDTO = new SpecParamDTO();
                specParamDTO.setCid(spuInfo1.getCid3());
                specParamDTO.setGeneric(false);
                Result<List<SpecParamEntity>> specParam = specificationFeign.listSpecParam(specParamDTO);

                if(specParam.getCode() == 200){
                    HashMap<Integer, String> hashMap = new HashMap<>();

                    specParam.getData().stream().forEach(spec -> {
                        hashMap.put(spec.getId(),spec.getName());
                    });

                    map.put("specParamList",hashMap);

                }

                //通过spuId去查询出sku的信息
                Result<List<SkuDTO>> skuResult = goodsFeign.getSkuBySpuID(spuId);

                if(skuResult.getCode() == 200){
                    List<SkuDTO> data = skuResult.getData();
                    map.put("skuList",data);
                }

                SpecificationDTO specificationDTO = new SpecificationDTO();
                specificationDTO.setId(spuInfo1.getCid3());
                Result<List<SpecificationEntity>> specificationResult = specificationFeign.listSpecification(specificationDTO);

                if(specificationResult.getCode() == 200){
                    List<SpecificationEntity> specificationResultData = specificationResult.getData();

                    List<SpecificationDTO> specGroupParam = specificationResultData.stream().map(specGroup -> {
                        SpecificationDTO specificationDTOResult = BaiduBeanUtil.copyProperties(specGroup, SpecificationDTO.class);

                        SpecParamDTO specParamDTO1 = new SpecParamDTO();
                        specParamDTO1.setGroupId(specificationDTOResult.getId());
                        specParamDTO1.setGeneric(true);

                        Result<List<SpecParamEntity>> specParam1 = specificationFeign.listSpecParam(specParamDTO1);
                        if (specParam1.getCode() == 200) {
                            List<SpecParamEntity> data = specParam1.getData();

                            specificationDTOResult.setSpecParams(data);

                        }
                        return specificationDTOResult;
                    }).collect(Collectors.toList());
                    map.put("specGroupParam",specGroupParam);
                }

            }
        }
        return map;
    }
}
