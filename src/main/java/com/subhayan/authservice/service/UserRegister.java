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

import java.time.LocalDate;

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

        LocalDate dateOfBirth = userRegisterRequestDTO.dateOfBirth();
        if (dateOfBirth.isBefore(LocalDate.now().minusYears(100)) || dateOfBirth.isAfter(LocalDate.now().minusYears(13))) {
            throw new IllegalArgumentException("INVALID AGE : User must be between 13 and 100 years old");
        }

        entity.setEmail(String.valueOf(userRegisterRequestDTO.email()));
        String password = userRegisterRequestDTO.password();
        String encodedPassword = passwordEncoder.encode(password);
        entity.setPassword(encodedPassword);
        entity.setRole(Role.USER);
        entity.setSalutation(userRegisterRequestDTO.salutation());
        entity.setFirstName(userRegisterRequestDTO.firstName());
        entity.setLastName(userRegisterRequestDTO.lastName());
        entity.setPhoneNumber(userRegisterRequestDTO.phoneNumber());
        entity.setDateOfBirth(userRegisterRequestDTO.dateOfBirth());


        return entity;
    }

    private UserRegisterResponseDTO mapRequestDTOToRequest(@NotNull UserEntity entity) {
        return new UserRegisterResponseDTO(
                entity.getId(),
                entity.getSalutation(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getEmail(),
                entity.getPhoneNumber(),
                entity.getDateOfBirth(),
                entity.getRole()
        );
    }
}
