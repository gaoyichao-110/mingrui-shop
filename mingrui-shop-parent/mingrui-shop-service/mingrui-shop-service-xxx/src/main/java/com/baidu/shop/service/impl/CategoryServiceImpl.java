package com.baidu.shop.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.entity.*;
import com.baidu.shop.mapper.*;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.service.CategoryService;

import com.baidu.shop.utils.ObjectUtill;
import tk.mybatis.mapper.entity.Example;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @ClassName CategoryServiceImpl
 * @Description: TODO
 * @Author shenyaqi
 * @Date 2020/8/27
 * @Version V1.0
 **/
@RestController
public class CategoryServiceImpl extends BaseApiService implements CategoryService {

    @Resource
    private CategoryMapper categoryMapper;

    @Resource
    private CategoryBrandMapper categoryBrandMapper;

    @Resource
    private SpecificationEntityMapper specificationEntityMapper;

    @Resource
    private SpuMapper spuMapper;

    //查询商品分类
    @Transactional
    @Override
    public Result<List<CategoryEntity>> getCategoryByPid(Integer pid) {

        CategoryEntity categoryEntity = new CategoryEntity();

        categoryEntity.setParentId(pid);

        List<CategoryEntity> list = categoryMapper.select(categoryEntity);

        return this.setResultSuccess(list);
    }

    @Transactional
    @Override
    public Result<JSONObject> saveCategorByPid(CategoryEntity entity) {

        //通过页面传来的parenid，查询parenid是否对应IsParent的数据Isparent==1，如果parentid！=1，就要把它修改成1
        CategoryEntity parentCateEntity  = new CategoryEntity();
        parentCateEntity.setId(entity.getParentId());
        parentCateEntity.setIsParent(1);
        categoryMapper.updateByPrimaryKeySelective(parentCateEntity);

        categoryMapper.insertSelective(entity);

        return this.setResultSuccess();
    }

    @Transactional
    @Override
    public Result<JSONObject> updateCategorBypid(CategoryEntity entity) {
        categoryMapper.updateByPrimaryKeySelective(entity);
        return this.setResultSuccess();
    }

    @Transactional
    @Override
    public Result<JSONObject> deleteCategorBypid(Integer id) {


        if(ObjectUtill.isNull(id)){
            return this.setResultSuccess("当前节点id不存在");
        }
        //去数据库查询传过来的id，如果这个id==null就是不存在，
        CategoryEntity categoryEntity = categoryMapper.selectByPrimaryKey(id);

        //查询出来父级节点==1的话就是就是父级节点，不能被删除
        if(categoryEntity.getParentId() == 1){
            return this.setResultError("当前节点为父级id，不能被删除");
        };

        Example example3 = new Example(SpuEntity.class);
        example3.createCriteria().andEqualTo("cid3",id);
        List<SpuEntity> list3 = spuMapper.selectByExample(example3);

        if(list3.size() > 0){
            return this.setResultError("该分类下边有商品,不能被删除");
        }

        //商品被品牌绑定,不能被删除
        Example example1 = new Example(CategoryBrandEntity.class);
        example1.createCriteria().andEqualTo("categoryId",id);
        List<CategoryBrandEntity> list1 = categoryBrandMapper.selectByExample(example1);

        if(list1.size() > 0){
                return this.setResultError("该商品分类被品牌绑定,不能被删除");
        }

        //商品被规格绑定,不能被删除
        Example example2 = new Example(SpecificationEntity.class);
        example2.createCriteria().andEqualTo("cid",id);
        List<SpecificationEntity> list2 = specificationEntityMapper.selectByExample(example2);

        if(list2.size() > 0){
            return this.setResultError("该商品分类有规格参数.不能被删除");
        }

        //查询商品的父节点,父节点不能被删除
        Example example = new Example(CategoryEntity.class);
        example.createCriteria().andEqualTo("partId",categoryEntity.getParentId());
        List<CategoryEntity> list = categoryMapper.selectByExample(example);

        //如果查询出来的数据只有一条，就将父级的id改为0
        if(list.size() == 1){//等于1的话，就将父节点的isParent的状态改为0
            CategoryEntity parentCateEntity = new CategoryEntity();
            parentCateEntity.setId(categoryEntity.getParentId());
            parentCateEntity.setIsParent(0);
            categoryMapper.updateByPrimaryKeySelective(parentCateEntity);

        }
        categoryMapper.deleteByPrimaryKey(id);


        return this.setResultSuccess();
    }

    @Override
    public Result<List<CategoryEntity>> getByBrand(Integer brandId) {
        try {
            List<CategoryEntity> list = categoryMapper.getCatesByBrand(brandId);
            return this.setResultSuccess(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }



}
