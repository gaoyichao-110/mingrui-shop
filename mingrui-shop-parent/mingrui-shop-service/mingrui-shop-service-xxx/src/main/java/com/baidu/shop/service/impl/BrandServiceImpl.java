package com.baidu.shop.service.impl;

import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.BrandDTO;
import com.baidu.shop.entity.BrandEntity;
import com.baidu.shop.entity.CategoryBrandEntity;
import com.baidu.shop.entity.SkuEntity;
import com.baidu.shop.entity.SpuEntity;
import com.baidu.shop.mapper.BrandMapper;
import com.baidu.shop.mapper.CategoryBrandMapper;
import com.baidu.shop.mapper.SkuEntityMapper;
import com.baidu.shop.mapper.SpuMapper;
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
import java.util.stream.Collectors;

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
    private SpuMapper spuMapper;
    @Resource
    private CategoryBrandMapper categoryBrandMapper;

    //这个查询是一次性查询出来所有的数据,压力太大
    @Override
    public Result<List<BrandEntity>> getCategoryAndBrand(Integer cid) {
            List<BrandEntity> list2 = brandMapper.getCategoryAndBrand(cid);
            return this.setResultSuccess(list2);

    }

    @Override
    public Result<List<BrandEntity>> getBrandByIdList(String brandList1) {

        String[] split = brandList1.split(",");
        List<String> strings = Arrays.asList(split);
        List<Integer> collect = strings.stream().map(brandMapper -> Integer.parseInt(brandList1)).collect(Collectors.toList());

        List<BrandEntity> list = brandMapper.selectByIdList(collect);

        return this.setResultSuccess(list);
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
        if(ObjectUtill.isNotNull(brandDTO.getId())) criteria.andEqualTo("id",brandDTO.getId());
        //条件查询
        if(StringUtil.isNotEmpty(brandDTO.getName())) criteria.andLike("name","%"+brandDTO.getName()+"%");

        if(ObjectUtill.isNotNull(brandDTO.getId())){
            criteria.andEqualTo("id",brandDTO.getId());
        }
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

        //调用批量新增方法
        this.categoryBrand(brandDTO,brandEntity);

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

        Example example = new Example(SpuEntity.class);
        example.createCriteria().andEqualTo("brandId",id);
        List<SpuEntity> list = spuMapper.selectByExample(example);

        if(list.size() > 0){
            return this.setResultError("该品牌下边有商品,不能被删除");
        }

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
            //通过split方法分割字符串的Array
            String[] split = brandDTO.getCategory().split(",");
            List<String> list = Arrays.asList(split);//使用Arrays.asList将Array转换为List,将传进来的数据放到List集合中

            List<CategoryBrandEntity> categoryBrandEntity = new ArrayList<>();//将关系表生成一个集合

            list.stream().forEach(cid -> {//使用JDK1.8的stream,遍历list
                CategoryBrandEntity categoryBrandEntity1 = new CategoryBrandEntity();//实例化关系表
                categoryBrandEntity1.setCategoryId(StringUtil.toInteger(cid));//cid里边包含了Category里边所有的id,所以是字符串类型的,将cid复制到关系表中,就是说将Category集合里边的id都放到了关系表
                categoryBrandEntity1.setBrandId(brandEntity.getId());//将品牌的id放到关系表中
                categoryBrandEntity.add(categoryBrandEntity1);//将赋值好的categoryBrandEntity1放到中间表的集合,最后批量新增这个中间表
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
