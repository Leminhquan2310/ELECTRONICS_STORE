package com.electronics_store.service;

import com.electronics_store.dto.user.UpdateProfileRequest;
import com.electronics_store.dto.user.UserDtoRegister;
import com.electronics_store.dto.user.UserProfileResponse;
import com.electronics_store.model.User;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {
    User create(Object userDtoRegister);

    User getById(Long id);

    List<User> getAll();

    boolean delete(Long id);

    void checkUniqueUsernameRegister(UserDtoRegister userDTO, BindingResult bindingResult);

    User getCurrentUser();

    UserProfileResponse getProfile(String username);

    User findByEmail(String email);

    void updateUserProfile(String username, UpdateProfileRequest dto);

    void updateAvatar(String username, MultipartFile file);

    void changePassword(String username, String currentPassword, String newPassword);
}
