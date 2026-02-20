package com.subhayan.authservice.dto;

import java.util.List;

public record PagedUserResponse(
        List<UserDetailsResponse> users,
        int page,
        int pageSize,
        long totalUsers
) {}
