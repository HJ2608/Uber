package com.firstapp.uber.user;

import model.User;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class UserRepo {

    private final JdbcClient jdbc;

    public UserRepo(JdbcClient jdbcClient){
        this.jdbc = jdbcClient;
    }
    public List<User> findAll(){
        return jdbc.sql("SELECT * FROM users")
                .query(User.class)
                .list();
    }
    public Optional<User> findById(Long id){
        return jdbc.sql("SELECT * FROM users WHERE user_id = :id")
                .param("id",id)
                .query(User.class)
                .optional();
    }
    public boolean create(User user){
        jdbc.sql("""
                    INSERT INTO users (first_name, last_name, mobile_num, email,password_hash) 
                    VALUES (:first_name, :last_name, :mobile_num, :email, :password_hash) 
                    """)
                .params(Map.of(
                        "first_name", user.first_name(),
                        "last_name", user.last_name(),
                        "mobile_num", user.mobile_num(),
                        "email", user.email(),
                        "password_hash", user.password_hash()
                ))
                .update();

        return true;
    }
    public boolean update(User user){
        jdbc.sql("""
                    UPDATE users 
                    SET(first_name, last_name,mobile_num,email,password_hash) = (:first_name, :last_name, :mobile_num, :email, :password_hash)
                    WHERE user_id = :id
                    """)
                .params(Map.of(
                        "first_name",user.first_name(),
                        "last_name",user.last_name(),
                        "mobile_num",user.mobile_num(),
                        "email",user.email(),
                        "id",user.id(),
                        "password_hash",user.password_hash()
                ))
                .update();
        return true;
    }

    public boolean delete(Integer id){
        jdbc.sql("DELETE FROM users WHERE user_id = :id")
                .param("id",id)
                .update();
        return true;
    }

    public Optional<User> findByMobile(String mobile) {
        return jdbc.sql("SELECT * FROM users WHERE mobile_num = :mobile")
                .param("mobile", mobile)
                .query(User.class)
                .optional();
    }

    public Optional<User> findByEmail(String email) {
        return jdbc.sql("SELECT * FROM users WHERE email = :email")
                .param("email", email)
                .query(User.class)
                .optional();
    }

}
