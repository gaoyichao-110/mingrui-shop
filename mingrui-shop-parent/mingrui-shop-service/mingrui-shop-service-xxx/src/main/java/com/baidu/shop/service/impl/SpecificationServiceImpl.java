package com.baidu.shop.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.SpecParamDTO;
import com.baidu.shop.dto.SpecificationDTO;
import com.baidu.shop.entity.SpecParamEntity;
import com.baidu.shop.entity.SpecificationEntity;
import com.baidu.shop.mapper.SpecParamMapper;
import com.baidu.shop.mapper.SpecificationEntityMapper;
import com.baidu.shop.service.SpecificationService;
import com.baidu.shop.utils.BaiduBeanUtil;
import com.baidu.shop.utils.ObjectUtill;
import com.baidu.shop.utils.StringUtil;
import javafx.beans.binding.ObjectExpression;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.List;

/**
 * @ClassName SpecificationServiceImpl
 * @Description: TODO
 * @Author shenyaqi
 * @Date 2020/9/3
 * @Version V1.0
 **/
@RestController
public class SpecificationServiceImpl extends BaseApiService implements SpecificationService{

    @Resource
    SpecificationEntityMapper specificationEntityMapper;

    @Resource
    SpecParamMapper specParamMapper;


    //规格查询
    @Transactional
    @Override
    public Result<List<SpecificationEntity>> listSpecification(SpecificationDTO specificationDTO) {

        Example example = new Example(SpecificationEntity.class);

        //通过分类id查询数据
        if(ObjectUtill.isNotNull(specificationDTO.getCid())) example.createCriteria().andEqualTo("cid",specificationDTO.getCid());

        List<SpecificationEntity> list = specificationEntityMapper.selectByExample(example);

        return this.setResultSuccess(list);
    }

    //规格新增
    @Transactional
    @Override
    public Result<JSONObject> save(SpecificationDTO specificationDTO) {
        specificationEntityMapper.insertSelective(BaiduBeanUtil.copyProperties(specificationDTO,SpecificationEntity.class));

        return this.setResultSuccess();
    }

    //规格修改
    @Transactional
    @Override
    public Result<JSONObject> update(SpecificationDTO specificationDTO) {

        specificationEntityMapper.updateByPrimaryKeySelective(BaiduBeanUtil.copyProperties(specificationDTO,SpecificationEntity.class));

        return this.setResultSuccess();
    }

    //规格删除
    @Transactional
    @Override
    public Result<JSONObject> delete(Integer id) {

        Example example = new Example(SpecParamEntity.class);
        example.createCriteria().andEqualTo("groupId",id);
        List<SpecParamEntity> list = specParamMapper.selectByExample(example);

        if(list.size() > 0) return this.setResultError("该规格里边有参数,不能被删除");
        
        specificationEntityMapper.deleteByPrimaryKey(id);
        return this.setResultSuccess();
    }

    //规格参数查询
    @Transactional
    @Override
    public Result<List<SpecParamEntity>> listSpecParam(SpecParamDTO specParamDTO) {

       // if(ObjectUtill.isNull(specParamDTO.getGroupId())) {return this.setResultSuccess("规格组id不能为空");};
        Example example = new Example(SpecParamEntity.class);
        Example.Criteria criteria = example.createCriteria();

        if(ObjectUtill.isNotNull(specParamDTO.getGroupId())){
            criteria.andEqualTo("groupId",specParamDTO.getGroupId());
        }

        if(ObjectUtill.isNotNull(specParamDTO.getCid())){
            criteria.andEqualTo("cid",specParamDTO.getCid());
        }

        if(ObjectUtill.isNotNull(specParamDTO.getSearching())){
            criteria.andEqualTo("searching",specParamDTO.getSearching());
        }


        List<SpecParamEntity> list = specParamMapper.selectByExample(example);

        return this.setResultSuccess(list);
    }

    //规格参数新增
    @Transactional
    @Override
    public Result<JSONObject> save(SpecParamDTO specParamDTO) {
        specParamMapper.insertSelective(BaiduBeanUtil.copyProperties(specParamDTO,SpecParamEntity.class));
        return this.setResultSuccess();
    }

    //规格参数修改
    @Transactional
    @Override
    public Result<JSONObject> update(SpecParamDTO specParamDTO) {

        specParamMapper.updateByPrimaryKeySelective(BaiduBeanUtil.copyProperties(specParamDTO,SpecParamEntity.class));

        return this.setResultSuccess();
    }

    //规格参数删除
    @Transactional
    @Override
    public Result<JSONObject> specDelete(Integer id) {
        specParamMapper.deleteByPrimaryKey(id);
        return this.setResultSuccess();
    }


}
