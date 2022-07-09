package com.atguigu.yygh.testMongo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.regex.Pattern;

/**
 * @author WangJin
 * @create 2022-06-22 15:30
 */
@RestController
@RequestMapping("/mongo")
public class TestMongo {

    @Autowired
    MongoTemplate mongoTemplate;

    @GetMapping("/create")
    public void create(){

        User user=new User();
        user.setAge(20);
        user.setName("test");
        user.setEmail("4932200@qq.com");
        System.out.println("mongoTemplate.insert(user) = " + mongoTemplate.insert(user));
    }


    @GetMapping("/findAll")
    public void findAll(){
        System.out.println("mongoTemplate.findAll(User.class) = " + mongoTemplate.findAll(User.class));
    }

    @GetMapping("/findById")
    public void findById(){
        User user = mongoTemplate.findById("62b2b8acbb2ca64478d2979d", User.class);

        System.out.println("user = " + user);
    }

    @GetMapping("/findUser")
    public void findUser(){
        Query  query=new Query(
               Criteria.where("name").is("李四").and("age").is(33)

        );
        List<User> users = mongoTemplate.find(query, User.class);
        System.out.println("users = " + users);
    }


    @GetMapping("/like")
    public void like(){
        String name = "est";
        String regex = String.format("%s%s%s", "^.*", name, ".*$");
        Pattern pattern=Pattern.compile(regex,Pattern.CASE_INSENSITIVE);
        Query query=new Query(
                Criteria.where("name").regex(pattern)
        );
        System.out.println(mongoTemplate.find(query, User.class));
    }
}
