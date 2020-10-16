package com.mr.pojo;

/**
 * @ClassName StudentPojo
 * @Description: TODO
 * @Author shenyaqi
 * @Date 2020/9/14
 * @Version V1.0
 **/
public class StudentPojo {
    private String code;

    private String pass;

    private int age;

    private String likeColor;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getLikeColor() {
        return likeColor;
    }

    public void setLikeColor(String likeColor) {
        this.likeColor = likeColor;
    }

    @Override
    public String toString() {
        return "StudentPojo{" +
                "code='" + code + '\'' +
                ", pass='" + pass + '\'' +
                ", age=" + age +
                ", likeColor='" + likeColor + '\'' +
                '}';
    }

    public StudentPojo(String code, String pass, int age, String likeColor) {
        this.code = code;
        this.pass = pass;
        this.age = age;
        this.likeColor = likeColor;
    }

    public StudentPojo() {
        
    }
}
