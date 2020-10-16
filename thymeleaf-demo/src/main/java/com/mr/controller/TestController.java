package com.mr.controller;

import com.mr.pojo.StudentPojo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Arrays;

/**
 * @ClassName TestController
 * @Description: TODO
 * @Author shenyaqi
 * @Date 2020/9/14
 * @Version V1.0
 **/
@Controller
public class TestController {

    @GetMapping("test")
    public String test(ModelMap map){
        map.put("name","tomcat");
        return "test";
    }

    @GetMapping("stu")
    public String student(ModelMap map){
        StudentPojo student = new StudentPojo();
        student.setCode("007");
        student.setPass("1258");
        student.setAge(24);
        student.setLikeColor("<font color='red'>红色</font>");
        map.put("stu",student);
        return "student";
    }

    @GetMapping("list")
    public String stuList(ModelMap map){
        StudentPojo s1 = new StudentPojo("001","111",18,"red");
        StudentPojo s2 = new StudentPojo("002","222",19,"red");
        StudentPojo s3 = new StudentPojo("003","333",16,"blue");
        StudentPojo s4 = new StudentPojo("004","444",28,"blue");
        StudentPojo s5 = new StudentPojo("005","555",68,"blue");
        StudentPojo s6 = new StudentPojo("006","555",68,"blue");
        StudentPojo s7 = new StudentPojo("007","555",68,"blue");

        map.put("stuList",Arrays.asList(s1,s2,s3,s4,s5,s6,s7));
        return "list";
    }
}
