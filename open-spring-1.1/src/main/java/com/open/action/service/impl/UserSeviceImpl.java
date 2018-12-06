package com.open.action.service.impl;

import com.open.action.model.User;
import com.open.action.service.IUserSevice;
import com.open.spring.annotation.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @Auther: Wang Ky
 * @Date: 2018/12/5 17:03
 * @Description:
 */
@Service
public class UserSeviceImpl implements IUserSevice {

    @Override
    public List<User> getUsers() {
        List<User> userLists = new ArrayList<User>();
        for (int i = 0; i <3 ; i++) {
            User user = new User();
            user.setUserId(i+1);
            user.setName("学生："+i+1);
            userLists.add(user);
        }
        return userLists;
    }
}
