package com.open.action.model;

/**
 * @Auther: Wang Ky
 * @Date: 2018/12/5 17:02
 * @Description:
 */
public class User {
    Integer userId;
    String name;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", name='" + name + '\'' +
                '}';
    }
}
