package com.baidu.shop.service.impl;

import com.baidu.shop.base.Result;
import com.baidu.shop.dto.BrandDTO;
import com.baidu.shop.dto.SpuDTO;
import com.baidu.shop.entity.BrandEntity;
import com.baidu.shop.feign.BrandFeign;
import com.baidu.shop.feign.GoodsFeign;
import com.baidu.shop.service.PageService;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName PageServiceImpl
 * @Description: TODO
 * @Author shenyaqi
 * @Date 2020/9/23
 * @Version V1.0
 **/
@Service
public class PageServiceImpl implements PageService {

    @Resource
    private GoodsFeign goodsFeign;

    @Resource
    private BrandFeign brandFeign;

    @Override
    public Map<String, Object> getPageBySpuId(Integer spuId) {
        Map<String, Object> map = new HashMap<>();

        SpuDTO spuDTO = new SpuDTO();
        spuDTO.setId(spuId);

        Result<List<SpuDTO>> spuInfo = goodsFeign.getSpuInfo(spuDTO);
        if(spuInfo.getCode() == 200){
            if(spuInfo.getData().size() == 1){
                SpuDTO spuInfo1 = spuInfo.getData().get(0);
                map.put("spuInfo",spuInfo1);

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
            }
        }
        return map;
    }
}
