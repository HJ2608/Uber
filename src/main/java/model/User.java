package model;


public record User(
        Integer id,
        String first_name ,
        String last_name ,
        String mobile_num ,
        String email,
        String password_hash
) {
}
