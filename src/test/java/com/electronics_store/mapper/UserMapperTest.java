package com.electronics_store.mapper;

import com.electronics_store.dto.user.UserDtoRegister;
import com.electronics_store.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class UserMapperTest {

    @InjectMocks
    private UserMapper userMapper;

    private UserDtoRegister dto;

    @BeforeEach
    void setUp() {
        dto = new UserDtoRegister();
        dto.setUsername("minhquan");
        dto.setFullname("Minh Quan");
        dto.setPassword("123456");
    }

    @Test
    void toEntity_ShouldMapAllFieldsCorrectly() {


        // When
        User user = userMapper.toEntity(dto);

        // Then
        assertNotNull(user);
        assertEquals("minhquan", user.getUsername());
        assertEquals("Minh Quan", user.getFullname());
        assertEquals("123456", user.getPassword());
    }

    @Test
    void toEntity_ShouldReturnNewUserObject() {
        // When
        User user1 = userMapper.toEntity(dto);
        User user2 = userMapper.toEntity(dto);

        // Then
        // Đảm bảo mỗi lần gọi trả về object mới
        assertNotSame(user1, user2);
    }
}
