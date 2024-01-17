package com.sarabrandserver.user.repository;

import com.sarabrandserver.AbstractRepositoryTest;
import com.sarabrandserver.enumeration.RoleEnum;
import com.sarabrandserver.user.entity.ClientRole;
import com.sarabrandserver.user.entity.SarreBrandUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Transactional
class RoleServiceImplTest extends AbstractRepositoryTest {

    @Autowired
    private RoleServiceImpl service;
    @Autowired
    private UserServiceImpl userService;

    @Test
    @DisplayName("test saving a role and finding it by id")
    void save() {
        assertDoesNotThrow(() -> {

            var user = userService
                    .save(
                            SarreBrandUser.builder()
                                    .firstname("test")
                                    .lastname("lastname")
                                    .email("test@email.com")
                                    .phoneNumber("0000000000")
                                    .password("password123")
                                    .enabled(true)
                                    .clientRole(new HashSet<>())
                                    .build()
                    );

            ClientRole save = service.save(RoleEnum.CLIENT, user);

            assertEquals(1L, save.getRoleId());
        });
    }

}