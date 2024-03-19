<<<<<<<< HEAD:webserver/src/main/java/dev/webserver/user/service/SarreBrandUserService.java
package dev.webserver.user.service;

import dev.webserver.user.entity.SarreBrandUser;
import dev.webserver.user.repository.UserRepository;
import dev.webserver.user.res.UserResponse;
========
package dev.capstone.user.service;

import dev.capstone.user.entity.SarreBrandUser;
import dev.capstone.user.repository.UserRepository;
import dev.capstone.user.res.UserResponse;
>>>>>>>> 38dca43c14b569b33b94a23c1bdce50584a67195:src/main/java/dev/capstone/user/service/SarreBrandUserService.java
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SarreBrandUserService {

    private final UserRepository userRepository;


    public Optional<SarreBrandUser> userByPrincipal(String principal) {
        return userRepository.userByPrincipal(principal);
    }

    /**
     * Returns a {@link Page} of {@link SarreBrandUser}.
     *
     * @param page number in the UI
     * @param size max amount of json pulled at one
     * @return {@link Page} of {@link UserResponse}.
     * */
    public Page<UserResponse> allUsers(int page, int size) {
        return userRepository
                .allUsers(PageRequest.of(page, size))
                .map(s -> new UserResponse(s.getFirstname(), s.getLastname(), s.getEmail(), s.getPhoneNumber()));
    }

}
