package com.sarabrandserver.user.repository;

import com.sarabrandserver.AbstractRepositoryTest;
import com.sarabrandserver.user.entity.SarreBrandUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
class UserServiceImplTest extends AbstractRepositoryTest {

    @Autowired
    private UserServiceImpl userService;

    @Test
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

            assertEquals("lastname", user.getLastname());
            assertEquals("test@email.com", user.getEmail());
            assertTrue(user.isEnabled());
        });
    }

    @Test
    void doesPrincipalExist() {
        // given
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

        // when
        assertTrue(userService.doesPrincipalExist(user.getEmail()));
        assertFalse(userService.doesPrincipalExist("frank"));
    }

    @Test
    void allUsers() {
        // given
        for (int i = 0; i < 10; i++) {
            userService
                    .save(
                            SarreBrandUser.builder()
                                    .firstname("test")
                                    .lastname("lastname")
                                    .email("test@email.com" + i)
                                    .phoneNumber("0000000000")
                                    .password("password123")
                                    .enabled(true)
                                    .clientRole(new HashSet<>())
                                    .build()
                    );
        }

        // when
        Page<SarreBrandUser> page = userService
                .allUsers(PageRequest.of(0, 5));

        assertEquals(10, page.getTotalElements());

        var list1 = page
                .stream().toList();
        assertEquals(5, list1.size());

        var list2 = userService.allUsers(PageRequest.of(1, 5))
                .stream().toList();

        assertEquals(5, list2.size());

        // assert list 1 and 2 do not equal as custom pagination
        assertNotEquals(list1, list2);

        var list3 = userService.allUsers(PageRequest.of(2, 5))
                .stream().toList();
        assertTrue(list3.isEmpty());
    }

    @Test
    void count() {
        // given
        for (int i = 0; i < 10; i++) {
            userService
                    .save(
                            SarreBrandUser.builder()
                                    .firstname("test")
                                    .lastname("lastname")
                                    .email("test@email.com" + i)
                                    .phoneNumber("0000000000")
                                    .password("password123")
                                    .enabled(true)
                                    .clientRole(new HashSet<>())
                                    .build()
                    );
        }

        assertEquals(10, userService.count());
    }

}