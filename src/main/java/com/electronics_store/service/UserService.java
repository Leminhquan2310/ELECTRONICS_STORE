package com.electronics_store.service;

import com.electronics_store.dto.user.UserDtoRegister;
import com.electronics_store.model.User;
import org.springframework.validation.BindingResult;

public interface UserService extends IGenerateService<User>{
    void checkUniqueUsernameRegister(UserDtoRegister userDTO, BindingResult bindingResult);
}
