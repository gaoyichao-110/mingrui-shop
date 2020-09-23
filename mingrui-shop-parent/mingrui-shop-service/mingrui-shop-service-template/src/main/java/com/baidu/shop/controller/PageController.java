package com.baidu.shop.controller;

import com.baidu.shop.service.PageService;
import com.netflix.discovery.converters.Auto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @ClassName PageController
 * @Description: TODO
 * @Author shenyaqi
 * @Date 2020/9/23
 * @Version V1.0
 **/
@Controller
@RequestMapping(value = "item")
public class PageController {

    @Resource
    private PageService pageService;

    @GetMapping(value = "{spuId}.html")
    public String test(@PathVariable(value = "spuId") Integer spuId, ModelMap modelMap){

        Map<String,Object> map = pageService.getPageBySpuId(spuId);
        modelMap.putAll(map);
        return "item";
    }
}
