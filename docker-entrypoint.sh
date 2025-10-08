#!/bin/sh

# Script de entrada para desenvolvimento com hot reloading
# Monitora mudanças no código fonte e recompila automaticamente

echo "🚀 Iniciando DistriSchool Microservice Template com Hot Reloading..."

# Função para limpar processos em background ao sair
cleanup() {
    echo "🛑 Parando processos..."
    kill $WATCH_PID 2>/dev/null
    kill $SPRING_PID 2>/dev/null
    exit 0
}

# Configura trap para cleanup ao receber sinais de parada
trap cleanup SIGTERM SIGINT

# Inicia o monitoramento de arquivos em background
echo "👀 Iniciando monitoramento de arquivos..."
while inotifywait -r -e modify,create,delete /app/src/main/; do 
    echo "📝 Detectada mudança no código fonte, recompilando..."
    mvn compile -o -DskipTests -q
    echo "✅ Recompilação concluída"
done >/dev/null 2>&1 &
WATCH_PID=$!

# Aguarda um momento para garantir que o monitoramento está ativo
sleep 2

# Inicia a aplicação Spring Boot
echo "🌱 Iniciando aplicação Spring Boot..."
mvn spring-boot:run \
    -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005" \
    -Dspring-boot.run.fork=false &
SPRING_PID=$!

echo "✅ Hot reloading ativo! A aplicação está rodando em:"
echo "   🌐 HTTP: http://localhost:8080"
echo "   🐛 Debug: localhost:5005"
echo "   🔄 LiveReload: localhost:35729"
echo ""

# Aguarda o processo Spring Boot
wait $SPRING_PID
