package com.mr.repository;

import com.mr.entity.GoodsEntity;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface GoodsEsRepository extends ElasticsearchRepository<GoodsEntity,Long> {

    List<GoodsEntity> findAllByAndTitle(String title);

    List<GoodsEntity> findByAndPriceBetween(Double start,Double end);

    List<GoodsEntity> findByPriceBetween(Double start,Double end);


}
