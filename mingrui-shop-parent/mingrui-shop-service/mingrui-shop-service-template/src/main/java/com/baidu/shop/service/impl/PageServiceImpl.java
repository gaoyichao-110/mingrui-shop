package com.baidu.shop.service.impl;

import com.baidu.shop.base.Result;
import com.baidu.shop.dto.*;
import com.baidu.shop.entity.*;
import com.baidu.shop.feign.BrandFeign;
import com.baidu.shop.feign.CategoryFeign;
import com.baidu.shop.feign.GoodsFeign;
import com.baidu.shop.feign.SpecificationFeign;
import com.baidu.shop.service.PageService;
import com.baidu.shop.utils.BaiduBeanUtil;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ClassName PageServiceImpl
 * @Description: TODO
 * @Author shenyaqi
 * @Date 2020/9/23
 * @Version V1.0
 **/
//@Service
public class PageServiceImpl implements PageService {

//    @Resource
    private GoodsFeign goodsFeign;

//    @Resource
    private BrandFeign brandFeign;

//    @Resource
    private CategoryFeign categoryFeign;

//    @Resource
    private SpecificationFeign specificationFeign;

//    @Override
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
