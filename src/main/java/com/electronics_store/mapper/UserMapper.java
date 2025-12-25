package com.electronics_store.mapper;

import com.electronics_store.dto.user.UserDtoRegister;
import com.electronics_store.model.User;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

@Component
public class UserMapper {

    public User toEntity(UserDtoRegister userDTORegister){
        User user = new User();
        user.setUsername(userDTORegister.getUsername());
        user.setFullname(userDTORegister.getFullname());
        user.setPassword(userDTORegister.getPassword());
        return user;
    }
}
