package com.distrischool.template.feign;

import com.distrischool.template.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * Feign client para comunicação com o serviço de gestão de alunos
 */
@FeignClient(name = "student-service", url = "${microservice.student.url:http://localhost:8081}")
public interface StudentServiceClient {

    /**
     * Busca o ID do aluno por Auth0 ID
     * 
     * @param auth0Id Auth0 ID do usuário
     * @return Resposta contendo o ID do aluno ou 404 se não encontrado
     */
    @GetMapping("/api/v1/students/by-auth0/{auth0Id}")
    ApiResponse<Map<String, Long>> getStudentIdByAuth0Id(@PathVariable String auth0Id);
}

