#!/bin/bash

# Script simples para desenvolvimento usando kubectl
# Alternativa mais simples ao Skaffold/Tilt

set -e

# Muda para o diretório raiz do projeto
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
cd "$PROJECT_ROOT"

echo "================================================"
echo "Desenvolvimento Simples no Kubernetes"
echo "================================================"
echo "Diretório do projeto: $PROJECT_ROOT"

# Verifica se o Minikube está rodando
if ! minikube status > /dev/null 2>&1; then
    echo "❌ Minikube não está rodando!"
    echo "Iniciando Minikube..."
    minikube start --cpus=4 --memory=3072
fi

echo "✅ Minikube está rodando"

# Configura o Docker para usar o daemon do Minikube
echo "Configurando Docker para usar o daemon do Minikube..."
eval $(minikube docker-env)

# Para qualquer deployment anterior
echo "Parando deployments anteriores..."
kubectl delete deployment microservice-template-dev -n distrischool 2>/dev/null || true
kubectl delete deployment microservice-template -n distrischool 2>/dev/null || true
kubectl delete service microservice-template-dev-service -n distrischool 2>/dev/null || true
kubectl delete service microservice-template-dev-nodeport -n distrischool 2>/dev/null || true
kubectl delete service microservice-template-service -n distrischool 2>/dev/null || true
kubectl delete service microservice-template-nodeport -n distrischool 2>/dev/null || true

# Aplica os manifestos básicos
echo "Aplicando manifestos básicos..."
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/kafka-deployment.yaml
kubectl apply -f k8s/configmap.yaml

# Aguarda o Kafka estar pronto
echo "Aguardando Kafka estar pronto..."
kubectl wait --namespace=distrischool \
  --for=condition=ready pod \
  --selector=app=kafka \
  --timeout=300s

echo "✅ Kafka está pronto"

# Build da imagem padrão
echo "Construindo imagem para Kubernetes..."
docker build -t microservice-template-dev:latest .

echo "✅ Imagem construída"

# Aplica o deployment de desenvolvimento
echo "Aplicando deployment de desenvolvimento..."
kubectl apply -f k8s/deployment-dev.yaml
kubectl apply -f k8s/service-dev.yaml

# Aguarda o pod estar pronto
echo "Aguardando pod de desenvolvimento estar pronto..."
kubectl wait --namespace=distrischool \
  --for=condition=ready pod \
  --selector=app=microservice-template-dev \
  --timeout=120s

echo "✅ Pod de desenvolvimento está pronto"

echo "================================================"
echo "Desenvolvimento iniciado com sucesso!"
echo "================================================"
echo ""
echo "Comandos úteis:"
echo "  - Ver logs: kubectl logs -f deployment/microservice-template-dev -n distrischool"
echo "  - Acessar serviço: minikube service microservice-template-dev-nodeport -n distrischool"
echo "  - Reiniciar pod: kubectl rollout restart deployment/microservice-template-dev -n distrischool"
echo ""
echo "💡 Para desenvolvimento com hot reloading, use Docker Compose:"
echo "   cd $PROJECT_ROOT && docker-compose -f docker-compose-dev.yml up"
echo ""
echo "Para parar o desenvolvimento:"
echo "  kubectl delete deployment microservice-template-dev -n distrischool"
echo "================================================"
