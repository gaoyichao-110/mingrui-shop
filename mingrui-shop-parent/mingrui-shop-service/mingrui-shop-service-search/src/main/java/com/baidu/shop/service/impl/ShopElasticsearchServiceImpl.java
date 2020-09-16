package com.baidu.shop.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.SkuDTO;
import com.baidu.shop.dto.SpuDTO;
import com.baidu.shop.feign.GoodsFeign;
import com.baidu.shop.service.ShopElasticsearchService;
import com.baidu.shop.status.HTTPStatus;
import com.baidu.shop.utils.JSONUtil;
import com.google.gson.JsonObject;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ClassName ShopElasticsearchServiceImpl
 * @Description: TODO
 * @Author shenyaqi
 * @Date 2020/9/16
 * @Version V1.0
 **/
@RestController
public class ShopElasticsearchServiceImpl extends BaseApiService implements ShopElasticsearchService {

    @Autowired
    private GoodsFeign goodsFeign;

    @Override
    public Result<JsonObject> esGoods() {
        SpuDTO spuDTO = new SpuDTO();
        spuDTO.setPage(1);
        spuDTO.setRows(5);
        Result<List<SpuDTO>> spuInfo = goodsFeign.getSpuInfo(spuDTO);

        if(spuInfo.getCode() == HTTPStatus.OK){
            //spu数据
            List<SpuDTO> spuList = spuInfo.getData();

            spuList.stream().forEach(spu -> {

                //通过spuID查询skuList
                Result<List<SkuDTO>> skuResult = goodsFeign.getSkuBySpuID(spu.getId());

                if(skuResult.getCode() == HTTPStatus.OK){

                    List<SkuDTO> skuList = skuResult.getData();

                    String skuJsonStr = JSONUtil.toJsonString(skuList);
                    System.out.println(skuJsonStr);
                    System.out.println(skuList);
                }
            });
        }
        //  System.out.println(spuInfo);

        return this.setResultSuccess();
    }
}
