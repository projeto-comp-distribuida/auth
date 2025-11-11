package com.distrischool.auth.security;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.distrischool.auth.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Filtro de autenticação JWT.
 * 
 * Suporta apenas Auth0 JWT tokens.
 * Auth0 gerencia completamente: passwords, email verification, password reset, MFA, login attempts.
 */
@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    @Lazy
    private Auth0JwtValidator auth0Validator;
    
    @Autowired
    @Lazy
    private EnhancedJwtValidator enhancedJwtValidator;
    
    @Autowired
    @Lazy
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        
        final String authHeader = request.getHeader("Authorization");
        
        // Verifica se o header Authorization está presente e começa com "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Extrai o token JWT do header
            final String jwt = authHeader.substring(7);
            
            // Verifica se o usuário já não está autenticado
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                filterChain.doFilter(request, response);
                return;
            }

            // Tenta validar como token enriquecido primeiro, depois Auth0 (backwards compatibility)
            if (!authenticateWithEnhancedToken(request, jwt)) {
                authenticateWithAuth0(request, jwt);
            }

        } catch (Exception e) {
            log.error("Erro ao processar token JWT: {}", e.getMessage());
            // Não bloqueia a requisição, apenas não autentica
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Autentica usando token enriquecido (com roles/permissions do DB)
     * @return true se autenticou com sucesso, false caso contrário
     */
    private boolean authenticateWithEnhancedToken(HttpServletRequest request, String token) {
        try {
            // Valida o token enriquecido
            DecodedJWT jwt = enhancedJwtValidator.validateToken(token);
            
            // Extrai informações do token
            String auth0Id = enhancedJwtValidator.getAuth0Id(jwt);
            String email = enhancedJwtValidator.getEmail(jwt);
            List<String> roles = enhancedJwtValidator.getRoles(jwt);
            List<String> permissions = enhancedJwtValidator.getPermissions(jwt);
            
            // Busca o usuário no banco (opcional, para pegar userId)
            var user = userRepository.findByAuth0Id(auth0Id).orElse(null);
            Long userId = user != null ? user.getId() : null;
            
            // Converte roles para authorities do Spring Security
            List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
            
            // Adiciona permissions como authorities também
            permissions.forEach(permission -> 
                authorities.add(new SimpleGrantedAuthority(permission))
            );

            // Cria o objeto de autenticação
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                new UserPrincipal(userId, email, auth0Id),
                null,
                authorities
            );
            
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
            
            log.debug("Usuário autenticado via token enriquecido: {} (auth0_id: {}) com roles: {} e permissions: {}", 
                email, auth0Id, roles, permissions);
            
            return true;
            
        } catch (Exception e) {
            // Token não é um token enriquecido válido, retorna false para tentar Auth0
            log.debug("Token não é um token enriquecido válido, tentando Auth0: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Autentica usando token Auth0 (backwards compatibility)
     */
    private void authenticateWithAuth0(HttpServletRequest request, String token) {
        try {
            // Valida o token Auth0 usando JWK
            DecodedJWT jwt = auth0Validator.validateToken(token);
            
            // Extrai informações do token
            String auth0Id = auth0Validator.getAuth0Id(jwt);
            String email = auth0Validator.getEmail(jwt);
            List<String> roles = auth0Validator.getRoles(jwt);
            
            // Busca o usuário no banco (precisa estar sincronizado)
            var user = userRepository.findByAuth0Id(auth0Id).orElse(null);
            
            Long userId = user != null ? user.getId() : null;
            
            // Se não encontrou usuário, usa roles do token
            // Se encontrou, usa roles do banco (mais atualizado)
            List<String> effectiveRoles = user != null && !user.getRoles().isEmpty()
                ? user.getRoles().stream().map(r -> r.getName().name()).collect(Collectors.toList())
                : roles;
            
            // Converte roles para authorities do Spring Security
            List<SimpleGrantedAuthority> authorities = effectiveRoles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());

            // Cria o objeto de autenticação
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                new UserPrincipal(userId, email, auth0Id),
                null,
                authorities
            );
            
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
            
            log.debug("Usuário autenticado via Auth0: {} (auth0_id: {}) com roles: {}", 
                email, auth0Id, effectiveRoles);
            
        } catch (Exception e) {
            log.warn("Falha ao validar token Auth0: {}", e.getMessage());
            throw e;
        }
    }

}

