#!/bin/bash
# ============================================
# 博客项目一键部署脚本
# 服务器 IP: 115.190.220.59
# 在本地 Git Bash 中运行: bash deploy/deploy.sh
# ============================================

set -e

SERVER_IP="115.190.220.59"
BOKE_HOME="/home/boke"
BACKEND_DIR="$BOKE_HOME/blog-backend"
FRONTEND_DIR="$BOKE_HOME/blog-frontend"
UPLOAD_DIR="$BOKE_HOME/uploadPath"
PROJECT_ROOT="/c/Users/82475/Desktop/boke"

echo "=========================================="
echo "  博客项目部署脚本"
echo "  服务器: $SERVER_IP"
echo "=========================================="

# ---------- 1. 创建目录 ----------
echo "[1/6] 创建必要目录..."
mkdir -p $BACKEND_DIR
mkdir -p $FRONTEND_DIR
mkdir -p $UPLOAD_DIR

# ---------- 2. 构建后端 ----------
echo "[2/6] 构建后端项目..."
cd $PROJECT_ROOT/blog-backend
mvn clean package -DskipTests
echo "后端 JAR 构建完成"

# ---------- 3. 构建前端 ----------
echo "[3/6] 构建前端项目..."
cd $PROJECT_ROOT/blog-frontend
npm run build
echo "前端构建完成"

# ---------- 4. 上传到服务器 ----------
echo "[4/6] 上传文件到服务器..."
# 后端 JAR
scp target/blog-backend-1.0.0.jar root@$SERVER_IP:$BACKEND_DIR/blog-backend.jar

# 前端 dist
scp -r dist/* root@$SERVER_IP:$FRONTEND_DIR/

# Nginx 配置
scp $PROJECT_ROOT/deploy/nginx.conf root@$SERVER_IP:/etc/nginx/conf.d/blog.conf

# Systemd 服务
scp $PROJECT_ROOT/deploy/blog-backend.service root@$SERVER_IP:/etc/systemd/system/

# SQL 文件（首次部署需要）
scp $PROJECT_ROOT/personal_blog_database.sql root@$SERVER_IP:$BOKE_HOME/

echo "文件上传完成"

# ---------- 5. 服务器配置 ----------
echo "[5/6] 配置服务器..."
ssh root@$SERVER_IP << 'EOF'
# 重载 Nginx
nginx -t && systemctl reload nginx

# 重载 systemd 并重启后端服务
systemctl daemon-reload
systemctl enable blog-backend
systemctl restart blog-backend

echo "服务配置完成"
EOF

# ---------- 6. 检查状态 ----------
echo "[6/6] 检查服务状态..."
ssh root@$SERVER_IP << 'EOF'
echo "--- 后端服务状态 ---"
systemctl status blog-backend --no-pager -l | head -10

echo ""
echo "--- Nginx 状态 ---"
systemctl status nginx --no-pager | head -5

echo ""
echo "--- 端口监听 ---"
ss -tlnp | grep -E ':(80|8080)\s'
EOF

echo ""
echo "=========================================="
echo "  部署完成！"
echo "  访问地址: http://$SERVER_IP"
echo "  后端地址: http://$SERVER_IP:8080"
echo "=========================================="
