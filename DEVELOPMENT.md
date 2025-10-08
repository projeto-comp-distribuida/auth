# Guia de Desenvolvimento

Este documento descreve as diferentes opções de ambiente de desenvolvimento disponíveis para o microserviço.

> **⚠️ Importante:** Docker Compose é usado **exclusivamente para desenvolvimento local**. Em produção, o sistema roda em Kubernetes (Azure AKS).

## 🚀 Opções de Desenvolvimento

### 1. Docker Compose com Hot Reloading (Recomendado) ⚡

**Melhor para:** Desenvolvimento diário, iteração rápida de código

**Vantagens:**
- ✅ Hot reloading automático
- ✅ Mudanças no código refletidas instantaneamente
- ✅ Debug remoto disponível na porta 5005
- ✅ LiveReload para recarregar o navegador automaticamente
- ✅ Mais rápido para iteração de código
- ✅ Não requer Kubernetes/Minikube
- ✅ Ambiente completo (Kafka, Zookeeper, Kafka UI)

**Como usar:**

```bash
# Opção 1: Usar o script auxiliar (recomendado)
./scripts/dev-docker.sh

# Opção 2: Comando direto
docker-compose -f docker-compose-dev.yml up --build

# Para executar em background
docker-compose -f docker-compose-dev.yml up -d --build

# Para parar
docker-compose -f docker-compose-dev.yml down
```

**Portas expostas:**
- `8080` - API do microserviço
- `5005` - Debug remoto (Java)
- `35729` - LiveReload
- `8090` - Kafka UI
- `9092` - Kafka (host)

**Volumes montados:**
- `./src` → `/app/src` - Código fonte (hot reloading)
- `./pom.xml` → `/app/pom.xml` - Dependências Maven
- Volume `maven-cache` - Cache de dependências Maven

### 2. Kubernetes/Minikube (Para Testes de Orquestração)

**Melhor para:** Testar manifests Kubernetes, configurações de deployment, probes, e comportamento em ambiente orquestrado

**Vantagens:**
- ✅ Ambiente idêntico à produção (AKS)
- ✅ Testa orquestração Kubernetes
- ✅ Testa escalabilidade e resiliência
- ✅ Probes de health/readiness configurados
- ✅ Validação de ConfigMaps, Services, etc.

**Desvantagens:**
- ❌ Sem hot reloading (requer rebuild)
- ❌ Mais lento para iteração de código
- ❌ Requer mais recursos (CPU/RAM)

**Como usar:**

```bash
# Usar o script auxiliar
./scripts/dev-simple.sh

# Ou manualmente
minikube start --cpus=4 --memory=3072
eval $(minikube docker-env)
docker build -t microservice-template-dev:latest .
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/kafka-deployment.yaml
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/deployment-dev.yaml
kubectl apply -f k8s/service-dev.yaml
```

**Comandos úteis:**
```bash
# Ver logs
kubectl logs -f deployment/microservice-template-dev -n distrischool

# Acessar serviço
minikube service microservice-template-dev-nodeport -n distrischool

# Reiniciar pod (após mudanças no código)
kubectl rollout restart deployment/microservice-template-dev -n distrischool

# Parar
kubectl delete deployment microservice-template-dev -n distrischool
```

### 3. Docker Compose Simples (Alternativa sem Hot Reloading)

Use o `docker-compose.yml` padrão para um ambiente mais leve:

```bash
docker-compose up --build
```

Este ambiente:
- ❌ **Não** tem hot reloading
- ✅ Usa a imagem otimizada (multi-stage build)
- ✅ Consome menos recursos
- ✅ Mais próximo do runtime de produção

> **Nota:** Produção roda em Kubernetes (AKS), não em Docker Compose.

## 🔧 Configuração de Debug Remoto

### IntelliJ IDEA

1. Run → Edit Configurations
2. Add New Configuration → Remote JVM Debug
3. Host: `localhost`
4. Port: `5005`
5. Debugger mode: Attach to remote JVM
6. Start debugging

### VS Code

Adicione ao `.vscode/launch.json`:

```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "java",
      "name": "Debug (Attach) - Docker",
      "request": "attach",
      "hostName": "localhost",
      "port": 5005
    }
  ]
}
```

## 🔄 Como Funciona o Hot Reloading

O hot reloading funciona através de:

1. **Spring Boot DevTools**: Monitora mudanças nas classes Java
2. **Volume Mount**: Sincroniza arquivos entre host e container
3. **Maven Spring Boot Plugin**: Recompila e reinicia automaticamente

**O que aciona o reload:**
- ✅ Mudanças em arquivos `.java`
- ✅ Mudanças em arquivos `.properties` / `.yml`
- ✅ Mudanças em recursos estáticos
- ❌ Mudanças no `pom.xml` (requer restart manual)

## 📊 Monitoramento

### Kafka UI
Acesse: http://localhost:8090

Visualize tópicos, mensagens e consumidores do Kafka.

### Actuator Endpoints
Acesse: http://localhost:8080/actuator

Endpoints disponíveis:
- `/actuator/health` - Status de saúde
- `/actuator/health/liveness` - Liveness probe
- `/actuator/health/readiness` - Readiness probe
- `/actuator/metrics` - Métricas da aplicação
- `/actuator/prometheus` - Métricas no formato Prometheus

## 🧪 Fluxo de Desenvolvimento Recomendado

### Desenvolvimento Diário (90% do tempo)
```bash
./scripts/dev-docker.sh
```
- Codifique e veja as mudanças instantaneamente
- Use debug remoto quando necessário
- Ambiente completo com Kafka e todas as dependências

### Antes de Fazer PR/Deploy
```bash
./scripts/dev-simple.sh
```
- Valide manifests Kubernetes funcionam corretamente
- Teste probes de liveness/readiness
- Confirme que a aplicação inicia corretamente no K8s
- Simule cenários de falha e recuperação

### Pipeline CI/CD → Produção (AKS)
- O pipeline construirá a imagem otimizada (`Dockerfile`)
- Deploy automático no Azure Kubernetes Service
- **Não usa Docker Compose em produção**

## 🐛 Troubleshooting

### Hot reloading não funciona

```bash
# Verifique se os volumes estão montados corretamente
docker-compose -f docker-compose-dev.yml ps
docker exec microservice-template-dev ls -la /app/src

# Reinicie o container
docker-compose -f docker-compose-dev.yml restart microservice-template-dev
```

### Erro de conexão com Kafka

```bash
# Verifique se o Kafka está saudável
docker-compose -f docker-compose-dev.yml ps kafka

# Veja os logs do Kafka
docker-compose -f docker-compose-dev.yml logs kafka
```

### Build Maven lento

O cache Maven está configurado, mas na primeira execução baixará todas as dependências. Nas próximas execuções será mais rápido.

### Mudanças no pom.xml não são aplicadas

Mudanças no `pom.xml` requerem rebuild:

```bash
docker-compose -f docker-compose-dev.yml down
docker-compose -f docker-compose-dev.yml up --build
```

## 📝 Notas Importantes

- O hot reloading adiciona overhead de memória (~200MB). Use apenas em desenvolvimento.
- O Spring DevTools está configurado para **não** ser incluído em builds de produção.
- O cache Maven é persistido em um volume para acelerar builds subsequentes.
- Para desenvolvimento intenso, recomenda-se pelo menos 4GB de RAM disponível para Docker.

