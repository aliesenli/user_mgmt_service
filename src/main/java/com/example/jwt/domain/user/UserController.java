package com.example.jwt.domain.user;

import com.example.jwt.domain.module.ModuleClient;
import com.example.jwt.domain.user.dto.UserDTO;
import com.example.jwt.domain.user.dto.UserMapper;
import com.example.jwt.domain.user.dto.UserRegisterDTO;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/users")
public class UserController {

  private final UserService userService;
  private final UserMapper userMapper;
  private final ModuleClient moduleClient;

  public UserController(UserService userService, UserMapper userMapper, ModuleClient moduleClient) {
    this.userService = userService;
    this.userMapper = userMapper;
    this.moduleClient = moduleClient;
  }

  @GetMapping("/me")
  public ResponseEntity<UserDTO> getAuthenticatedUser() {
    UserDetailsImpl userDetailsImpl = (UserDetailsImpl) SecurityContextHolder.getContext()
        .getAuthentication().getPrincipal();
    return ResponseEntity.ok(userMapper.toDTO(userDetailsImpl.user()));
  }

  @GetMapping("/{id}")
  public ResponseEntity<UserDTO> retrieveById(@PathVariable UUID id) {
    return ResponseEntity.ok(userMapper.toDTO(userService.findById(id)));
  }

  @GetMapping({"", "/"})
  public ResponseEntity<List<UserDTO>> retrieveAll() {
    return ResponseEntity.ok(userMapper.toDTOs(userService.findAll()));
  }

  @PostMapping("/register")
  public ResponseEntity<UserDTO> register(@Valid @RequestBody UserRegisterDTO userRegisterDTO) {
    User user = userService.register(userMapper.fromUserRegisterDTO(userRegisterDTO));
    return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toDTO(user));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAuthority('USER_MODIFY') && @userPermissionEvaluator.isUserAboveAge(authentication.principal.user,18)")
  public ResponseEntity<UserDTO> updateById(@PathVariable UUID id,
      @Valid @RequestBody UserDTO userDTO) {
    User user = userService.updateById(id, userMapper.fromDTO(userDTO));
    return ResponseEntity.ok(userMapper.toDTO(user));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAuthority('USER_DELETE')")
  public ResponseEntity<Void> deleteById(@PathVariable UUID id) {
    userService.deleteById(id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{userId}/modules/{moduleId}")
  public ResponseEntity<?> assignModule(@PathVariable UUID userId, @PathVariable UUID moduleId) {
    userService.findById(userId);
    if (!moduleClient.isModuleAvailable(moduleId)) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(Map.of("code", "MODULE_NOT_FOUND", "message", "Module " + moduleId + " was not found"));
    }
    return ResponseEntity.ok(Map.of("userId", userId, "moduleId", moduleId));
  }
}
