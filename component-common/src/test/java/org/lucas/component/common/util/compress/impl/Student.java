package org.lucas.component.common.util.compress.impl;

import java.io.Serializable;

/**
 * Created by joy on 17-2-8.
 */
public class Student implements Serializable {
    private String stuno;
    private String stuname;
    /**
     * 该元素不会进行jvm默认的序列化,也可以自己完成这个元素的序列化
     */
    private int stuage;

    public Student(String stuno, String stuname, int stuage) {
        super();
        this.stuno = stuno;
        this.stuname = stuname;
        this.stuage = stuage;
    }

    public String getStuno() {
        return stuno;
    }

    public void setStuno(String stuno) {
        this.stuno = stuno;
    }

    public String getStuname() {
        return stuname;
    }

    public void setStunae(String stuname) {
        this.stuname = stuname;
    }

    public int getStuage() {
        return stuage;
    }

    public void setStuage(int stuage) {
        this.stuage = stuage;
    }

    @Override
    public String toString() {
        return "Student{" +
                "stuno='" + stuno + '\'' +
                ", stuname='" + stuname + '\'' +
                ", stuage=" + stuage +
                '}';
    }


}
