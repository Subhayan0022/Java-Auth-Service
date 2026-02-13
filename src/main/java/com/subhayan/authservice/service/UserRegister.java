package com.subhayan.authservice.service;

import com.subhayan.authservice.dto.DtoAuthRegister.*;
import com.subhayan.authservice.entity.UserEntity;
import com.subhayan.authservice.repository.UserRepository;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;

@Service
public class UserRegister {
    private final UserRepository userRepository;

    public UserRegister(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserRegisterResponseDTO registerUser(UserRegisterRequestDTO userRegisterRequestDTO) {
        UserEntity userEntity = mapRequestDTOToEntity(userRegisterRequestDTO);
        UserEntity savedUserEntity = userRepository.save(userEntity);

        return mapRequestDTOToRequest(savedUserEntity);
    }

    private UserEntity mapRequestDTOToEntity(@NotNull UserRegisterRequestDTO userRegisterRequestDTO) {
        UserEntity entity = new UserEntity();
        entity.setEmail(String.valueOf(userRegisterRequestDTO.email()));
        entity.setPassword(userRegisterRequestDTO.password());

        return entity;
    }

    private UserRegisterResponseDTO mapRequestDTOToRequest(@NotNull UserEntity entity) {
        return  new UserRegisterResponseDTO(entity.getId(), entity.getEmail(), entity.getRole());
    }
}
