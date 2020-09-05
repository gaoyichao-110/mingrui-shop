package com.baidu.shop.dto;

import com.baidu.shop.validate.group.MingruiOperation;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @ClassName SpecParamEntity
 * @Description: TODO
 * @Author shenyaqi
 * @Date 2020/9/3
 * @Version V1.0
 **/
//对属性的说明或者或者对数据操作更改
@ApiModel(value = "规格参数数据传输DTO")
@Data
public class SpecParamDTO {

    @ApiModelProperty(value = "主键",example = "1")
    @NotNull(message = "主键不能为空",groups = {MingruiOperation.Update.class})
    private Integer id;

    //用于方法、字段，表示对model属性的说明或者数据操作更改
    @ApiModelProperty(value = "商品分类Id",example = "1")
    private Integer cid;

    @ApiModelProperty(value = "规格组Id",example = "1")
    private Integer groupId;

    @ApiModelProperty(value="规格参数名称")
    private String name;

    @ApiModelProperty(value = "是否是数字类型参数，1=true，0=false",example = "false")
    @NotNull(message = "是否是数字类型参数不能为空",groups = {MingruiOperation.Update.class,MingruiOperation.Add.class})
    private Boolean numeric;

    @ApiModelProperty(value = "数字类型的单位，非数字类型为空")
    private String unit;

    @ApiModelProperty(value = "是否是sku通用属性，true或false",example = "false")
    @NotNull(message = "是否是sku通用属性",groups = {MingruiOperation.Update.class,MingruiOperation.Add.class})
    private Boolean generic;

    @ApiModelProperty(value = "是否用户搜索过滤",example = "false")
    @NotNull(message = "是否用户搜索过滤",groups = {MingruiOperation.Update.class,MingruiOperation.Add.class})
    private Boolean searching;

    @ApiModelProperty(value = "分段间隔值")
    private String segments;

}
