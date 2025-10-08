#!/bin/bash

# Script para build local do projeto
set -e

# Muda para o diretório raiz do projeto
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
cd "$PROJECT_ROOT"

echo "================================================"
echo "Build Local do Microserviço Template"
echo "================================================"

# Verifica se o Maven está instalado
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven não está instalado!"
    exit 1
fi

echo "Executando testes..."
mvn clean test

echo "Construindo aplicação..."
mvn clean package -DskipTests

echo "✅ Build concluído com sucesso!"
echo "JAR criado em: target/microservice-template-1.0.0.jar"

