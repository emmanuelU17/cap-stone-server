package com.sarabrandserver.user.repository;

import com.sarabrandserver.exception.CustomSqlException;
import com.sarabrandserver.user.entity.SarreBrandUser;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl {

    private final JdbcClient jdbcClient;

    public SarreBrandUser save(@NotNull SarreBrandUser user) {
        String sql = """
        INSERT INTO clientz (
            firstname, lastname, email, phone_number,
            password, enabled
        ) VALUES (
            :firstname, :lastname, :email, :phone,
            :password, :enabled
        );
        """;

        KeyHolder key = new GeneratedKeyHolder();
        jdbcClient.sql(sql)
                .param("firstname", user.getFirstname())
                .param("lastname", user.getLastname())
                .param("email", user.getEmail())
                .param("phone", user.getPhoneNumber())
                .param("password", user.getPassword())
                .param("enabled", user.isEnabled() ? 1 : 0)
                .update(key);

        return findById(Objects.requireNonNull(key.getKey()).longValue())
                .orElseThrow(() -> new CustomSqlException("unable to save user name " + user.getFirstname()));
    }

    public Optional<SarreBrandUser> findById(@NotNull final Long roleId) {
        return this.jdbcClient
                .sql("SELECT * FROM clientz c WHERE c.client_id = :id;")
                .param("id", roleId)
                .query(SarreBrandUser.class)
                .optional();
    }

    public Optional<SarreBrandUser> findByPrincipal(@NotNull final String principal) {
        return this.jdbcClient
                .sql("SELECT * FROM clientz c WHERE c.email = :email;")
                .param("email", principal)
                .query(SarreBrandUser.class)
                .optional();
    }

    /**
     * Validates if users principal exists.
     *
     * @param principal is email of a {code SarreBrandUser}
     * @return boolean where true means user principal exists and false is otherwise.
     * */
    public boolean doesPrincipalExist(@NotNull final String principal) {
        return this.jdbcClient
                .sql("SELECT * FROM clientz c WHERE c.email = :email;")
                .param("email", principal)
                .query(SarreBrandUser.class)
                .optional()
                .isPresent();
    }

    /**
     * Returns a Page of {@code SarreBrandUser}
     * */
    public Page<SarreBrandUser> allUsers(@NotNull Pageable request) {
        int total = count();
        var list = jdbcClient
                .sql("SELECT * FROM clientz LIMIT :size OFFSET :calc;")
                .param("size", request.getPageSize())
                .param("calc", request.getOffset())
                .query(SarreBrandUser.class)
                .list();

        return new PageImpl<>(list, request, total);
    }

    /**
     * Returns the number of users in the db
     * */
    public int count() {
        return this.jdbcClient
                .sql("SELECT COUNT(c.client_id) FROM clientz c;")
                .query(Integer.class)
                .single();
    }

}
