package com.atguigu.yygh.testMongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * @author WangJin
 * @create 2022-06-22 16:14
 */
@Repository
public interface UserRepository extends MongoRepository<User,String> {
}
