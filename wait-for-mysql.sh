#!/bin/bash
set -e

host="db"
port=3306
user="root"
password="${MYSQL_ROOT_PASSWORD}"
database="${MYSQL_DATABASE}"
max_retries=30
sleep_seconds=2

echo "Waiting for MySQL ($host:$port) to accept connections..."

until nc -z "$host" "$port"; do
  echo "❌ MySQL port not open yet..."
  sleep $sleep_seconds
done
echo "✅ Port open. Checking full readiness..."

counter=0
until mysql -h"$host" -P"$port" -u"$user" -p"$password" -e "SELECT 1;" "$database" > /dev/null 2>&1; do
  counter=$((counter+1))
  echo "❌ MySQL not ready yet... ($counter/$max_retries)"
  if [ $counter -ge $max_retries ]; then
    echo "💥 MySQL did not become ready after $((max_retries * sleep_seconds)) seconds. Exiting."
    exit 1
  fi
  sleep $sleep_seconds
done

echo "✅ MySQL fully ready. Waiting 5 more seconds for internal stabilization..."
sleep 10

echo "🚀 Starting Spring Boot app..."

set +e
java -jar app.jar
exit_code=$?
if [ $exit_code -ne 0 ]; then
  echo "⚠️ App exited with $exit_code. Retrying once after short delay..."
  sleep 5
  java -jar app.jar
  exit_code=$?
fi
exit $exit_code