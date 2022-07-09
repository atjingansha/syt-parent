package com.atguigu.yygh.testMongo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.regex.Matcher;

/**
 * @author WangJin
 * @create 2022-06-22 16:15
 */
@RestController
@RequestMapping("/mongo2")
public class TestMongo2 {

    @Autowired
    UserRepository userRepository;

    //添加
    @GetMapping("create")
    public void createUser() {
        User user = new User();
        user.setAge(20);
        user.setName("张三");
        user.setEmail("3332200@qq.com");
        User user1 = userRepository.save(user);

        System.out.println("user1 = " + user1);
    }

    //查询所有
    @GetMapping("findAll")
    public void findUser() {
        List<User> userList = userRepository.findAll();
        System.out.println(userList);
    }

    //条件查询
    @GetMapping("/condition")
    public void condition(){

        User user=new User();
        user.setAge(20);
        user.setName("张三");
        user.setEmail("3332200@qq.com");

        Example<User> example=Example.of(user);

        List<User> userList = userRepository.findAll(example);

        userList.forEach(System.out::println);
    }

    //模糊查询
    @GetMapping("/like")
    public void like(){

        User user=new User();
        user.setName("三");

        ExampleMatcher matcher=ExampleMatcher.matching()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase(true);

        Example<User> example=Example.of(user,matcher);

        List<User> userList = userRepository.findAll(example);

        userList.forEach(System.out::println);
    }


    //分页条件查询
    @GetMapping("/page")
    public void page(){

        User user=new User();
        user.setName("三");

        ExampleMatcher matcher=ExampleMatcher.matching()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase(true);

        Example<User> example=Example.of(user,matcher);

        Sort sort=Sort.by(Sort.Direction.DESC,"age");

        Pageable pageable= PageRequest.of(0,3,sort);

        Page<User> page = userRepository.findAll(example, pageable);

        System.out.println(page+","+page.getTotalElements());

    }

}
