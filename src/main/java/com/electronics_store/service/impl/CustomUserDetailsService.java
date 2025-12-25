package com.electronics_store.service.impl;

import com.electronics_store.dto.user.UserDtoRegister;
import com.electronics_store.exception.RoleNotFoundException;
import com.electronics_store.mapper.UserMapper;
import com.electronics_store.model.Role;
import com.electronics_store.model.User;
import com.electronics_store.repository.RoleRepository;
import com.electronics_store.repository.UserRepository;
import com.electronics_store.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService, UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    private UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return (UserDetails) userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("Username not found"));
    }

    @Override
    @Transactional
    public User create(Object userDtoRegister) {
        User user = userMapper.toEntity((UserDtoRegister) userDtoRegister);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getRoles().isEmpty()) {
            Role role = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new RoleNotFoundException("ROLE_USER not found"));
            user.setRoles(Collections.singleton(role));
        }
        return userRepository.save(user);
    }

    @Override
    public List<User> createBatch(List<User> entities) {
        return List.of();
    }

    @Override
    public User getById(Long id) {
        return new User();
    }

    @Override
    public List<User> getAll() {
        return List.of();
    }

    @Override
    public User update(Object user) {
        return null;
    }

    @Override
    public boolean delete(Long id) {
        return false;
    }

    @Override
    public void checkUniqueUsernameRegister(UserDtoRegister userDTO, BindingResult bindingResult) {
        if (!userDTO.getUsername().isEmpty() && userRepository.existsByUsername(userDTO.getUsername()))
            bindingResult.rejectValue("username", "username.exists", "Username already existed");
    }
}
