#!/bin/bash

# Script para gerenciar portas e resolver conflitos
# Versão: 1.0.0

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

# Função para mostrar informações sobre uma porta
show_port_info() {
    local port="$1"
    local info=$(lsof -i :"$port" 2>/dev/null || echo "Porta livre")
    
    if [ "$info" = "Porta livre" ]; then
        echo -e "  ${GREEN}Porta $port: Livre${NC}"
    else
        echo -e "  ${RED}Porta $port: Em uso${NC}"
        echo "$info" | while read line; do
            echo "    $line"
        done
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
    local force="$2"
    local pids=$(lsof -ti :"$port" 2>/dev/null || true)
    
    if [ -n "$pids" ]; then
        log_warning "Processo(s) usando porta $port: $pids"
        
        if [ "$force" = "true" ]; then
            echo "$pids" | xargs kill -9 2>/dev/null || true
            log_success "Processo(s) finalizado(s) forçadamente"
            return 0
        else
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
    fi
    return 0
}

# Função para liberar todas as portas do projeto
free_all_ports() {
    local ports=(8080 5005 35729 9092 2181 8090)
    local force="$1"
    
    log "Liberando todas as portas do projeto..."
    
    for port in "${ports[@]}"; do
        if check_port "$port"; then
            kill_process_on_port "$port" "$force"
        fi
    done
    
    log_success "Verificação de portas concluída"
}

# Função para mostrar status de todas as portas
show_status() {
    local ports=(8080 5005 35729 9092 2181 8090)
    
    echo -e "${PURPLE}================================================${NC}"
    echo -e "${PURPLE}Status das Portas do DistriSchool${NC}"
    echo -e "${PURPLE}================================================${NC}"
    echo ""
    
    for port in "${ports[@]}"; do
        show_port_info "$port"
    done
    
    echo ""
}

# Função para encontrar portas livres alternativas
find_alternative_ports() {
    local ports=(8080 5005 35729 9092 2181 8090)
    
    echo -e "${PURPLE}================================================${NC}"
    echo -e "${PURPLE}Portas Alternativas Disponíveis${NC}"
    echo -e "${PURPLE}================================================${NC}"
    echo ""
    
    for port in "${ports[@]}"; do
        if check_port "$port"; then
            local alt_port=$(find_free_port "$port")
            echo -e "  ${RED}Porta $port: Em uso${NC} → ${GREEN}Alternativa: $alt_port${NC}"
        else
            echo -e "  ${GREEN}Porta $port: Livre${NC}"
        fi
    done
    
    echo ""
}

# Menu principal
show_menu() {
    echo -e "${PURPLE}================================================${NC}"
    echo -e "${PURPLE}Gerenciador de Portas - DistriSchool${NC}"
    echo -e "${PURPLE}================================================${NC}"
    echo ""
    echo "1. Mostrar status das portas"
    echo "2. Liberar todas as portas (com confirmação)"
    echo "3. Liberar todas as portas (forçado)"
    echo "4. Encontrar portas alternativas"
    echo "5. Liberar porta específica"
    echo "6. Verificar porta específica"
    echo "0. Sair"
    echo ""
}

# Função principal
main() {
    case "${1:-menu}" in
        "status")
            show_status
            ;;
        "free")
            free_all_ports "false"
            ;;
        "force")
            free_all_ports "true"
            ;;
        "alternatives")
            find_alternative_ports
            ;;
        "kill")
            if [ -z "$2" ]; then
                log_error "Por favor, especifique uma porta"
                echo "Uso: $0 kill <porta>"
                exit 1
            fi
            kill_process_on_port "$2" "false"
            ;;
        "check")
            if [ -z "$2" ]; then
                log_error "Por favor, especifique uma porta"
                echo "Uso: $0 check <porta>"
                exit 1
            fi
            show_port_info "$2"
            ;;
        "menu"|*)
            while true; do
                show_menu
                read -p "Escolha uma opção: " choice
                echo ""
                
                case $choice in
                    1)
                        show_status
                        ;;
                    2)
                        free_all_ports "false"
                        ;;
                    3)
                        free_all_ports "true"
                        ;;
                    4)
                        find_alternative_ports
                        ;;
                    5)
                        read -p "Digite a porta a ser liberada: " port
                        kill_process_on_port "$port" "false"
                        ;;
                    6)
                        read -p "Digite a porta a ser verificada: " port
                        show_port_info "$port"
                        ;;
                    0)
                        log_info "Saindo..."
                        exit 0
                        ;;
                    *)
                        log_error "Opção inválida!"
                        ;;
                esac
                
                echo ""
                read -p "Pressione Enter para continuar..."
                echo ""
            done
            ;;
    esac
}

# Executa a função principal
main "$@"
