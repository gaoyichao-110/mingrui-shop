package com.baidu.shop.document;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @ClassName GoodsDoc
 * @Description: TODO
 * @Author shenyaqi
 * @Date 2020/9/16
 * @Version V1.0
 **/
@Document(indexName = "goods",shards = 1,replicas = 0)
@Data
public class GoodsDoc {

    private Long id;

    @Field(type = FieldType.Text,analyzer = "ik_max_word")
    private String titile;

    @Field(type = FieldType.Text,analyzer = "lk_max_word")
    private String brandName;

    @Field(type = FieldType.Text,analyzer = "lk_max_word")
    private String categoryName;

    @Field(type = FieldType.Text,analyzer = "lk_max_word")
    private String subTitle;

    private Long brandId;

    private Long cid1;

    private Long cid2;

    private Long cid3;

    private Date createTime;

    private List<Long> price;

    @Field(type = FieldType.Keyword,index = false)
    private String skus;

    private Map<String,Object> specs;
}
