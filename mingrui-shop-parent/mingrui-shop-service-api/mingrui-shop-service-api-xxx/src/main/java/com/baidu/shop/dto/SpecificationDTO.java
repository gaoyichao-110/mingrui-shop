package com.baidu.shop.dto;

import com.baidu.shop.base.BaseDTO;
import com.baidu.shop.entity.SpecParamEntity;
import com.baidu.shop.validate.group.MingruiOperation;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @ClassName SpecificationDTO
 * @Description: TODO
 * @Author shenyaqi
 * @Date 2020/9/3
 * @Version V1.0
 **/
@ApiModel(value = "规格组数据传输DTD")
@Data
public class SpecificationDTO extends BaseDTO {

    @ApiModelProperty(value = "主键",example = "1")
    @NotNull(message = "主键不能为空",groups={MingruiOperation.Update.class})
    private Integer id;

    @ApiModelProperty(value = "类型ID",example = "1")
    @NotNull(message = "类型id不能为空",groups = {MingruiOperation.Add.class})
    private Integer cid;

    @ApiModelProperty(value = "规格组名称")
    @NotEmpty(message = "规格组名称不能为空",groups={MingruiOperation.Add.class})
    private String name;

    @ApiModelProperty(value = "规格组里边的参数")
    @NotEmpty
    private List<SpecParamEntity>  specParams;
}
