package com.baidu.shop.response;

import com.baidu.shop.base.Result;
import com.baidu.shop.document.GoodsDoc;
import com.baidu.shop.entity.BrandEntity;
import com.baidu.shop.entity.CategoryEntity;
import com.baidu.shop.status.HTTPStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @ClassName GoodsResponse
 * @Description: TODO
 * @Author shenyaqi
 * @Date 2020/9/21
 * @Version V1.0
 **/
@Data
@NoArgsConstructor
public class GoodsResponse extends Result<List<GoodsDoc>> {
    private Long total;

    private Long totalPage;

    private List<BrandEntity> brandEntityList;

    private List<CategoryEntity> categoryEntityList;

    public GoodsResponse(Long total,Long totalPage,List<BrandEntity> brandEntityList,List<CategoryEntity> categoryEntityList
            ,List<GoodsDoc> goodsDocs){

        super(HTTPStatus.OK,HTTPStatus.OK + "",goodsDocs);
        this.total = total;
        this.totalPage = totalPage;
        this.brandEntityList = brandEntityList;
        this.categoryEntityList = categoryEntityList;
    }
}
