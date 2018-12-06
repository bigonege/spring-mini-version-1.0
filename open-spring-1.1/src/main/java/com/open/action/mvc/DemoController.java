package com.open.action.mvc;

import com.open.action.model.User;
import com.open.action.service.IUserSevice;
import com.open.spring.annotation.Autowired;
import com.open.spring.annotation.Controller;
import com.open.spring.annotation.RequestMapping;

import java.util.List;

/**
 * @Auther: Wang Ky
 * @Date: 2018/12/5 16:14
 * @Description:
 */
@Controller
@RequestMapping("/demo")
public class DemoController {

    @Autowired
    IUserSevice userSevice;

    @RequestMapping("/getUser")
    public void getUsers(){
        List<User> users = userSevice.getUsers();
        for (User user: users ) {
            System.out.println("open-spring-1.1"+user.toString());
        }
    }

}
