# DistriSchool Microservice Template

Template base para microsserviços do DistriSchool - Sistema de Gestão Escolar Distribuído.

## 🏗️ Arquitetura

Este template segue a arquitetura de microsserviços definida para o DistriSchool:

- **Backend**: Spring Boot 3.2.0 com Spring Data JPA e Spring Kafka
- **Banco de Dados**: PostgreSQL com Flyway para migrações
- **Cache**: Redis para performance
- **Mensageria**: Apache Kafka para comunicação assíncrona
- **Comunicação**: Spring Cloud OpenFeign para comunicação entre serviços
- **Resiliência**: Resilience4j Circuit Breaker
- **Monitoramento**: Prometheus + Micrometer

## 🚀 Funcionalidades Base

### Estrutura do Template

```
src/main/java/com/distrischool/template/
├── controller/          # Controllers REST
│   └── HealthController.java
├── dto/                # Data Transfer Objects
│   └── ApiResponse.java
├── entity/             # Entidades JPA
│   ├── BaseEntity.java
│   └── SystemConfig.java
├── kafka/              # Configuração Kafka
│   ├── DistriSchoolEvent.java
│   ├── EventProducer.java
│   └── EventConsumer.java
├── config/             # Configurações Spring
├── exception/          # Tratamento de exceções
├── repository/         # Repositórios JPA
├── service/           # Lógica de negócio
└── TemplateApplication.java
```

### Componentes Principais

#### 1. BaseEntity
Entidade base com campos comuns:
- Auditoria (created_at, updated_at, created_by, updated_by)
- Soft delete (deleted_at, deleted_by)
- Métodos utilitários para exclusão lógica

#### 2. ApiResponse
DTO padronizado para respostas da API:
- Formato consistente de resposta
- Métodos estáticos para sucesso/erro
- Timestamp automático

#### 3. DistriSchoolEvent
Evento base para comunicação Kafka:
- Estrutura padronizada para eventos
- Metadados e dados flexíveis
- Métodos utilitários para criação

#### 4. HealthController
Controller de exemplo com endpoints de saúde:
- `/api/v1/health` - Status do serviço
- `/api/v1/health/info` - Informações do serviço

## 📋 Requisitos do DistriSchool

Este template está preparado para implementar as funcionalidades do DistriSchool:

### Microsserviços Planejados
- **school-core-service**: Gestão de alunos/turmas
- **notification-service**: Envio de mensagens
- **user-service**: Autenticação e autorização
- **teacher-service**: Gestão de professores
- **schedule-service**: Gestão de horários
- **attendance-service**: Registro de presenças
- **grade-service**: Gestão de notas

### Tópicos Kafka
- `student.created` - Aluno criado
- `teacher.assigned` - Professor atribuído
- `schedule.updated` - Horário atualizado
- `attendance.recorded` - Presença registrada
- `user.logged` - Usuário logado

## 🛠️ Como Usar

### 1. Configuração do Ambiente

```bash
# Clone o template
git clone <repository-url>
cd microservice-template

# Configure as variáveis de ambiente
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/distrischool_template
export SPRING_DATASOURCE_USERNAME=distrischool
export SPRING_DATASOURCE_PASSWORD=distrischool123
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

### 2. Executar com Docker

```bash
# Subir todos os serviços
docker-compose up -d

# Verificar logs
docker-compose logs -f
```

> Prometheus estará disponível em `http://localhost:9090` e Grafana em `http://localhost:3001`.

### 2.1 Monitoramento Local

- **Prometheus**: já configurado para coletar `microservice-auth-dev` em `/actuator/prometheus`. A UI web permite explorar as métricas `auth_operations_total`, `auth_role_assignments_total` e `auth_events_total`.
- **Grafana**: datasource Prometheus provisionado automaticamente (login padrão `admin` / `admin`, configurável via `GRAFANA_ADMIN_USER` e `GRAFANA_ADMIN_PASSWORD`).
- **Alertas/Dashboards**: importe dashboards do Grafana Labs ou crie painéis personalizados para acompanhar autenticações, criação de usuários e falhas em eventos Kafka. Exemplos de consultas úteis:
  - **Requisições por segundo (RPS)**: `rate(http_server_requests_seconds_count{uri!~"/actuator/.*"}[1m])`
  - **Latência P95**: `histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m]))`
  - **Tempo médio por endpoint de login**: `rate(auth_login_seconds_sum[5m]) / rate(auth_login_seconds_count[5m])`
  - **Distribuição de status HTTP**: `sum(rate(http_server_requests_seconds_count[5m])) by (status)`

### 3. Desenvolvimento

```bash
# Executar aplicação
./mvnw spring-boot:run

# Executar testes
./mvnw test

# Build
./mvnw clean package
```

## 📡 Endpoints Disponíveis

### Health Check
- `GET /api/v1/health` - Status do serviço
- `GET /api/v1/health/info` - Informações do serviço

### Actuator
- `GET /actuator/health` - Health check detalhado
- `GET /actuator/info` - Informações da aplicação
- `GET /actuator/metrics` - Métricas Prometheus

## 🔧 Configuração

### application.yml
O arquivo de configuração está otimizado para:
- PostgreSQL com pool de conexões
- Redis para cache
- Kafka para mensageria
- Prometheus para métricas
- Resilience4j para circuit breaker

### Variáveis de Ambiente
- `SPRING_DATASOURCE_URL` - URL do PostgreSQL
- `SPRING_DATASOURCE_USERNAME` - Usuário do banco
- `SPRING_DATASOURCE_PASSWORD` - Senha do banco
- `KAFKA_BOOTSTRAP_SERVERS` - Servidores Kafka
- `SERVER_PORT` - Porta da aplicação

## 🚀 Próximos Passos

1. **Criar Entidades**: Estender `BaseEntity` para suas entidades específicas
2. **Implementar Controllers**: Criar endpoints REST seguindo o padrão do `HealthController`
3. **Configurar Kafka**: Definir tópicos específicos no `application.yml`
4. **Implementar Serviços**: Criar lógica de negócio nos services
5. **Adicionar Testes**: Implementar testes unitários e de integração

## 📚 Documentação Adicional

- [Spring Boot Reference](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)
- [Spring Kafka Documentation](https://docs.spring.io/spring-kafka/docs/current/reference/html/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Redis Documentation](https://redis.io/documentation)
- [Kafka Documentation](https://kafka.apache.org/documentation/)

## 🤝 Contribuição

Este template é baseado nos requisitos do DistriSchool e deve ser mantido atualizado conforme a evolução do projeto.