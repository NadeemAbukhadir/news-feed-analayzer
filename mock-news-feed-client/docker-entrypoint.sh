#!/bin/sh
# Generate config.properties from environment variables
mkdir -p ./config
cat > ./config/config.properties <<EOF
# Client Configuration
news.analyze.server.host=${NEWS_ANALYZE_SERVER_HOST}
news.analyze.server.port=${NEWS_ANALYZE_SERVER_PROT}
scheduler.message-send.intervalInMs=${SEND_MESSAGE_INTERVAL_IN_MS}
EOF

#exec java -jar app.jar
# Run with config directory first in classpath
exec java -cp "app.jar:./config" de.tick.ts.mocknewsfeed.MockNewsServiceApplication