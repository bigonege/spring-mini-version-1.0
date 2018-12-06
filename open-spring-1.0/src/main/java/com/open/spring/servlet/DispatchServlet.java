package com.open.spring.servlet;


import com.open.action.mvc.DemoController;
import com.open.spring.annotation.Autowired;
import com.open.spring.annotation.Controller;
import com.open.spring.annotation.Service;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;


import java.lang.reflect.Field;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Auther: Wang Ky
 * @Date: 2018/12/5 15:43
 * @Description:
 */
public class DispatchServlet extends HttpServlet {
    Properties configContext = new Properties();
    private List<String> classNames = new ArrayList<String>();
    //Map beansMap = new HashMap<String,Object>();
    Map<String,Object> beansMap = new ConcurrentHashMap<String,Object>();
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("--------调用了post---------");
        DemoController demoController = (DemoController)beansMap.get("DemoController");
        demoController.getUsers();
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        //1，定位
        doLoadConfig(config.getInitParameter("contextConfigLocation"));
        //2，加载
        doScanner(configContext.getProperty("scanPackage"));
        //3，注册
        doRegistry();
        //4，自动注入
        doAutowried();


    }
    //2,根据包路径——扫描类——放到map容器里
    private void doScanner(String scanPackage) {
        URL url = this.getClass().getClassLoader().getResource("/"+scanPackage.replaceAll("\\.","/"));
        File files = new File(url.getFile());
        for (File file: files.listFiles() ) {
            if(file.isDirectory()){
                doScanner(scanPackage+"."+file.getName());
            }else {
                classNames.add(scanPackage+"."+file.getName().replace(".class",""));
            }
        }

    }
    private void doRegistry() {

        if(classNames == null) return;

        for (String clazz :classNames ) {
            try {
                Class<?> aClass = Class.forName(clazz);
                if(aClass.isAnnotationPresent(Controller.class)){
                    beansMap.put(aClass.getSimpleName(),aClass.newInstance());
                }else if(aClass.isAnnotationPresent(Service.class)){

                    Service service = aClass.getAnnotation(Service.class);
                    String beanName =service.value();
                    if("".equals(beanName.trim())){
                        beansMap.put(aClass.getSimpleName(),aClass.newInstance());
                    }
                    Class<?>[] interfaces = aClass.getInterfaces();
                    for (Class<?> s: interfaces)
                    {
                        beansMap.put(s.getName(),aClass.newInstance());
                    }

                }else{
                    continue;
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }

    }
    private void doAutowried() {
        if(beansMap.isEmpty()){ return; }

        for (Map.Entry<String,Object> entry : beansMap.entrySet()) {

            Field[] fields = entry.getValue().getClass().getDeclaredFields();

            for (Field field : fields) {

                if (!field.isAnnotationPresent(Autowired.class)) {
                    continue;
                }

                Autowired autowried = field.getAnnotation(Autowired.class);

                String beanName = autowried.value().trim();
                if ("".equals(beanName)) {
                    beanName = field.getType().getName();
                }
                field.setAccessible(true);

                try {
                    field.set(entry.getValue(), beansMap.get(beanName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

            }
        }
    }



    private void doLoadConfig(String location) {
        InputStream resourceAsStream = DispatchServlet.class.getClassLoader().getResourceAsStream(location.replace("classpath:",""));
        try {
            configContext.load(resourceAsStream);
            System.out.println(configContext.getProperty("scanPackage"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
