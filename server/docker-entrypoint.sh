#!/bin/sh
# server/docker-entrypoint.sh
mkdir -p ./config
cat > ./config/config.properties <<EOF
server.port=${SERVER_PORT}
server.connectionsPoolSize=${SERVER_CONNECTIONS_POOL_SIZE}
scheduler.news-summary-report.periodInSeconds=${NEWS_SUMMARY_REPORT_PERIOD_IN_SECONDS}
EOF

#exec java -jar app.jar
# Run with config directory first in classpath
exec java -cp "app.jar:./config" com.github.nadeemabukhadir.server.ServerApplication