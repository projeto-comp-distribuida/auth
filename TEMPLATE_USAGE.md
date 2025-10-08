# 📖 Guia de Uso do Template

Este documento fornece instruções detalhadas sobre como usar este template para criar novos microserviços.

## 🎯 Objetivo

Este template foi criado para acelerar o desenvolvimento de novos microserviços no projeto DistriSchool, fornecendo:

- Estrutura base padronizada
- Configurações pré-definidas
- Exemplos de implementação
- Scripts de deploy automatizados

## 🚀 Início Rápido

### Passo 1: Usar o Template

No GitHub, clique em "Use this template" ou:

```bash
git clone https://github.com/seu-usuario/microservice-template.git meu-novo-servico
cd meu-novo-servico
rm -rf .git
git init
git add .
git commit -m "Initial commit from template"
```

### Passo 2: Renomear o Projeto

#### 2.1. Atualizar `pom.xml`

```xml
<groupId>com.distrischool</groupId>
<artifactId>meu-novo-servico</artifactId>
<version>1.0.0</version>
<name>Meu Novo Serviço</name>
<description>Descrição do meu serviço</description>
```

#### 2.2. Renomear Pacotes

Renomeie os pacotes de `com.distrischool.template` para `com.distrischool.meunovovervico`:

```bash
# Linux/Mac
find src -type f -name "*.java" -exec sed -i 's/com.distrischool.template/com.distrischool.meunovoservico/g' {} +

# Renomear diretórios
mv src/main/java/com/distrischool/template src/main/java/com/distrischool/meunovoservico
mv src/test/java/com/distrischool/template src/test/java/com/distrischool/meunovoservico
```

#### 2.3. Atualizar `application.yml`

```yaml
spring:
  application:
    name: meu-novo-servico

microservice:
  name: meu-novo-servico
  kafka:
    topics:
      # Defina seus tópicos aqui
      example-event: meu-novo-servico.example.event
```

#### 2.4. Atualizar Arquivos Docker e Kubernetes

**docker-compose.yml:**
```yaml
services:
  meu-novo-servico:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: meu-novo-servico
    # ... resto da configuração
```

**k8s/deployment.yaml:**
```yaml
metadata:
  name: meu-novo-servico
  labels:
    app: meu-novo-servico
```

**k8s/service.yaml:**
```yaml
metadata:
  name: meu-novo-servico-service
  labels:
    app: meu-novo-servico
```

### Passo 3: Implementar Funcionalidades

#### 3.1. Criar Seus DTOs

Renomeie/copie `ExampleDTO.java` para seus próprios DTOs:

```java
package com.distrischool.meunovoservico.dto;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeuDTO {
    private String id;
    // Seus campos aqui
}
```

#### 3.2. Criar Seus Controllers

```java
package com.distrischool.meunovoservico.controller;

@RestController
@RequestMapping("/api/v1/meus-recursos")
public class MeuController {
    // Seus endpoints aqui
}
```

#### 3.3. Implementar Services

```java
package com.distrischool.meunovoservico.service;

@Service
public class MeuService {
    // Sua lógica de negócio aqui
}
```

#### 3.4. Configurar Kafka

**Definir seus eventos:**
```java
package com.distrischool.meunovoservico.dto.event;

@Data
@Builder
public class MeuEvento {
    private String eventId;
    private String eventType;
    // Seus campos aqui
}
```

**Publicar eventos:**
```java
@Service
public class MeuService {
    private final EventProducer eventProducer;
    
    public void criarRecurso() {
        // Sua lógica
        eventProducer.sendEventToTopic("meu-topico", evento);
    }
}
```

**Consumir eventos:**
```java
@Component
public class MeuConsumer {
    @KafkaListener(topics = "meu-topico", groupId = "meu-grupo")
    public void consumir(MeuEvento evento) {
        // Processar evento
    }
}
```

#### 3.5. Adicionar Feign Clients

Se precisar comunicar com outros serviços:

```java
@FeignClient(
    name = "outro-servico",
    url = "${feign.client.outro-servico.url}"
)
public interface OutroServicoClient {
    @GetMapping("/api/v1/recurso")
    ApiResponse<RecursoDTO> getRecurso();
}
```

## 🧪 Testes

### Testar Localmente

```bash
# Build e testes
./scripts/local-build.sh

# Executar
mvn spring-boot:run
```

### Testar com Docker

```bash
# Subir stack completo
docker-compose up -d

# Ver logs
docker-compose logs -f meu-novo-servico

# Testar endpoint
curl http://localhost:8080/actuator/health
```

### Testar no Kubernetes

```bash
# Deploy
./scripts/deploy-minikube.sh

# Verificar
kubectl get pods -n distrischool
kubectl logs -f deployment/meu-novo-servico -n distrischool
```

## 🔄 Integração com Outros Serviços

### Comunicação Síncrona (Feign)

1. Adicione o Feign Client
2. Configure a URL no `application.yml`
3. Injete o client no seu service
4. Use-o como uma interface normal

```java
@Service
public class MeuService {
    private final OutroServicoClient outroServicoClient;
    
    public void minhaLogica() {
        ApiResponse<RecursoDTO> resposta = outroServicoClient.getRecurso();
        // Processar resposta
    }
}
```

### Comunicação Assíncrona (Kafka)

1. Defina o tópico no `application.yml`
2. Configure o tópico no `KafkaConfig`
3. Publique eventos com `EventProducer`
4. Consuma eventos com `@KafkaListener`

## 📦 Deploy em Produção

### Azure Kubernetes Service (AKS)

```bash
# Login no Azure
az login

# Configurar kubectl para AKS
az aks get-credentials --resource-group distrischool-rg --name distrischool-cluster

# Build e push da imagem
docker build -t distrischool.azurecr.io/meu-novo-servico:1.0.0 .
docker push distrischool.azurecr.io/meu-novo-servico:1.0.0

# Aplicar manifestos
kubectl apply -f k8s/
```

## 🎨 Personalização Avançada

### Adicionar Banco de Dados

1. Adicione a dependência no `pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
</dependency>
```

2. Configure no `application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/meudb
    username: user
    password: pass
  jpa:
    hibernate:
      ddl-auto: validate
```

3. Adicione PostgreSQL no `docker-compose.yml`

### Adicionar Redis

1. Adicione a dependência:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

2. Configure:
```yaml
spring:
  redis:
    host: localhost
    port: 6379
```

### Adicionar Autenticação JWT

1. Adicione Spring Security
2. Configure JWT filter
3. Proteja seus endpoints

## ✅ Checklist de Implementação

Antes de considerar seu serviço pronto:

- [ ] Todos os pacotes renomeados
- [ ] `application.yml` configurado
- [ ] Endpoints implementados
- [ ] Testes escritos e passando
- [ ] Kafka configurado (se necessário)
- [ ] Feign clients configurados (se necessário)
- [ ] Dockerfile funcionando
- [ ] docker-compose.yml testado
- [ ] Manifestos K8s atualizados
- [ ] Health checks funcionando
- [ ] Logs adequados
- [ ] README atualizado
- [ ] Documentação dos endpoints
- [ ] Métricas configuradas

## 🐛 Troubleshooting

### Problema: Kafka não conecta

**Solução:**
- Verifique se o Kafka está rodando
- Verifique o `KAFKA_BOOTSTRAP_SERVERS` no `application.yml`
- Para Docker: use `kafka:29092`
- Para K8s: use `kafka-service:9092`

### Problema: Feign Client retorna 404

**Solução:**
- Verifique a URL configurada
- Verifique se o serviço de destino está rodando
- Verifique os logs do fallback

### Problema: Pod não inicia no K8s

**Solução:**
```bash
# Ver logs
kubectl logs -f pod/meu-pod -n distrischool

# Descrever pod
kubectl describe pod meu-pod -n distrischool

# Verificar eventos
kubectl get events -n distrischool --sort-by='.lastTimestamp'
```

## 📚 Recursos Adicionais

- Documentação Spring Boot
- Documentação Kafka
- Documentação Kubernetes
- Exemplos no código

---

**Boa codificação! 🚀**

