package com.subhayan.authservice.service;

import com.subhayan.authservice.dto.DtoAuthRegister.*;
import com.subhayan.authservice.entity.Role;
import com.subhayan.authservice.entity.UserEntity;
import com.subhayan.authservice.exception.UserAlreadyExistsException;
import com.subhayan.authservice.repository.UserRepository;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserRegister {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserRegister(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    public UserRegisterResponseDTO registerUser(UserRegisterRequestDTO userRegisterRequestDTO) {
        UserEntity userEntity = mapRequestDTOToEntity(userRegisterRequestDTO);
        UserEntity savedUserEntity = userRepository.save(userEntity);

        return mapRequestDTOToRequest(savedUserEntity);
    }

    private UserEntity mapRequestDTOToEntity(@NotNull UserRegisterRequestDTO userRegisterRequestDTO) {
        UserEntity entity = new UserEntity();
        if (userRepository.existsByEmail(String.valueOf(userRegisterRequestDTO.email()))){
            throw new UserAlreadyExistsException("A user with this email already exists");
        }
        entity.setEmail(String.valueOf(userRegisterRequestDTO.email()));
        String password = userRegisterRequestDTO.password();
        String encodedPassword = passwordEncoder.encode(password);
        entity.setPassword(encodedPassword);
        entity.setRole(Role.USER);

        return entity;
    }

    private UserRegisterResponseDTO mapRequestDTOToRequest(@NotNull UserEntity entity) {
        return  new UserRegisterResponseDTO(entity.getId(), entity.getEmail(), entity.getRole());
    }
}
