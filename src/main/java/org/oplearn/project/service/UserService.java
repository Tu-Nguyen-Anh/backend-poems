package org.oplearn.project.service;

import org.oplearn.project.dto.request.UserRequest;
import org.oplearn.project.dto.request.UserUpdateRequest;
import org.oplearn.project.dto.response.PageResponse;
import org.oplearn.project.dto.response.UserResponse;
import org.oplearn.project.entity.User;

public interface UserService {
  UserResponse create(UserRequest request);

  UserResponse update(UserUpdateRequest request, Long id);

  void delete(Long id);

  PageResponse<UserResponse> list(String keyword, int size, int page, boolean isAll);

  UserResponse detail(Long id);

  User getUsernameOrThrow(String username);

  User getAvailableUserAndThrow(Long id);
}
