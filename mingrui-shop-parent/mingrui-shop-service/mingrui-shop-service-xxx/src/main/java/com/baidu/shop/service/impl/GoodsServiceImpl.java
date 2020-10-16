package com.baidu.shop.service.impl;

import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.BrandDTO;
import com.baidu.shop.dto.SkuDTO;
import com.baidu.shop.dto.SpuDTO;
import com.baidu.shop.entity.*;
import com.baidu.shop.mapper.*;
import com.baidu.shop.service.BrandService;
import com.baidu.shop.service.GoodsService;
import com.baidu.shop.status.HTTPStatus;
import com.baidu.shop.utils.BaiduBeanUtil;
import com.baidu.shop.utils.ObjectUtill;
import com.baidu.shop.utils.StringUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName GoodsServiceImpl
 * @Description: TODO
 * @Author shenyaqi
 * @Date 2020/9/7
 * @Version V1.0
 **/
@RestController
public class GoodsServiceImpl extends BaseApiService implements GoodsService {

    @Resource
    private SpuMapper spuMapper;

    @Autowired
    private BrandService brandService;

    @Resource
    private CategoryMapper categoryMapper;

    @Resource
    private StockEntityMapper stockEntityMapper;

    @Resource
    private SpuDetailEntityMapper spuDetailEntityMapper;

    @Resource
    private SkuEntityMapper skuEntityMapper;


    //商品新增,各种信息,各种规格
    @Transactional
    @Override
    public Result<JsonObject> saveSpu(SpuDTO spuDTO) {
        Date date = new Date();

        //把DTO转换成class类型,因为DTO只是接参传参
        SpuEntity spuEntity = BaiduBeanUtil.copyProperties(spuDTO, SpuEntity.class);
        spuEntity.setSaleable(1);//spu中的属性,必须手动赋值
        spuEntity.setValid(1);
        spuEntity.setCreateTime(date);
        spuEntity.setLastUpdateTime(date);
        //新增spu
        spuMapper.insertSelective(spuEntity);
        //获得了刚刚新增的spu数据的spuId
        Integer spuId = spuEntity.getId();

        //新增spuDetail
        //把DTO转换成class类型,因为DTO只是接参传参
        SpuDetailEntity spuDetailEntity = BaiduBeanUtil.copyProperties(spuDTO.getSpuDetail(), SpuDetailEntity.class);
        spuDetailEntity.setSpuId(spuId);//将刚刚新增过的spuId赋值给spuDetail中的spuId,是它们链接在一起
        spuDetailEntityMapper.insertSelective(spuDetailEntity);//然后新增spuDetail

        this.saveSkuStock(spuDTO.getSkus(),spuDTO.getId(),date);

        return this.setResultSuccess();
    }

    //商品信息查询spu,用于修改回显
    @Override
    public Result<SpuDetailEntity> getSpuDetailBydSpu(Integer spuId) {

        SpuDetailEntity spuDetailEntity = spuDetailEntityMapper.selectByPrimaryKey(spuId);

        return this.setResultSuccess(spuDetailEntity);
    }
    //商品信息查询sku,用于修改回显
    @Override
    public Result<List<SkuDTO>> getSkuBySpuID(Integer spuId) {
        List<SkuDTO> list =  skuEntityMapper.getSkuBySpuID(spuId);
        return this.setResultSuccess(list);
    }

    //商品信息修改
    @Transactional
    @Override
    public Result<JsonObject> editSpu(SpuDTO spuDTO) {
        Date date = new Date();
        //把DTO转换成class类型,因为DTO只是接参传参
        SpuEntity spuEntity = BaiduBeanUtil.copyProperties(spuDTO,SpuEntity.class);
        spuEntity.setLastUpdateTime(date);
        spuMapper.updateByPrimaryKeySelective(spuEntity);

        //修改spuDetail
        SpuDetailEntity spuDetailEntity = BaiduBeanUtil.copyProperties(spuDTO.getSpuDetail(), SpuDetailEntity.class);
        spuDetailEntityMapper.updateByPrimaryKeySelective(spuDetailEntity);


        //修改Sku 修改sku是将原来的sku和stock给删除了,再新增新的sku和stock
        // 调用了listSku方法,返回了一个Long类型的list集合
        List<Long> listSku = this.listSku(spuDTO.getId());

        //将传过来的skuId给删了
        skuEntityMapper.deleteByIdList(listSku);
        stockEntityMapper.deleteByIdList(listSku);//因为stock和sku绑定,所以删除skuId.

        //这里放的是新的sku数据,把这个sku数据放到调用的saveSkuStock方法里.
        List<SkuDTO> skus = spuDTO.getSkus();

        //方法中定义的参数,接收的时候参数需要赋值
        this.saveSkuStock(spuDTO.getSkus(),spuDTO.getId(),date);

        return this.setResultSuccess();
    }
    //商品删除
    @Transactional
    @Override
    public Result<JsonObject> delete(Integer spuId) {
        spuMapper.deleteByPrimaryKey(spuId);
        spuDetailEntityMapper.deleteByExample(spuId);
        //查询出来skuId
        List<Long> longs = this.listSku(spuId);
        //判断一下,skuId有数据的话就删除,没有就不删除,防止所有数据被删除
        if(longs.size() > 0){
            skuEntityMapper.deleteByIdList(longs);
            stockEntityMapper.deleteByIdList(longs);
        }
        return this.setResultSuccess();
    }



    //查询出sku的数据
    private List<Long> listSku(Integer spuId){
        //构建条件查询
        Example example = new Example(SkuEntity.class);
        example.createCriteria().andEqualTo("spuId",spuId);//根据spuId查询出来当前spuId相等的sku的一条数据,就是通过spuId查询出来了和当前传入的spuId相对应的sku数据
        List<SkuEntity> skuEntities = skuEntityMapper.selectByExample(example);
        //查询出来一条的sku数据,将这个因为sku是一个集合,将sku数据遍历,找到skuId,将skuId作为一个Long类型的List集合返回回去
        return skuEntities.stream().map(sku -> sku.getId()).collect(Collectors.toList());
    }

    //新增sku和stock
    private void saveSkuStock(List<SkuDTO> skus,Integer spuId,Date date){
            //遍历sku这个属性,这个属性是一个list集合,需要把这个集合里边的属性遍历出来才可以新增
            skus.stream().forEach(skuDTO ->{
            //新增sku
            SkuEntity skuEntity = BaiduBeanUtil.copyProperties(skuDTO, SkuEntity.class);
            skuEntity.setSpuId(spuId);//spu的主键
            skuEntity.setCreateTime(date);//添加时间
            skuEntity.setLastUpdateTime(date);//最后修改时间
            skuEntityMapper.insertSelective(skuEntity);

            //新增stock 因为它和spu没有联系,所以不用DTO传参,直接new对象就行,只要把skuid放入它里边的skuId主键让两个联系起来就行
            StockEntity stockEntity = new StockEntity();
            stockEntity.setSkuId(skuEntity.getId());
            stockEntity.setStock(skuDTO.getStock());//将sku里边的库存放到stock里边
            stockEntityMapper.insertSelective(stockEntity);

        });
    }

    @Override
    public Result<List<SpuDTO>> getSpuInfo(SpuDTO spuDTO) {

        //分页判断page和rows是否为空
        if(ObjectUtill.isNotNull((spuDTO.getPage())) && ObjectUtill.isNotNull(spuDTO.getRows())) PageHelper.startPage(spuDTO.getPage(),spuDTO.getRows());

        List<SpuEntity> list = collect2(spuDTO);

        List<SpuDTO> collect1 = list.stream().map(spuEntity -> {
            SpuDTO spuDTO1 = BaiduBeanUtil.copyProperties(spuEntity, SpuDTO.class);

            //设置品牌名称
            BrandDTO brandDTO = new BrandDTO();
            brandDTO.setId(spuEntity.getBrandId());
            //获取一下品牌的信息
            Result<PageInfo<BrandEntity>> brandInfo = brandService.getBrandInfo(brandDTO);
            if (ObjectUtill.isNotNull(brandInfo)) {
                PageInfo<BrandEntity> data = brandInfo.getData();
                List<BrandEntity> dataList = data.getList();
                if (!dataList.isEmpty() && dataList.size() == 1) {
                    spuDTO1.setBrandName(dataList.get(0).getName());
                }
            }

            //设置分类 通过cid1 cid2 cid3
            List<CategoryEntity> categoryEntities = categoryMapper.selectByIdList(Arrays.asList(spuDTO1.getCid1(), spuDTO1.getCid2(), spuDTO1.getCid3()));
            String collect = categoryEntities.stream().map(category -> category.getName()).collect(Collectors.joining("/"));

            spuDTO1.setCategoryName(collect);

            return spuDTO1;

        }).collect(Collectors.toList());

        PageInfo<SpuEntity> info = new PageInfo<>(list);

        return this.setResult(HTTPStatus.OK,info.getTotal()+"",collect1);
    }


    private List<SpuEntity> collect2(SpuDTO spuDTO){

        //构建条件查询
        Example example = new Example(SpuEntity.class);

        //查询条件,判断标题不能为空,根据标题查询
        Example.Criteria criteria = example.createCriteria();
        if(StringUtil.isNotEmpty(spuDTO.getTitle())){
            criteria.andLike("title","%"+ spuDTO.getTitle() +"%");
        }
        //查询出上架商品
        if(ObjectUtill.isNotNull(spuDTO.getSaleable()) && spuDTO.getSaleable() != 2){
            criteria.andEqualTo("saleable",spuDTO.getSaleable());
        }

        if(ObjectUtill.isNotNull(spuDTO.getId())){
            criteria.andEqualTo("id",spuDTO.getId());
        }

        //排序
        if(ObjectUtill.isNotNull(spuDTO.getSort())){
            example.setOrderByClause(spuDTO.getOrderByClause());
        }

        List<SpuEntity> list = spuMapper.selectByExample(example);

        return list;
    }


    @Transactional
    @Override
    public Result<JsonObject> updateItems(SpuDTO spuDTO) {
        SpuEntity spuEntity = BaiduBeanUtil.copyProperties(spuDTO, SpuEntity.class);
        //将传进来的id传到spuEntityid里,然后spuEntity执行的数据就是传入id的那条数据
        spuEntity.setId(spuDTO.getId());
        //如果传进来的saleable==1就将它修改为0,不等于1就修改为1
        if(spuEntity.getSaleable() == 1){
            spuEntity.setSaleable(0);
            spuMapper.updateByPrimaryKeySelective(spuEntity);
            return this.setResultSuccess("商品下架成功");
        }else{
            spuEntity.setSaleable(1);
            spuMapper.updateByPrimaryKeySelective(spuEntity);
            return this.setResultSuccess("商品上架成功");
        }
    }

}
