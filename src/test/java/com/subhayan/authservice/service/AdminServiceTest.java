package com.subhayan.authservice.service;

import com.subhayan.authservice.dto.AdminUpdateRequest;
import com.subhayan.authservice.dto.PagedUserResponse;
import com.subhayan.authservice.dto.UserDetailsResponse;
import com.subhayan.authservice.entity.Role;
import com.subhayan.authservice.entity.Salutation;
import com.subhayan.authservice.entity.UserEntity;
import com.subhayan.authservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AdminService adminService;

    private UserEntity userEntity;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        userEntity = UserEntity.builder()
                .id(userId)
                .salutation(Salutation.MR)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .password("encoded")
                .phoneNumber("+1234567890")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .role(Role.USER)
                .createdAt(LocalDateTime.of(2024, 1, 1, 0, 0))
                .active(true)
                .build();
    }

    @Test
    void getUserDetailsById_withExistingUser_returnsUserDetails() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));

        UserDetailsResponse response = adminService.getUserDetailsById(userId);

        assertThat(response.id()).isEqualTo(userId);
        assertThat(response.email()).isEqualTo("john@example.com");
        assertThat(response.firstName()).isEqualTo("John");
        assertThat(response.lastName()).isEqualTo("Doe");
        assertThat(response.salutation()).isEqualTo(Salutation.MR);
        assertThat(response.role()).isEqualTo(Role.USER);
        assertThat(response.phoneNumber()).isEqualTo("+1234567890");
        assertThat(response.dateOfBirth()).isEqualTo(LocalDate.of(1990, 1, 1));
        assertThat(response.createdAt()).isEqualTo(LocalDateTime.of(2024, 1, 1, 0, 0));
    }

    @Test
    void getUserDetailsById_withNonExistentUser_throwsRuntimeException() {
        UUID unknownId = UUID.randomUUID();
        when(userRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.getUserDetailsById(unknownId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User with id " + unknownId + " not found");
    }

    @Test
    void queryUsers_withRoleFilter_returnsFilteredResults() {
        Page<UserEntity> page = new PageImpl<>(List.of(userEntity), PageRequest.of(0, 10), 1);
        when(userRepository.findByRoleAndActive(eq(Role.USER), eq(true), any(PageRequest.class))).thenReturn(page);

        PagedUserResponse response = adminService.queryUsers(Role.USER, 0, 10);

        assertThat(response.users()).hasSize(1);
        assertThat(response.users().get(0).email()).isEqualTo("john@example.com");
        assertThat(response.page()).isZero();
        assertThat(response.pageSize()).isEqualTo(10);
        assertThat(response.totalUsers()).isEqualTo(1);
    }

    @Test
    void queryUsers_withoutRoleFilter_returnsAllActiveUsers() {
        Page<UserEntity> page = new PageImpl<>(List.of(userEntity), PageRequest.of(0, 10), 1);
        when(userRepository.findByActive(eq(true), any(PageRequest.class))).thenReturn(page);

        PagedUserResponse response = adminService.queryUsers(null, 0, 10);

        assertThat(response.users()).hasSize(1);
        verify(userRepository).findByActive(eq(true), any(PageRequest.class));
        verify(userRepository, never()).findByRoleAndActive(any(), anyBoolean(), any());
    }

    @Test
    void updateUser_withAllFields_updatesAllFields() {
        AdminUpdateRequest request = new AdminUpdateRequest(
                Salutation.MRS, "Jane", "Smith", "jane@example.com",
                "+9876543210", LocalDate.of(1985, 6, 15), Role.ADMIN
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);

        adminService.updateUser(userId, request);

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(captor.capture());

        UserEntity updated = captor.getValue();
        assertThat(updated.getSalutation()).isEqualTo(Salutation.MRS);
        assertThat(updated.getFirstName()).isEqualTo("Jane");
        assertThat(updated.getLastName()).isEqualTo("Smith");
        assertThat(updated.getEmail()).isEqualTo("jane@example.com");
        assertThat(updated.getPhoneNumber()).isEqualTo("+9876543210");
        assertThat(updated.getDateOfBirth()).isEqualTo(LocalDate.of(1985, 6, 15));
        assertThat(updated.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void updateUser_withNullFields_preservesExistingValues() {
        AdminUpdateRequest request = new AdminUpdateRequest(
                null, "UpdatedFirst", null, null, null, null, null
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);

        adminService.updateUser(userId, request);

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(captor.capture());

        UserEntity updated = captor.getValue();
        assertThat(updated.getFirstName()).isEqualTo("UpdatedFirst");
        assertThat(updated.getSalutation()).isEqualTo(Salutation.MR);
        assertThat(updated.getLastName()).isEqualTo("Doe");
        assertThat(updated.getEmail()).isEqualTo("john@example.com");
        assertThat(updated.getPhoneNumber()).isEqualTo("+1234567890");
        assertThat(updated.getRole()).isEqualTo(Role.USER);
    }

    @Test
    void updateUser_withNonExistentUser_throwsRuntimeException() {
        UUID unknownId = UUID.randomUUID();
        AdminUpdateRequest request = new AdminUpdateRequest(
                null, "Name", null, null, null, null, null
        );
        when(userRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.updateUser(unknownId, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User with id " + unknownId + " not found");

        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUser_withExistingUser_softDeletesBySettingInactive() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);

        adminService.deleteUser(userId);

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().isActive()).isFalse();
    }

    @Test
    void deleteUser_withNonExistentUser_throwsRuntimeException() {
        UUID unknownId = UUID.randomUUID();
        when(userRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.deleteUser(unknownId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User with id " + unknownId + " not found");

        verify(userRepository, never()).save(any());
    }
}
