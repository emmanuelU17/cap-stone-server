package com.sarabrandserver.user.repository;

import com.sarabrandserver.enumeration.RoleEnum;
import com.sarabrandserver.exception.CustomSqlException;
import com.sarabrandserver.user.entity.ClientRole;
import com.sarabrandserver.user.entity.SarreBrandUser;
import jakarta.validation.constraints.NotNull;

import java.util.Optional;

public interface RoleService {

    ClientRole save(@NotNull final RoleEnum role, @NotNull final SarreBrandUser user) throws CustomSqlException;
    Optional<ClientRole> findById(@NotNull final Long id);

}
