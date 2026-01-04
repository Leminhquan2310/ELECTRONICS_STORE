package com.electronics_store.service;

import com.electronics_store.dto.user.UserDtoRegister;
import com.electronics_store.model.User;
import org.springframework.validation.BindingResult;

import java.util.List;

public interface UserService {
    User create(Object userDtoRegister);

    User getById(Long id);

    List<User> getAll();

    boolean delete(Long id);

    void checkUniqueUsernameRegister(UserDtoRegister userDTO, BindingResult bindingResult);

    User getCurrentUser();

    User findByEmail(String email);
}
