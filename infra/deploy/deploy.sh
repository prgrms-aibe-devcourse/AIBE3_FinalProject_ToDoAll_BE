set -e

APP_DIR="/home/ubuntu/jobda"
DEPLOY_DIR="/home/ubuntu/deploy"
CURRENT_COLOR_FILE="$DEPLOY_DIR/CURRENT_COLOR"
NGINX_UPSTREAM="/etc/nginx/conf.d/jobda-upstream.conf"

IMAGE_TAG=$1

if [ ! -f "$CURRENT_COLOR_FILE" ]; then
  echo "blue" > "$CURRENT_COLOR_FILE"
fi

CURRENT_COLOR=$(cat $CURRENT_COLOR_FILE)

if [ "$CURRENT_COLOR" = "blue" ]; then
  TARGET_COLOR="green"
  TARGET_PORT=8081
  TARGET_ACTUATOR_PORT=8081
  COMPOSE_FILE="docker-compose-green.yml"
  SERVICE_NAME="jobda-app-green"
else
  TARGET_COLOR="blue"
  TARGET_PORT=8080
  TARGET_ACTUATOR_PORT=8081
  COMPOSE_FILE="docker-compose-blue.yml"
  SERVICE_NAME="jobda-app-blue"
fi

echo "현재 라이브 색 : $CURRENT_COLOR"
echo "이번 배포 대상 : $TARGET_COLOR"

cd $APP_DIR

echo "이미지 Pull..."
IMAGE_TAG=$IMAGE_TAG docker compose -f docker-compose-base.yml -f $COMPOSE_FILE pull $SERVICE_NAME

echo "새 버전 컨테이너 실행..."
IMAGE_TAG=$IMAGE_TAG docker compose -f docker-compose-base.yml -f $COMPOSE_FILE up -d --no-deps $SERVICE_NAME

echo "헬스 체크 시작..."
HEALTH_OK=false

for i in {1..20}; do
  if curl -fsS "http://127.0.0.1:${TARGET_ACTUATOR_PORT}/actuator/health/liveness" ; then
    echo "Health OK!"
    HEALTH_OK=true
    break
  fi
  echo "대기중 ($i)..."
  sleep 5
done

if [ "$HEALTH_OK" != true ]; then
  echo "헬스 체크 실패 → 배포 중단"
  exit 1
fi

echo "Nginx Upstream 전환 → $TARGET_PORT"
sudo bash -c "cat > $NGINX_UPSTREAM" <<EOF
upstream jobda_backend {
    server 127.0.0.1:$TARGET_PORT;
}
EOF

sudo systemctl reload nginx
echo "$TARGET_COLOR" > "$CURRENT_COLOR_FILE"

echo "=== 블루/그린 무중단 배포 완료 ==="