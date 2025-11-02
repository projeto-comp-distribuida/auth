package com.distrischool.template.service;

import com.distrischool.template.entity.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Serviço que mapeia roles para permissions.
 * Define quais permissões cada role possui no sistema.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionService {

    /**
     * Mapeia roles para suas respectivas permissions.
     * Retorna todas as permissions que o usuário possui baseado em suas roles.
     */
    public List<String> getPermissionsFromRoles(Set<UserRole> roles) {
        Set<String> permissions = new HashSet<>();
        
        for (UserRole role : roles) {
            permissions.addAll(getPermissionsForRole(role));
        }
        
        return new ArrayList<>(permissions);
    }

    /**
     * Retorna as permissions para uma role específica.
     */
    private List<String> getPermissionsForRole(UserRole role) {
        return switch (role) {
            case ADMIN -> Arrays.asList(
                // Teachers
                "read:teachers",
                "write:teachers",
                "delete:teachers",
                // Students
                "read:students",
                "write:students",
                "delete:students",
                // Classes/Grades
                "read:classes",
                "write:classes",
                "delete:classes",
                // Grades
                "read:grades",
                "write:grades",
                "delete:grades",
                // Attendance
                "read:attendance",
                "write:attendance",
                // Reports
                "read:reports",
                "write:reports",
                // Users
                "read:users",
                "write:users",
                "delete:users"
            );
            case TEACHER -> Arrays.asList(
                // Teachers (own info)
                "read:teachers",
                "write:teachers",
                // Students (classes assigned)
                "read:students",
                // Classes/Grades (assigned)
                "read:classes",
                "write:classes",
                // Grades (can grade students)
                "read:grades",
                "write:grades",
                // Attendance (can mark attendance)
                "read:attendance",
                "write:attendance"
            );
            case STUDENT -> Arrays.asList(
                // Students (own info)
                "read:students",
                // Classes (enrolled)
                "read:classes",
                // Grades (own grades)
                "read:grades",
                // Attendance (own attendance)
                "read:attendance"
            );
            case PARENT -> Arrays.asList(
                // Students (children)
                "read:students",
                // Classes (children's classes)
                "read:classes",
                // Grades (children's grades)
                "read:grades",
                // Attendance (children's attendance)
                "read:attendance"
            );
        };
    }
}

