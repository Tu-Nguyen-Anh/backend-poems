package org.oplearn.project.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oplearn.project.dto.request.UserRequest;
import org.oplearn.project.dto.request.UserUpdateRequest;
import org.oplearn.project.dto.response.PageResponse;
import org.oplearn.project.dto.response.UserResponse;
import org.oplearn.project.entity.User;
import org.oplearn.project.exception.EmailAlreadyExistedException;
import org.oplearn.project.exception.ProtectedAccountException;
import org.oplearn.project.exception.UserNotFoundException;
import org.oplearn.project.exception.UserUnauthorizedException;
import org.oplearn.project.exception.UsernameAlreadyExistedException;
import org.oplearn.project.repository.UserRepository;
import org.oplearn.project.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
  private final UserRepository repository;
  private final PasswordEncoder passwordEncoder;

  @Override
  @Transactional
  public UserResponse create(UserRequest request) {
    log.info("create user");
    if (repository.existsByUsernameAndIsDeletedFalse(request.getUsername())) {
      throw new UsernameAlreadyExistedException();
    }
    if (StringUtils.hasText(request.getEmail())
      && repository.existsByEmailAndIsDeletedFalse(request.getEmail())) {
      throw new EmailAlreadyExistedException();
    }

    User user = User.builder()
      .username(request.getUsername())
      .password(passwordEncoder.encode(request.getPassword()))
      .phoneNumber(request.getPhoneNumber())
      .email(request.getEmail())
      .role(request.getRole())
      .build();

    return UserResponse.from(repository.save(user));
  }

  @Override
  @Transactional
  public UserResponse update(UserUpdateRequest request, Long id) {
    log.info("(update) id: {}", id);

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String currentUsername = authentication.getName();

    User currentUser = repository.findByUsernameAndIsDeletedFalse(currentUsername)
      .orElseThrow(UserNotFoundException::new);

    boolean isAdmin = authentication.getAuthorities().stream()
      .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

    boolean isSelf = currentUser.getId().equals(id);

    if (!isAdmin && !isSelf) {
      log.warn("(update) user not authorized");
      throw new UserUnauthorizedException();
    }

    User user = repository.findByIdAndIsDeletedFalse(id)
      .orElseThrow(UserNotFoundException::new);

    // Tài khoản hệ thống (admin/superadmin) chỉ chính chủ mới được sửa,
    // admin khác không được đụng tới.
    if (isProtectedAccount(user.getUsername()) && !isSelf) {
      log.warn("(update) protected account {} chỉ chính chủ được sửa", user.getUsername());
      throw new ProtectedAccountException();
    }

    if (!Objects.equals(user.getUsername(), request.getUsername())
      && repository.existsByUsernameAndIsDeletedFalse(request.getUsername())) {
      throw new UsernameAlreadyExistedException();
    }
    if (StringUtils.hasText(request.getEmail())
      && !Objects.equals(user.getEmail(), request.getEmail())
      && repository.existsByEmailAndIsDeletedFalse(request.getEmail())) {
      throw new EmailAlreadyExistedException();
    }

    user.setUsername(request.getUsername());
    user.setPhoneNumber(request.getPhoneNumber());
    user.setEmail(request.getEmail());
    // Chỉ đổi mật khẩu khi có nhập (sửa thông tin không reset mật khẩu).
    if (StringUtils.hasText(request.getPassword())) {
      user.setPassword(passwordEncoder.encode(request.getPassword()));
    }
    // Nâng/hạ vai trò: chỉ admin mới được đổi role.
    if (request.getRole() != null && isAdmin) {
      user.setRole(request.getRole());
    }

    return UserResponse.from(repository.save(user));
  }

  /** Username các tài khoản hệ thống được bảo vệ (hardcode). */
  private static final java.util.Set<String> PROTECTED_USERNAMES = java.util.Set.of("admin", "superadmin");

  private boolean isProtectedAccount(String username) {
    return username != null && PROTECTED_USERNAMES.contains(username.toLowerCase());
  }

  @Override
  public PageResponse<UserResponse> list(String keyword, int size, int page, boolean isAll) {
    Pageable pageable = isAll
      ? PageRequest.of(0, org.oplearn.project.constants.OpLearnConstants.VariableConstant.MAX_ALL_SIZE)
      : PageRequest.of(page, size);

    Page<User> users = StringUtils.hasText(keyword)
      ? repository.search(keyword, pageable)
      : repository.findAllByIsDeletedFalse(pageable);

    return PageResponse.of(
      users.map(UserResponse::from).getContent(),
      (int) users.getTotalElements()
    );
  }

  @Override
  public UserResponse detail(Long id) {
    // Chống IDOR: chỉ chính chủ hoặc admin mới xem được profile của một id.
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    User currentUser = repository.findByUsernameAndIsDeletedFalse(authentication.getName())
      .orElseThrow(UserNotFoundException::new);
    boolean isAdmin = authentication.getAuthorities().stream()
      .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    if (!isAdmin && !currentUser.getId().equals(id)) {
      log.warn("(detail) user {} not authorized to view user {}", currentUser.getId(), id);
      throw new UserUnauthorizedException();
    }

    return repository.findByIdAndIsDeletedFalse(id)
      .map(UserResponse::from)
      .orElseThrow(UserNotFoundException::new);
  }

  @Override
  @Transactional
  public void delete(Long id) {
    User user = repository.findByIdAndIsDeletedFalse(id)
      .orElseThrow(UserNotFoundException::new);
    // Không cho xoá tài khoản hệ thống (admin/superadmin).
    if (isProtectedAccount(user.getUsername())) {
      log.warn("(delete) từ chối xoá tài khoản bảo vệ {}", user.getUsername());
      throw new ProtectedAccountException();
    }
    repository.softDeleteById(id);
  }

  public User getUsernameOrThrow(String username) {
    return repository.findByUsernameAndIsDeletedFalse(username)
      .orElseThrow(UserNotFoundException::new);
  }

  public User getAvailableUserAndThrow(Long id) {
    return repository.findByIdAndIsDeletedFalse(id)
      .orElseThrow(UserNotFoundException::new);
  }
}
