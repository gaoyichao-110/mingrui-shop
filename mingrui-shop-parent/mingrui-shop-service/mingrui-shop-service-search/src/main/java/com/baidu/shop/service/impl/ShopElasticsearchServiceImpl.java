package com.baidu.shop.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.document.GoodsDoc;
import com.baidu.shop.dto.SkuDTO;
import com.baidu.shop.dto.SpecParamDTO;
import com.baidu.shop.dto.SpuDTO;
import com.baidu.shop.entity.BrandEntity;
import com.baidu.shop.entity.CategoryEntity;
import com.baidu.shop.entity.SpecParamEntity;
import com.baidu.shop.entity.SpuDetailEntity;
import com.baidu.shop.feign.BrandFeign;
import com.baidu.shop.feign.CategoryFeign;
import com.baidu.shop.feign.GoodsFeign;
import com.baidu.shop.feign.SpecificationFeign;

import com.baidu.shop.response.GoodsResponse;
import com.baidu.shop.service.ShopElasticsearchService;
import com.baidu.shop.status.HTTPStatus;
import com.baidu.shop.utils.ESHighLightUtil;
import com.baidu.shop.utils.JSONUtil;
import com.baidu.shop.utils.StringUtil;
import com.google.gson.JsonObject;
import com.sun.xml.internal.bind.WhiteSpaceProcessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.Resource;
import java.sql.SQLOutput;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName ShopElasticsearchServiceImpl
 * @Description: TODO
 * @Author shenyaqi
 * @Date 2020/9/16
 * @Version V1.0
 **/
@Slf4j
@RestController
public class ShopElasticsearchServiceImpl extends BaseApiService implements ShopElasticsearchService {

    @Autowired
    private GoodsFeign goodsFeign;

    @Autowired
    private SpecificationFeign specificationFeign;

    @Autowired
    private BrandFeign brandFeign;

    @Resource
    private CategoryFeign categoryFeign;

    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Override
    public Result<JSONObject> saveData(Integer spuId) {
        //通过spuId查询数据
        SpuDTO spuDTO = new SpuDTO();
        spuDTO.setId(spuId);

        List<GoodsDoc> goodsDocs = this.esGoods(spuDTO);
        elasticsearchRestTemplate.save(goodsDocs.get(0));
        return this.setResultSuccess();
    }

    @Override
    public Result<JSONObject> delData(Integer spuId) {
        GoodsDoc goodsDoc = new GoodsDoc();
        goodsDoc.setId(spuId.longValue());
        elasticsearchRestTemplate.delete(goodsDoc);
        return this.setResultSuccess();
    }


    private List<GoodsDoc> esGoods(SpuDTO spuDTO) {

        List<GoodsDoc> goodss = new ArrayList<>();

        //查询出来spuDTO的信息
        Result<List<SpuDTO>> spuInfo = goodsFeign.getSpuInfo(spuDTO);

        if(spuInfo.getCode() == HTTPStatus.OK){

            //spu数据
            List<SpuDTO> spuList = spuInfo.getData();
            //将查询出来的spuDTO方法放到GoodsDoc里
            spuList.stream().forEach(spu -> {

                GoodsDoc goodsDoc = new GoodsDoc();

                goodsDoc.setId(spu.getId().longValue());
                goodsDoc.setTitle(spu.getTitle());
                goodsDoc.setBrandName(spu.getBrandName());
                goodsDoc.setCategoryName(spu.getCategoryName());
                goodsDoc.setSubTitle(spu.getSubTitle());
                goodsDoc.setBrandId(spu.getBrandId().longValue());
                goodsDoc.setCid1(spu.getCid1().longValue());
                goodsDoc.setCid2(spu.getCid2().longValue());
                goodsDoc.setCid3(spu.getCid3().longValue());
                goodsDoc.setCreateTime(spu.getCreateTime());


                //通过spuID查询skuList
                Map<List<Long>, List<Map<String, Object>>> skus = this.getSkusAndPriceList(spu.getId());

                //遍历传过来的sku数据,将sku的数据放到GoodsDoc中去
                skus.forEach((key,value) ->{
                    goodsDoc.setPrice(key);
                    goodsDoc.setSkus(JSONUtil.toJsonString(value));
                });

                //通过spu信息查询出规格参数
                Map<String, Object> specMap = this.getSpecMap(spu);

                //将规格参数也放到GoodsDoc中
                goodsDoc.setSpecs(specMap);
                goodss.add(goodsDoc);
                System.out.println(goodss);

            });
        }
        //  System.out.println(spuInfo);

        return goodss;
    }

    //根据spuid来查询出来sku的信息,放到Map里.Map集合里可以放各种参数
    private Map<List<Long>,List<Map<String, Object>>> getSkusAndPriceList(Integer spuId){

        Map<List<Long>,List<Map<String, Object>>> hashMap = new HashMap<>();

        //通过spuId查询到sku的信息
        Result<List<SkuDTO>> skuResult = goodsFeign.getSkuBySpuID(spuId);

        //建两个集合来接收获取到的sku参数,等一会将两个参数传上去
        List<Long> priceList = new ArrayList<>();
        List<Map<String, Object>> skuMap = null;

        //判断sku传来的数据==200
        if(skuResult.getCode() == HTTPStatus.OK){

            //获得到sku的数据
            List<SkuDTO> skuList = skuResult.getData();
            //遍历sku传来的数据,将值赋到一个map集合中去,等会再将这个map集合返回回去
            skuMap = skuList.stream().map(sku -> {

                Map<String, Object> map = new HashMap<>();

                map.put("id", sku.getId());
                map.put("title", sku.getTitle());
                map.put("images", sku.getImages());
                map.put("price", sku.getPrice());

                //一个返回到list集合,一个返回到map集合中
                priceList.add(sku.getPrice().longValue());
                return map;

            }).collect(Collectors.toList());
        }

        //获取到两个集合的参数,返回回去
        hashMap.put(priceList,skuMap);
        return hashMap;
    }

    //通过spu信息查询出规格参数
    private Map<String, Object> getSpecMap(SpuDTO spuDTO) {

        //商品信息
        SpecParamDTO specParamDTO = new SpecParamDTO();
        //查询cid3的下面的规格参数
        specParamDTO.setCid(spuDTO.getCid3());
        Result<List<SpecParamEntity>> specParamResult = specificationFeign.listSpecParam(specParamDTO);

        HashMap<String, Object> specMap = new HashMap<>();

        if (specParamResult.getCode() == HTTPStatus.OK) {

            //只有规格参数的id和规格参数的名字
            List<SpecParamEntity> paramList = specParamResult.getData();

            //通过spuId去查询spuDetail,detail里面有通用的特殊规格参数的值
            Result<SpuDetailEntity> spuDetailBydSpu = goodsFeign.getSpuDetailBydSpu(spuDTO.getId());

            if(spuDetailBydSpu.getCode() == HTTPStatus.OK){
                SpuDetailEntity spuDetailList = spuDetailBydSpu.getData();

                //通用规格参数的值GenericSpec通用参数的属性
                String genericSpec = spuDetailList.getGenericSpec();
                Map<String, String> genericSpecMap  = JSONUtil.toMapValueString(genericSpec);

                //特有规格参数的值SpecialSpec特有规格参数的属性
                String specialSpec = spuDetailList.getSpecialSpec();
                Map<String, List<String>> specialSpecMap  = JSONUtil.toMapValueStrList(specialSpec);

                paramList.stream().forEach(param -> {
                    //判断一下是否是通用属性
                    if(param.getGeneric()){
                        //判断一下是否是数字类型,是否是用户过滤搜索
                        if(param.getNumeric() && param.getSearching()){
                            specMap.put(param.getName(),this.chooseSegment(genericSpecMap.get(param.getId()+""),param.getSegments(),param.getUnit()));
                        }else{
                            specMap.put(param.getName(),genericSpecMap.get(param.getId()+""));
                        }
                    }else{
                        specMap.put(param.getName(),specialSpecMap.get(param.getId()+""));
                    }
                });
            }

        }
        System.out.println(specMap);
        return specMap;
    }

    //用来做分段间隔值的
    private String chooseSegment(String value, String segments, String unit) {
        double val = NumberUtils.toDouble(value);
        String result = "其它";
        // 保存数值段
        for (String segment : segments.split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if(segs.length == 2){
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if(val >= begin && val < end){
                if(segs.length == 1){
                    result = segs[0] + unit + "以上";
                }else if(begin == 0){
                    result = segs[1] + unit + "以下";
                }else{
                    result = segment + unit;
                }
                break;
            }
        }
        return result;
    }


    @Override
    public Result<JSONObject> initGoodsData() {
        //创建一个索引
        IndexOperations indexOps = elasticsearchRestTemplate.indexOps(GoodsDoc.class);
        if(!indexOps.exists()){
            indexOps.create();
            log.info("索引创建成功");
            indexOps.createMapping();
            log.info("映射创建成功");
        }

        //esGoods里边有spu,sku和各种规格参数
        List<GoodsDoc> goodsDocs = this.esGoods(new SpuDTO());
        elasticsearchRestTemplate.save(goodsDocs);

        return this.setResultSuccess();
    }

    @Override
    public Result<JSONObject> clearGoodsData() {
        IndexOperations indexOperations = elasticsearchRestTemplate.indexOps(GoodsDoc.class);
        if(indexOperations.exists()){
            indexOperations.delete();
            log.info("索引删除成功");
        }
        return this.setResultSuccess();
    }

    /*
    * 搜索
    * */
    @Override
    public GoodsResponse search(String search,Integer page,String filter) {

        //判断查询的字段不能为空
        if (StringUtil.isEmpty(search)) throw new RuntimeException("查询信息不能为空");

        //查询出来了拥有上述功能的商品信息
        SearchHits<GoodsDoc> hits = elasticsearchRestTemplate.search(this.getSearchQueryBuilder(search,page,filter).build(), GoodsDoc.class);

        List<SearchHit<GoodsDoc>> highLightHit = ESHighLightUtil.getHighLightHit(hits.getSearchHits());

        List<GoodsDoc> goodsDoc = highLightHit.stream().map(searchHit -> searchHit.getContent()).collect(Collectors.toList());

        long total = hits.getTotalHits();
        //Integer totalPage = total / 10;
        //double ceil = Math.ceil(Long.valueOf(total).doubleValue() / 10);
        //基本数据类型和包装数据类型如何拆装箱?????
        long totalPage = Double.valueOf(Math.ceil(Long.valueOf(total).doubleValue() / 10)).longValue();

        //String message = total + "," + totalPage;
        //查询出来了总条数和总页数
//        Map<String, Long> messageMap = new HashMap<>();
//        messageMap.put("total",total);
//        messageMap.put("totalPage",totalPage);
//        messageMap.toString();

        Aggregations aggregations = hits.getAggregations();

        Map<Integer, List<CategoryEntity>> map = this.getCategoryByIdList(aggregations);

        List<CategoryEntity> categoryEntityList = null;
        Integer hotCid = 0;

        for(Map.Entry<Integer,List<CategoryEntity>> mapEntry : map.entrySet()){
            hotCid = mapEntry.getKey();
            categoryEntityList = mapEntry.getValue();
        }

        //通过cid查询出规格参数
        Map<String, List<String>> specParamListMap = this.getspecParam(hotCid, search);


        List<BrandEntity> brandByIdList = this.getBrandByIdList(aggregations);

        return new GoodsResponse(total,totalPage,brandByIdList,categoryEntityList,goodsDoc,specParamListMap);

    }



    //取商品的规格参数
    private Map<String, List<String>> getspecParam(Integer hotCid,String search){

        //实例出来specParamDTO,去里边查询出规格参数,给使用cid去查询,给是否过滤搜索赋值true,将数据发送到参数的server去库里查询
        SpecParamDTO specParamDTO = new SpecParamDTO();
        specParamDTO.setCid(hotCid);
        specParamDTO.setSearching(true);

        Result<List<SpecParamEntity>> specParamResult = specificationFeign.listSpecParam(specParamDTO);

        //得判断一下返回的是两百
        if(specParamResult.getCode() == 200){

            List<SpecParamEntity> specParamResultList = specParamResult.getData();
            //聚合查询,构建一下查询条件
            NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
            queryBuilder.withQuery(QueryBuilders.multiMatchQuery(search,"brandName","categoryName","title"));

            //分页必须查询出一条数据
            queryBuilder.withPageable(PageRequest.of(0,1));


            specParamResultList.stream().forEach(specParam -> {
                queryBuilder.addAggregation(AggregationBuilders.terms(specParam.getName()).field("specs."+specParam.getName()+".keyword"));
            });

            SearchHits<GoodsDoc> searchHits = elasticsearchRestTemplate.search(queryBuilder.build(), GoodsDoc.class);

            Map<String, List<String>> map = new HashMap<>();

            Aggregations aggregations = searchHits.getAggregations();

            specParamResultList.stream().forEach(specParam ->{
                Terms terms = aggregations.get(specParam.getName());
                List<? extends Terms.Bucket> buckets = terms.getBuckets();

                List<String> valueList = buckets.stream().map(bucket -> bucket.getKeyAsString()).collect(Collectors.toList());

                map.put(specParam.getName(), valueList);
            });

            return map;
        }

        return null;

    }

    //构建条件查询
    private NativeSearchQueryBuilder getSearchQueryBuilder(String search,Integer page,String filter){

        //根据字段条件查询
        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();

        if(StringUtil.isNotEmpty(filter) && filter.length() > 2){
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            Map<String, String> filterMap = JSONUtil.toMapValueString(filter);

            filterMap.forEach((key,value) -> {
                MatchQueryBuilder matchQueryBuilder = null;
                if(key.equals("cid3") || key.equals("brandId")){
                    matchQueryBuilder = QueryBuilders.matchQuery(key, value);
                }else{
                    matchQueryBuilder = QueryBuilders.matchQuery("specs."+key+".keyword",value);
                }
                boolQueryBuilder.must(matchQueryBuilder);
            });
            builder.withFilter(boolQueryBuilder);
        }

        builder.withQuery(QueryBuilders.multiMatchQuery(search,"brandName","categoryName","title"));

        //将title信息给加上高亮字段
        builder.withHighlightBuilder(ESHighLightUtil.getHighlightBuilder("title"));

        //分页
        builder.withPageable(PageRequest.of(page-1,10));

        //查询出品牌和分类的信息
        //通过cid3查询出来它下边的商品信息,还有通过brandId查询出来cid下边商品的品牌信息
        //把cid放到聚合桶内
        builder.addAggregation(AggregationBuilders.terms("cid_agg").field("cid3"));
        builder.addAggregation(AggregationBuilders.terms("brand_agg").field("brandId"));

        return builder;
    }

    //获取分类的集合
    private Map<Integer, List<CategoryEntity>> getCategoryByIdList(Aggregations aggregations){
        Terms cid_agg = aggregations.get("cid_agg");

        //返回了一个cid的集合,通过这个id的集合去查询数据
        //获得聚合桶里边的数据
        List<? extends Terms.Bucket> cidBuckets = cid_agg.getBuckets();

        List<Integer> hotCidArr = Arrays.asList(0);
        List<Long> maxCount = Arrays.asList(0L);

        Map<Integer, List<CategoryEntity>> map = new HashMap<>();

        List<String> cidList = cidBuckets.stream().map(cidbucket -> {
            Number keyAsNumber = cidbucket.getKeyAsNumber();

            if(cidbucket.getDocCount() > maxCount.get(0)){
                maxCount.set(0,cidbucket.getDocCount());
                hotCidArr.set(0,keyAsNumber.intValue());
            }
                return keyAsNumber.intValue()+"";
//            return cidbucket.getKeyAsString();
        }).collect(Collectors.toList());

        //使用String的join将集合用逗号分割,返回字符串
        String cidStr = String.join(",", cidList);
        Result<List<CategoryEntity>> categoryByIdList = categoryFeign.getCategoryByIdList(cidStr);

        map.put(hotCidArr.get(0),categoryByIdList.getData());
        return map;
    }

    //获取品牌的集合
    private List<BrandEntity> getBrandByIdList(Aggregations aggregations){

        Terms brand_agg = aggregations.get("brand_agg");
        //得到brandId集合去查询brand的信息
        List<? extends Terms.Bucket> buckets = brand_agg.getBuckets();

        List<String> brandList = buckets.stream().map(brandBucket -> brandBucket.getKeyAsNumber().intValue() + "").collect(Collectors.toList());

        String brandList1 = String.join(",", brandList);
        Result<List<BrandEntity>> brandByIdList = brandFeign.getBrandByIdList(brandList1);

        return brandByIdList.getData();

    }
}
