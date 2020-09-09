package com.baidu.shop.service.impl;

import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.BrandDTO;
import com.baidu.shop.entity.BrandEntity;
import com.baidu.shop.entity.CategoryBrandEntity;
import com.baidu.shop.mapper.BrandMapper;
import com.baidu.shop.mapper.CategoryBrandMapper;
import com.baidu.shop.service.BrandService;
import com.baidu.shop.utils.BaiduBeanUtil;
import com.baidu.shop.utils.ObjectUtill;
import com.baidu.shop.utils.PinyinUtil;
import com.baidu.shop.utils.StringUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.gson.JsonObject;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @ClassName BrandServiceImpl
 * @Description: TODO
 * @Author shenyaqi
 * @Date 2020/8/31
 * @Version V1.0
 **/
@RestController
public class BrandServiceImpl extends BaseApiService implements BrandService {
    @Resource
    private BrandMapper brandMapper;

    @Resource
    private CategoryBrandMapper categoryBrandMapper;

    @Override
    public Result<List<BrandEntity>> getCategoryAndBrand(Integer cid) {

            List<BrandEntity> list2 = brandMapper.getCategoryAndBrand(cid);
            return this.setResultSuccess(list2);



    }

    @Transactional
    @Override
    public Result<PageInfo<BrandEntity>> getBrandInfo(BrandDTO brandDTO) {

        //分页
        if(ObjectUtill.isNotNull(brandDTO.getPage()) && ObjectUtill.isNotNull(brandDTO.getRows())){
            PageHelper.startPage(brandDTO.getPage(),brandDTO.getRows());
        }

        //排序
        Example example = new Example(BrandEntity.class);
        if(StringUtil.isNotEmpty(brandDTO.getSort())) example.setOrderByClause(brandDTO.getOrderByClause());

        Example.Criteria criteria = example.createCriteria();
        if(ObjectUtill.isNotNull(brandDTO.getId()))
            criteria.andEqualTo("id",brandDTO.getId());
        //条件查询
        if(StringUtil.isNotEmpty(brandDTO.getName())) criteria.
                andLike("name","%"+brandDTO.getName()+"%");

        //查询
        List<BrandEntity> list = brandMapper.selectByExample(example);

        //数据封装
        PageInfo<BrandEntity> pageInfo = new PageInfo<>(list);

        return this.setResultSuccess(pageInfo);


    }

    @Transactional
    @Override
    public Result<JsonObject> save(BrandDTO brandDTO) {

        //java中的方法大小最多是一个屏幕的大小小
        //新增品牌并且可以返回组件
        BrandEntity brandEntity = BaiduBeanUtil.copyProperties(brandDTO, BrandEntity.class);

        //获取到品牌的名称
        //获取到品牌的第一个字
        //将第一个字转化为拼音
        //获取拼音的第一个首字母
        //统一转化为大写，加入到数据库中

//        String name = brandEntity.getName();
//        char c = name.charAt(0);
//        String s = PinyinUtil.getUpperCase(String.valueOf(c), PinyinUtil.TO_FIRST_CHAR_PINYIN);
//        brandEntity.setLetter(s.charAt(0));

        brandEntity.setLetter(PinyinUtil.getUpperCase(String.valueOf(brandEntity.getName().charAt(0)),
                PinyinUtil.TO_FIRST_CHAR_PINYIN).charAt(0));

        brandMapper.insertSelective(brandEntity);

        //批量新增
        categoryBrand(brandDTO,brandEntity);

        return this.setResultSuccess();
    }

    @Transactional
    @Override
    public Result<JsonObject> update(BrandDTO brandDTO) {

        BrandEntity brandEntity = BaiduBeanUtil.copyProperties(brandDTO, BrandEntity.class);

        brandEntity.setLetter(PinyinUtil.getUpperCase(String.valueOf
                (brandDTO.getName().charAt(0)),PinyinUtil.TO_FIRST_CHAR_PINYIN).charAt(0));
        //执行修改操作
        brandMapper.updateByPrimaryKeySelective(brandEntity);

        //通过brandDTO删除关系表中的数据,将表中原来的数据删除
        this.deleteCategoryAndBrand(brandDTO.getId());
            //再增加新的数据
        categoryBrand(brandDTO,brandEntity);

        return this.setResultSuccess();
    }

    @Transactional
    @Override
    public Result<JsonObject> delete(Integer id) {
        brandMapper.deleteByPrimaryKey(id);

        this.deleteCategoryAndBrand(id);

        return this.setResultSuccess();
    }



    //删除关系表中的数据
    private void deleteCategoryAndBrand(Integer id){
        Example example = new Example(CategoryBrandEntity.class);
        example.createCriteria().andEqualTo("brandId",id);
        categoryBrandMapper.deleteByExample(example);
    }

    //给关系表新增数据
    public void categoryBrand(BrandDTO brandDTO,BrandEntity brandEntity){
        //批量新增
        if(brandDTO.getCategory().contains(",")){

            String[] split = brandDTO.getCategory().split(",");
            List<String> list = Arrays.asList(split);

            List<CategoryBrandEntity> categoryBrandEntity = new ArrayList<>();

            list.stream().forEach(cid -> {
                CategoryBrandEntity categoryBrandEntity1 = new CategoryBrandEntity();
                categoryBrandEntity1.setCategoryId(StringUtil.toInteger(cid));
                categoryBrandEntity1.setBrandId(brandEntity.getId());
                categoryBrandEntity.add(categoryBrandEntity1);
            });

            //通过split方法分割字符串的Array
            //Arrays.asList将Array转换为List
            //使用JDK1,8的stream
            //使用map函数返回一个新的数据
            //collect 转换集合类型Stream<T>
            //Collectors.toList())将集合转换为List类型
//            List<CategoryBrandEntity> categoryBrandEntities = Arrays.asList(brandDTO.getCategory().split(","))
//                    .stream().map(cid -> {
//
//                        CategoryBrandEntity entity = new CategoryBrandEntity();
//                        entity.setCategoryId(StringUtil.toInteger(cid));
//                        entity.setBrandId(brandEntity.getId());
//
//                        return entity;
//                    }).collect(Collectors.toList());

            categoryBrandMapper.insertList(categoryBrandEntity);

        }else{
            CategoryBrandEntity categoryBrandEntity1 = new CategoryBrandEntity();

            categoryBrandEntity1.setCategoryId(StringUtil.toInteger(brandDTO.getCategory()));
            categoryBrandEntity1.setBrandId(brandEntity.getId());

            categoryBrandMapper.insertSelective(categoryBrandEntity1);

        }
    }
}
