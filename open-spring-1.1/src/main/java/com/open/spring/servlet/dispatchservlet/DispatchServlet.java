package com.open.spring.servlet.dispatchservlet;

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
 * @Date: 2018/12/6 11:44
 * @Description:
 */
public class DispatchServlet extends HttpServlet {
    Properties properties = new Properties();
    List<String> classList = new ArrayList<String>();
    Map<String,Object> beansMap = new ConcurrentHashMap<String,Object>();
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("------invoke methed doPost() -------");
        DemoController demoController = (DemoController)beansMap.get("demoController");
        demoController.getUsers();
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        //1,定位
        doLocation(config.getInitParameter("contextLocation").replace("classpath:",""));
        //2,加载
        doScanner(properties.getProperty("scanPackage"));
        //3,注册
        doRegister();
        //4,自动注入
        doAutowired();


    }
    //1,定位,从配置文件中找到需要扫描的包（需要初始化为bean的包），进行初始化bean使用
    private void doLocation(String contextLocation) {
        //从classpath下面后去application.properties，里面有需要扫描的包路径
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(contextLocation.replace("classpath:", ""));
        try {
            //Reads a property list (key and element pairs) from the input byte stream
            properties.load(resourceAsStream);
            System.out.println(properties.getProperty("scanPackage"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //扫描bean所在的包，获取class并存到class容器中
    private void doScanner(String scanPackage) {
         // 1,根据扫描的包名进行class读取
        URL url = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.", "/"));
        File files = new File(url.getFile());
        for(File file : files.listFiles()){
            if (file.isDirectory()) {
                doScanner(scanPackage+"."+file.getName());
            }else {
                classList.add(scanPackage+"."+file.getName().replace(".class",""));
            }
        }

        // 2, 把扫描的class存到class容器中，以便注册时生成bean
    }
    //从class容器中获取class，然后初始化为bean，存到bean容器中
    private void doRegister() {
       if(classList == null){return;}
       for(String className : classList){
           try {
               Class<?> clazz = Class.forName(className);
               if (clazz.isAnnotationPresent(Controller.class)){
                   String beanName = lowerFrist(clazz.getSimpleName());
                   beansMap.put(beanName,clazz.newInstance());
               }else if (clazz.isAnnotationPresent(Service.class)){
                   String beanName = lowerFrist(clazz.getSimpleName());
                   beansMap.put(beanName,clazz.newInstance());

                   //如果是service，则也为它实现的接口赋值，赋值为serviceImpl的实例bean，用于注入的时候使用
                   Class<?>[] interfaces = clazz.getInterfaces();
                   for(Class<?> iservice : interfaces){
                        beansMap.put(iservice.getSimpleName(),clazz.newInstance());
                   }
               }else {
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
    //扫描bean容器中的field，如果加有@Autowired，则把bean容器中对应的值赋给该变量
    private void doAutowired() {
        if(beansMap == null){return;}

        for(Map.Entry<String,Object> entry:beansMap.entrySet()){
            Object value = entry.getValue();
            Field[] fields = value.getClass().getDeclaredFields();
            for(Field field :fields){
                Autowired annotation = field.getAnnotation(Autowired.class);
                if(annotation != null) {
                    field.setAccessible(true);
                    try {
                        field.set(entry.getValue(), beansMap.get(field.getType().getSimpleName()));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }

            }

        }

    }


    private String lowerFrist(String beanName) {
        char[] chars = beanName.toCharArray();
        chars[0] = (char) (chars[0]+32);
        return String.valueOf(chars);
    }








}
