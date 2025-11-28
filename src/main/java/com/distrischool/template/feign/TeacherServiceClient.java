package com.distrischool.template.feign;

import com.distrischool.template.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * Feign client para comunicação com o serviço de gestão de professores
 */
@FeignClient(name = "teacher-service", url = "${microservice.teacher.url:http://localhost:8082}")
public interface TeacherServiceClient {

    /**
     * Busca o ID do professor por Auth0 ID
     * 
     * @param auth0Id Auth0 ID do usuário
     * @return Resposta contendo o ID do professor ou 404 se não encontrado
     */
    @GetMapping("/api/v1/teachers/by-auth0/{auth0Id}")
    ApiResponse<Map<String, Long>> getTeacherIdByAuth0Id(@PathVariable String auth0Id);
}

