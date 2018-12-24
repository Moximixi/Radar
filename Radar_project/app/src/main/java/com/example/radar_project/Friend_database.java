package com.example.radar_project;

import org.litepal.crud.DataSupport;

/**
 * 定义数据表中的字段
 */
public class Friend_database extends DataSupport {
    private int id;
    private String name;
    private String num;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }
}
