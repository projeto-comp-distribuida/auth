#!/bin/bash

# Script para inicializar um novo projeto a partir do template
# Este script renomeia automaticamente o template para seu novo serviço
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

# Função para validar nome do serviço
validate_service_name() {
    local name="$1"
    
    # Verifica se não está vazio
    if [ -z "$name" ]; then
        log_error "Nome do serviço não pode estar vazio!"
        return 1
    fi
    
    # Verifica se contém apenas caracteres válidos (letras, números, hífens)
    if ! [[ "$name" =~ ^[a-zA-Z0-9-]+$ ]]; then
        log_error "Nome do serviço deve conter apenas letras, números e hífens!"
        return 1
    fi
    
    # Verifica se não começa ou termina com hífen
    if [[ "$name" =~ ^- ]] || [[ "$name" =~ -$ ]]; then
        log_error "Nome do serviço não pode começar ou terminar com hífen!"
        return 1
    fi
    
    # Verifica se não contém hífens consecutivos
    if [[ "$name" =~ -- ]]; then
        log_error "Nome do serviço não pode conter hífens consecutivos!"
        return 1
    fi
    
    return 0
}

# Função para verificar se arquivos necessários existem
check_required_files() {
    local missing_files=()
    
    local required_files=(
        "pom.xml"
        "src/main/resources/application.yml"
        "src/main/resources/application-docker.yml"
        "src/main/resources/application-kubernetes.yml"
        "src/test/resources/application-test.yml"
        "src/main/java/com/distrischool/template/TemplateApplication.java"
        "src/test/java/com/distrischool/template/TemplateApplicationTests.java"
        "docker-compose.yml"
        "README.md"
    )
    
    for file in "${required_files[@]}"; do
        if [ ! -f "$file" ]; then
            missing_files+=("$file")
        fi
    done
    
    if [ ${#missing_files[@]} -gt 0 ]; then
        log_error "Arquivos necessários não encontrados:"
        for file in "${missing_files[@]}"; do
            echo "  - $file"
        done
        log_error "Execute este script a partir do diretório raiz do template!"
        return 1
    fi
    
    return 0
}

# Função para criar backup
create_backup() {
    local backup_dir="backup_$(date +%Y%m%d_%H%M%S)"
    log_info "Criando backup em: $backup_dir"
    
    mkdir -p "$backup_dir"
    
    # Copia arquivos importantes
    cp -r src "$backup_dir/"
    cp pom.xml "$backup_dir/"
    cp docker-compose.yml "$backup_dir/"
    cp README.md "$backup_dir/"
    cp -r k8s "$backup_dir/" 2>/dev/null || true
    cp -r scripts "$backup_dir/" 2>/dev/null || true
    
    echo "$backup_dir"
}

# Função para rollback
rollback() {
    local backup_dir="$1"
    if [ -n "$backup_dir" ] && [ -d "$backup_dir" ]; then
        log_warning "Executando rollback..."
        cp -r "$backup_dir"/* .
        log_success "Rollback concluído!"
    fi
}

echo -e "${PURPLE}================================================${NC}"
echo -e "${PURPLE}   Inicializador de Projeto DistriSchool v2.0${NC}"
echo -e "${PURPLE}================================================${NC}"
echo ""

# Verifica se está no diretório correto
if ! check_required_files; then
    exit 1
fi

log_success "Arquivos do template verificados com sucesso!"

# Solicita o nome do novo serviço
while true; do
    read -p "Digite o nome do novo serviço (ex: student-service): " SERVICE_NAME
    
    if validate_service_name "$SERVICE_NAME"; then
        break
    fi
    echo ""
done

# Converte para diferentes formatos
SERVICE_NAME_LOWER=$(echo "$SERVICE_NAME" | tr '[:upper:]' '[:lower:]' | tr '-' '_')
SERVICE_NAME_CAMEL=$(echo "$SERVICE_NAME" | sed -r 's/(^|-)([a-z])/\U\2/g' | sed 's/-//g')
PACKAGE_NAME=$(echo "$SERVICE_NAME" | tr '[:upper:]' '[:lower:]' | tr '-' '')

echo ""
log_info "Configuração do serviço:"
echo -e "  ${CYAN}Nome do serviço:${NC} $SERVICE_NAME"
echo -e "  ${CYAN}Package:${NC} com.distrischool.$PACKAGE_NAME"
echo -e "  ${CYAN}Classe principal:${NC} ${SERVICE_NAME_CAMEL}Application"
echo -e "  ${CYAN}Nome em snake_case:${NC} $SERVICE_NAME_LOWER"
echo ""

# Pergunta sobre backup
read -p "Deseja criar um backup antes das modificações? (y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    BACKUP_DIR=$(create_backup)
    log_success "Backup criado em: $BACKUP_DIR"
else
    log_warning "Backup não será criado. Continuando..."
fi

read -p "Confirma a criação do serviço com estas configurações? (y/n) " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    log_warning "Operação cancelada."
    exit 1
fi

echo ""
log "Iniciando configuração do projeto..."

# Função para executar comando com tratamento de erro
execute_with_error_handling() {
    local description="$1"
    local command="$2"
    
    log "Executando: $description"
    if eval "$command"; then
        log_success "$description concluído"
    else
        log_error "Falha ao executar: $description"
        if [ -n "$BACKUP_DIR" ]; then
            log_warning "Executando rollback..."
            rollback "$BACKUP_DIR"
        fi
        exit 1
    fi
}

# 1. Atualizar pom.xml
execute_with_error_handling "Atualização do pom.xml" "
    sed -i 's/<artifactId>microservice-template<\/artifactId>/<artifactId>$SERVICE_NAME<\/artifactId>/g' pom.xml &&
    sed -i 's/<name>DistriSchool Microservice Template<\/name>/<name>DistriSchool $SERVICE_NAME_CAMEL<\/name>/g' pom.xml &&
    sed -i 's/<description>Template base para microserviços do DistriSchool<\/description>/<description>$SERVICE_NAME_CAMEL service for DistriSchool<\/description>/g' pom.xml
"

# 2. Atualizar application.yml
execute_with_error_handling "Atualização do application.yml" "
    sed -i 's/name: microservice-template/name: $SERVICE_NAME/g' src/main/resources/application.yml &&
    sed -i 's/microservice-template/$SERVICE_NAME/g' src/main/resources/application.yml &&
    sed -i 's/name: \${spring.application.name}/name: $SERVICE_NAME/g' src/main/resources/application.yml
"

# 3. Atualizar application-docker.yml
execute_with_error_handling "Atualização do application-docker.yml" "
    sed -i 's/name: microservice-template/name: $SERVICE_NAME/g' src/main/resources/application-docker.yml
"

# 4. Atualizar application-kubernetes.yml
execute_with_error_handling "Atualização do application-kubernetes.yml" "
    sed -i 's/name: microservice-template/name: $SERVICE_NAME/g' src/main/resources/application-kubernetes.yml
"

# 5. Atualizar application-test.yml
execute_with_error_handling "Atualização do application-test.yml" "
    sed -i 's/name: microservice-template-test/name: $SERVICE_NAME-test/g' src/test/resources/application-test.yml
"

# 6. Renomear pacotes Java
execute_with_error_handling "Renomeação de pacotes Java" "
    find src -type f -name '*.java' -exec sed -i 's/com.distrischool.template/com.distrischool.$PACKAGE_NAME/g' {} +
"

# 7. Renomear classe principal
execute_with_error_handling "Renomeação da classe principal" "
    sed -i 's/TemplateApplication/${SERVICE_NAME_CAMEL}Application/g' src/main/java/com/distrischool/template/TemplateApplication.java &&
    mv src/main/java/com/distrischool/template/TemplateApplication.java src/main/java/com/distrischool/template/${SERVICE_NAME_CAMEL}Application.java
"

execute_with_error_handling "Renomeação da classe de teste" "
    sed -i 's/TemplateApplicationTests/${SERVICE_NAME_CAMEL}ApplicationTests/g' src/test/java/com/distrischool/template/TemplateApplicationTests.java &&
    mv src/test/java/com/distrischool/template/TemplateApplicationTests.java src/test/java/com/distrischool/template/${SERVICE_NAME_CAMEL}ApplicationTests.java
"

# 8. Renomear diretórios de pacotes
execute_with_error_handling "Renomeação de diretórios" "
    mv src/main/java/com/distrischool/template src/main/java/com/distrischool/$PACKAGE_NAME &&
    mv src/test/java/com/distrischool/template src/test/java/com/distrischool/$PACKAGE_NAME
"

# 9. Atualizar docker-compose.yml
execute_with_error_handling "Atualização do docker-compose.yml" "
    sed -i 's/microservice-template/$SERVICE_NAME/g' docker-compose.yml
"

# 10. Atualizar manifestos Kubernetes
execute_with_error_handling "Atualização de manifestos Kubernetes" "
    find k8s -type f -name '*.yaml' -exec sed -i 's/microservice-template/$SERVICE_NAME/g' {} +
"

# 11. Atualizar scripts
execute_with_error_handling "Atualização de scripts" "
    find scripts -type f -name '*.sh' -exec sed -i 's/microservice-template/$SERVICE_NAME/g' {} +
"

# 12. Atualizar README
execute_with_error_handling "Atualização do README.md" "
    sed -i 's/microservice-template/$SERVICE_NAME/g' README.md &&
    sed -i 's/DistriSchool Microservice Template/DistriSchool $SERVICE_NAME_CAMEL/g' README.md
"

# Verificação final
log "Verificando integridade do projeto..."

# Verifica se os arquivos principais foram criados corretamente
if [ ! -f "src/main/java/com/distrischool/$PACKAGE_NAME/${SERVICE_NAME_CAMEL}Application.java" ]; then
    log_error "Classe principal não foi criada corretamente!"
    if [ -n "$BACKUP_DIR" ]; then
        rollback "$BACKUP_DIR"
    fi
    exit 1
fi

if [ ! -f "src/test/java/com/distrischool/$PACKAGE_NAME/${SERVICE_NAME_CAMEL}ApplicationTests.java" ]; then
    log_error "Classe de teste não foi criada corretamente!"
    if [ -n "$BACKUP_DIR" ]; then
        rollback "$BACKUP_DIR"
    fi
    exit 1
fi

log_success "Verificação de integridade concluída!"

# Limpeza do backup se tudo correu bem
if [ -n "$BACKUP_DIR" ]; then
    read -p "Deseja manter o backup criado? (y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        log_info "Removendo backup..."
        rm -rf "$BACKUP_DIR"
        log_success "Backup removido!"
    else
        log_info "Backup mantido em: $BACKUP_DIR"
    fi
fi

echo ""
log_success "Projeto configurado com sucesso!"
echo ""
echo -e "${PURPLE}================================================${NC}"
echo -e "${PURPLE}Próximos Passos:${NC}"
echo -e "${PURPLE}================================================${NC}"
echo ""
echo -e "${CYAN}1. Revise as mudanças:${NC}"
echo "   git status"
echo "   git diff"
echo ""
echo -e "${CYAN}2. Teste o build:${NC}"
echo "   ./mvnw clean package"
echo ""
echo -e "${CYAN}3. Execute o serviço:${NC}"
echo "   ./mvnw spring-boot:run"
echo "   # ou"
echo "   docker-compose up -d"
echo ""
echo -e "${CYAN}4. Implemente suas funcionalidades:${NC}"
echo "   - Controllers: src/main/java/com/distrischool/$PACKAGE_NAME/controller/"
echo "   - Services: src/main/java/com/distrischool/$PACKAGE_NAME/service/"
echo "   - DTOs: src/main/java/com/distrischool/$PACKAGE_NAME/dto/"
echo "   - Configurações: src/main/java/com/distrischool/$PACKAGE_NAME/config/"
echo "   - Kafka: src/main/java/com/distrischool/$PACKAGE_NAME/kafka/"
echo ""
echo -e "${CYAN}5. Configure tópicos Kafka:${NC}"
echo "   Edite src/main/resources/application.yml"
echo "   Seção: microservice.kafka.topics"
echo ""
echo -e "${CYAN}6. Consulte a documentação:${NC}"
echo "   - README.md"
echo "   - DEVELOPMENT.md"
echo ""
echo -e "${CYAN}7. Teste a aplicação:${NC}"
echo "   curl http://localhost:8080/actuator/health"
echo ""
echo -e "${PURPLE}================================================${NC}"
echo ""
echo -e "${GREEN}Boa codificação! 🚀${NC}"
echo ""

