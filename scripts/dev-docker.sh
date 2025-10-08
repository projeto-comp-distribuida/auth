#!/bin/bash

# Script para desenvolvimento local com hot reloading usando Docker Compose
# Versão: 2.0.0

set -e

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Função para logging
log() {
    echo -e "${BLUE}[$(date +'%Y-%m-%d %H:%M:%S')]${NC} $1"
}

log_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

log_error() {
    echo -e "${RED}❌ $1${NC}"
}

log_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

log_info() {
    echo -e "${CYAN}ℹ️  $1${NC}"
}

# Função para verificar se uma porta está em uso
check_port() {
    local port="$1"
    if lsof -i :"$port" > /dev/null 2>&1; then
        return 0  # Porta em uso
    else
        return 1  # Porta livre
    fi
}

# Função para encontrar uma porta livre
find_free_port() {
    local start_port="$1"
    local port="$start_port"
    
    while check_port "$port"; do
        port=$((port + 1))
        if [ "$port" -gt $((start_port + 100)) ]; then
            log_error "Não foi possível encontrar uma porta livre próxima a $start_port"
            return 1
        fi
    done
    
    echo "$port"
}

# Função para matar processo usando uma porta
kill_process_on_port() {
    local port="$1"
    local pids=$(lsof -ti :"$port" 2>/dev/null || true)
    
    if [ -n "$pids" ]; then
        log_warning "Processo(s) usando porta $port: $pids"
        read -p "Deseja finalizar o(s) processo(s)? (y/n) " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            echo "$pids" | xargs kill -9 2>/dev/null || true
            log_success "Processo(s) finalizado(s)"
            return 0
        else
            return 1
        fi
    fi
    return 0
}

# Muda para o diretório raiz do projeto
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
cd "$PROJECT_ROOT"

echo -e "${PURPLE}================================================${NC}"
echo -e "${PURPLE}Desenvolvimento Local com Hot Reloading v2.0${NC}"
echo -e "${PURPLE}================================================${NC}"
echo -e "${CYAN}Diretório do projeto:${NC} $PROJECT_ROOT"
echo ""

# Verifica se o Docker está rodando
if ! docker info > /dev/null 2>&1; then
    log_error "Docker não está rodando!"
    echo "Por favor, inicie o Docker e tente novamente."
    exit 1
fi

log_success "Docker está rodando"

# Verifica se o docker-compose-dev.yml existe
if [ ! -f "docker-compose-dev.yml" ]; then
    log_error "Arquivo docker-compose-dev.yml não encontrado!"
    echo "Certifique-se de estar no diretório raiz do projeto."
    exit 1
fi

log_success "Arquivo docker-compose-dev.yml encontrado"

# Verifica conflitos de porta
log "Verificando conflitos de porta..."

PORTS_TO_CHECK=(8080 5005 35729 9092 2181 8090)
PORT_CONFLICTS=()

for port in "${PORTS_TO_CHECK[@]}"; do
    if check_port "$port"; then
        PORT_CONFLICTS+=("$port")
    fi
done

if [ ${#PORT_CONFLICTS[@]} -gt 0 ]; then
    log_warning "Conflitos de porta detectados:"
    for port in "${PORT_CONFLICTS[@]}"; do
        echo "  - Porta $port"
    done
    echo ""
    
    # Tenta resolver conflitos automaticamente
    for port in "${PORT_CONFLICTS[@]}"; do
        if ! kill_process_on_port "$port"; then
            log_error "Não foi possível resolver conflito na porta $port"
            echo ""
            echo "Opções:"
            echo "1. Finalize manualmente os processos usando essas portas"
            echo "2. Modifique as portas no docker-compose-dev.yml"
            echo "3. Use portas alternativas"
            exit 1
        fi
    done
fi

log_success "Verificação de portas concluída"

# Para containers anteriores se existirem
log "Parando containers anteriores (se existirem)..."
docker compose -f docker-compose-dev.yml down 2>/dev/null || true

echo ""
log "Iniciando ambiente de desenvolvimento..."
echo ""
log_info "Recursos disponíveis:"
echo "  🔄 Hot Reloading ativo!"
echo "  📝 Modifique arquivos em src/ e eles serão detectados automaticamente"
echo "  🔧 O Spring Boot DevTools reiniciará a aplicação automaticamente"
echo "  📦 Para mudanças em pom.xml, pare e reinicie o container"
echo ""
echo -e "${CYAN}URLs importantes:${NC}"
echo "  🌐 Aplicação: http://localhost:8080"
echo "  🔍 Health Check: http://localhost:8080/actuator/health"
echo "  📊 Kafka UI: http://localhost:8090"
echo "  🐛 Debug Remoto: localhost:5005"
echo ""
echo -e "${PURPLE}================================================${NC}"
echo ""

# Inicia os containers em modo attached para ver os logs
log "Iniciando containers..."
docker compose -f docker-compose-dev.yml up --build
