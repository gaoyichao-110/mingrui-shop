package com.baidu.shop.entity;

import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @ClassName SpecificationDTO
 * @Description: TODO
 * @Author shenyaqi
 * @Date 2020/9/3
 * @Version V1.0
 **/
@Table(name="tb_spec_group")
@Data
public class SpecificationEntity {

    @Id
    private Integer id;

    private Integer cid;

    private String name;
}
