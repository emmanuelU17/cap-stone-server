package com.sarabrandserver.user.repository;

import com.sarabrandserver.enumeration.RoleEnum;
import com.sarabrandserver.exception.CustomSqlException;
import com.sarabrandserver.user.entity.ClientRole;
import com.sarabrandserver.user.entity.SarreBrandUser;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
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
public class RoleServiceImpl implements RoleService {

    private final JdbcClient jdbcClient;

    /**
     * Using JDBC, a new {@code ClientRole} is saved
     *
     * @param role is of type {code RoleEnum}
     * @param user is of {@code SarreBrandUser}
     * @throws CustomSqlException which is a checked exception
     * @return {code ClientRole}
     * */
    @Override
    public ClientRole save(@NotNull final RoleEnum role, @NotNull final SarreBrandUser user) {
        String sql = """
        INSERT INTO client_role(role, client_id)
        VALUES (:role, :id);
        """;

        KeyHolder key = new GeneratedKeyHolder();

        this.jdbcClient
                .sql(sql)
                .param("role", role.name())
                .param("id", user.getClientId())
                .update(key);

        return findById(Objects.requireNonNull(key.getKey()).longValue())
                .orElseThrow(() -> new CustomSqlException("unable to save role " + role.name()));
    }

    /**
     * Returns a {@code Optional} of {@code ClientRole} base on its id or primary key
     * */
    @Override
    public Optional<ClientRole> findById(@NotNull final Long roleId) {
        return this.jdbcClient
                .sql("SELECT * FROM client_role r WHERE r.role_id = :id")
                .param("id", roleId)
                .query(ClientRole.class)
                .optional();
    }

}
