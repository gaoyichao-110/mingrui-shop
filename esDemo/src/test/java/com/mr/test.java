package com.mr;

import com.mr.entity.GoodsEntity;
import com.mr.repository.GoodsEsRepository;

import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.Max;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ClassName test
 * @Description: TODO
 * @Author shenyaqi
 * @Date 2020/9/14
 * @Version V1.0
 **/
//让测试再spring容器的环境下测试
@RunWith(SpringRunner.class)
//声明一个启动类,当我们启动测试的时候会自动帮我们启动测试类
@SpringBootTest(classes = {RunTesyEsApplication.class})
public class test {

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Resource
    private GoodsEsRepository goodsEsRepository;

    @Test
    public void createGoodsIndex(){
        IndexOperations zhaojunhao = elasticsearchRestTemplate.indexOps(IndexCoordinates.of("zhaojunhao"));
        zhaojunhao.create();
        System.out.println(zhaojunhao.exists()?"索引创建成功":"索引创建失败");
    }

    @Test
    public void createGaoYiChaoIndex(){
        IndexOperations zhaojunhao = elasticsearchRestTemplate.indexOps(IndexCoordinates.of("gaoyichao"));
        zhaojunhao.create();
        System.out.println(zhaojunhao.exists()?"索引创建成功":"索引创建失败");
    }

    @Test
    public void createGoods(){
        IndexOperations indexOperations = elasticsearchRestTemplate.indexOps(GoodsEntity.class);
        indexOperations.create();
        System.out.println(indexOperations.exists()?"索引创建成功":"索引创建失败");
    }

    @Test
    public void createGoodsMapping(){
        //这个构造函数会查询出有没有这个索引的存在,如果没有的话就创建一个,如果有的话就用原来的
        IndexOperations indexOperations = elasticsearchRestTemplate.indexOps(GoodsEntity.class);
        //创建映射
        indexOperations.create();
        indexOperations.createMapping(GoodsEntity.class);
        System.out.println("映射创建成功");
    }

    @Test
    public void deleteGoodsIndex(){
        IndexOperations indexOperations = elasticsearchRestTemplate.indexOps(GoodsEntity.class);
        indexOperations.delete();
        System.out.println("删除索引成功");

    }

    @Test
    public void saveData(){
        GoodsEntity entity = new GoodsEntity();
        entity.setId(1L);
        entity.setBrand("苹果");
        entity.setCategory("手机");
        entity.setImages("手机.jpg");
        entity.setPrice(1000D);
        entity.setTitle("好手机");

        goodsEsRepository.save(entity);

        System.out.println("新增成功");
    }

    @Test
    public void saveAllData(){
        GoodsEntity entity = new GoodsEntity();
        entity.setId(1L);
        entity.setBrand("苹果");
        entity.setCategory("手机");
        entity.setImages("手机.jpg");
        entity.setPrice(1000D);
        entity.setTitle("苹果手机");

        GoodsEntity entity1 = new GoodsEntity();
        entity1.setId(2L);
        entity1.setBrand("华为");
        entity1.setCategory("手机");
        entity1.setImages("手机.jpg");
        entity1.setPrice(800D);
        entity1.setTitle("华为手机");

        GoodsEntity entity2 = new GoodsEntity();
        entity2.setId(3L);
        entity2.setBrand("小米");
        entity2.setCategory("手机");
        entity2.setImages("手机.jpg");
        entity2.setPrice(700D);
        entity2.setTitle("小米手机");

        goodsEsRepository.saveAll(Arrays.asList(entity,entity1,entity2));

        System.out.println("批量新增成功");
    }

    @Test
    public void updateGoods(){
        GoodsEntity entity = new GoodsEntity();
        entity.setId(1L);
        entity.setBrand("苹果果");
        entity.setCategory("手机");
        entity.setImages("手机.jpg");
        entity.setPrice(1000D);
        entity.setTitle("苹果果手机");

        goodsEsRepository.save(entity);

        System.out.println("修改成功");
    }

    @Test
    public void deleteGoods(){
        GoodsEntity entity = new GoodsEntity();
        entity.setId(2L);
        goodsEsRepository.delete(entity);
        System.out.println("删除成功");
    }

    //查询所有
    @Test
    public void searchAll(){
        long count = goodsEsRepository.count();
        System.out.println(count);
        Iterable<GoodsEntity> all = goodsEsRepository.findAll();
        all.forEach(goods -> {
            System.out.println(goods);
        });


    }

    //自定义查询
    @Test
    public void customizeSearch(){
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        queryBuilder.withQuery(
                QueryBuilders.boolQuery()
                        .must(QueryBuilders.matchQuery("title","华为"))
                        .must(QueryBuilders.rangeQuery("price").gte(500).lte(1000))
        );
        System.out.println(queryBuilder);

        //排序
        queryBuilder.withSort(SortBuilders.fieldSort("price").order(SortOrder.DESC));

        //这是分页，当前页
        queryBuilder.withPageable(PageRequest.of(0,10));

        SearchHits<GoodsEntity> search = elasticsearchRestTemplate.search(queryBuilder.build(), GoodsEntity.class);

        System.out.println(search);

        search.getSearchHits().stream().forEach(hit -> {
            System.out.println(hit.getContent());
        });

    }

    @Test
    public void customizeSearchHighLight(){

        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //构建高亮查询
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        HighlightBuilder.Field title = new HighlightBuilder.Field("title");
        title.preTags("<span style='color:red'>");
        title.postTags("</span>");
        highlightBuilder.field(title);

        queryBuilder.withHighlightBuilder(highlightBuilder);

        System.out.println(queryBuilder);

        queryBuilder.withQuery(
                QueryBuilders.boolQuery()
                        .must(QueryBuilders.matchQuery("title","华为手机"))
                        .must(QueryBuilders.rangeQuery("price").gte(500).lte(1000))
        );

        queryBuilder.withSort(SortBuilders.fieldSort("price").order(SortOrder.DESC));
        queryBuilder.withPageable(PageRequest.of(0,2));

        SearchHits<GoodsEntity> search = elasticsearchRestTemplate.search(queryBuilder.build(), GoodsEntity.class);

        List<SearchHit<GoodsEntity>> searchHits = search.getSearchHits();

        //重新设置title
        List<SearchHit<GoodsEntity>> result = searchHits.stream().map(hit -> {
            Map<String, List<String>> highlightFields = hit.getHighlightFields();
            hit.getContent().setTitle(highlightFields.get("title").get(0));
            return hit;
        }).collect(Collectors.toList());
        System.out.println(result);

    }

    @Test
    public void searchAgg(){
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        queryBuilder.addAggregation(
                AggregationBuilders.terms("brand_agg").field("brand")
        );

        SearchHits<GoodsEntity> search = elasticsearchRestTemplate.search(queryBuilder.build(), GoodsEntity.class);

        Aggregations aggregations = search.getAggregations();

        //terms 是Aggregation的子类
        //Aggregation brand_agg = agregations.get("brand_agg");
        Terms terms = aggregations.get("brand_agg");

        List<? extends Terms.Bucket> buckets = terms.getBuckets();

        buckets.forEach(bucket -> {
            System.out.println(bucket.getKeyAsString()+":"+bucket.getDocCount());
        });

        System.out.println(search);
    }


    @Test
    public void searchAngMethod(){
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        queryBuilder.addAggregation(
                AggregationBuilders.terms("brand_agg").field("brand")
                    .subAggregation(AggregationBuilders.max("max_price").field("price"))
        );

        SearchHits<GoodsEntity> search = elasticsearchRestTemplate.search(queryBuilder.build(), GoodsEntity.class);

        Aggregations aggregations = search.getAggregations();

        Terms terms = aggregations.get("brand_agg");

        List<? extends Terms.Bucket> buckets = terms.getBuckets();

        buckets.forEach(bucket -> {
            System.out.println(bucket.getKeyAsString()+":"+bucket.getDocCount());

            //获取聚合
            Aggregations aggregations1 = bucket.getAggregations();
            //得到map
            Map<String, Aggregation> map = aggregations1.asMap();
            //需要强转,Aggregations是一个类Terms是他的子类,Aggregtion是有一个接口Max是他的子接口,而且Max是好几个接口的子接口.
            Max max_price = (Max)map.get("max_price");

            System.out.println(max_price.getValue());
        });
    }
}
