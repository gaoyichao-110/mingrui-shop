package com.baidu.shop.feign;

import com.baidu.shop.service.TemplateService;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @ClassName TemplateFeign
 * @Description: TODO
 * @Author shenyaqi
 * @Date 2020/9/29
 * @Version V1.0
 **/
@FeignClient(value = "template-server",contextId = "")
public interface TemplateFeign extends TemplateService {
}
