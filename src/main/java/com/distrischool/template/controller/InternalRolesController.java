package com.distrischool.template.controller;

import com.distrischool.template.entity.User;
import com.distrischool.template.entity.UserRole;
import com.distrischool.template.repository.UserRepository;
import com.distrischool.template.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
@Slf4j
public class InternalRolesController {

    private final UserRepository userRepository;
    private final PermissionService permissionService;

    @Value("${app.internal.shared-token:}")
    private String sharedToken;

    @GetMapping("/{sub}/roles")
    public ResponseEntity<?> getRoles(@RequestHeader(value = "X-Internal-Token", required = false) String token,
                                      @PathVariable("sub") String sub) {
        if (sharedToken == null || sharedToken.isBlank()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("error", "shared token not configured"));
        }
        if (token == null || !Objects.equals(token, sharedToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "unauthorized"));
        }
        if (sub == null || sub.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "sub is required"));
        }

        Optional<User> userOpt = userRepository.findByAuth0Id(sub);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "user not found"));
        }

        User user = userOpt.get();
        Set<UserRole> userRoles = new HashSet<>(user.getRoles());
        List<String> roles = userRoles.stream().map(UserRole::name).sorted().toList();
        List<String> permissions = new ArrayList<>(permissionService.getPermissionsFromRoles(userRoles));
        permissions.sort(String::compareTo);

        return ResponseEntity.ok(Map.of(
            "sub", sub,
            "roles", roles,
            "permissions", permissions
        ));
    }
}


