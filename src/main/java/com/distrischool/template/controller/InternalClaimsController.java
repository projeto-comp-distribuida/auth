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
@RequestMapping("/internal/claims")
@RequiredArgsConstructor
@Slf4j
public class InternalClaimsController {

    private final UserRepository userRepository;
    private final PermissionService permissionService;

    @Value("${app.internal.actions-token:}")
    private String actionsSharedToken;

    public record ClaimsRequest(String sub) {}

    @PostMapping
    public ResponseEntity<?> getClaims(@RequestHeader(value = "X-Internal-Token", required = false) String internalToken,
                                       @RequestBody ClaimsRequest request) {
        if (actionsSharedToken == null || actionsSharedToken.isBlank()) {
            log.warn("Internal claims endpoint called but shared token is not configured");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("error", "actions token not configured"));
        }

        if (internalToken == null || !Objects.equals(internalToken, actionsSharedToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "unauthorized"));
        }

        if (request == null || request.sub() == null || request.sub().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "sub is required"));
        }

        Optional<User> userOpt = userRepository.findByAuth0Id(request.sub());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "user not found"));
        }

        User user = userOpt.get();
        Set<UserRole> userRoles = new HashSet<>(user.getRoles());
        List<String> roles = userRoles.stream().map(UserRole::name).sorted().toList();
        List<String> permissions = new ArrayList<>(permissionService.getPermissionsFromRoles(userRoles));
        permissions.sort(String::compareTo);

        Map<String, Object> payload = new HashMap<>();
        payload.put("roles", roles);
        payload.put("permissions", permissions);
        payload.put("sub", request.sub());

        return ResponseEntity.ok(payload);
    }
}


