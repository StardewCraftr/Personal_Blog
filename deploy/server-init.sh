#!/bin/bash
# ============================================
# 服务器初始化脚本（首次部署执行一次）
# 在服务器上运行: bash server-init.sh
# ============================================

set -e

echo "=========================================="
echo "  服务器初始化"
echo "=========================================="

# ---------- 1. 安装 Java 17 ----------
echo "[1/4] 检查 Java..."
if ! command -v java &> /dev/null; then
    echo "安装 Java 17..."
    apt update
    apt install -y openjdk-17-jdk
else
    java -version
fi

# ---------- 2. 安装 Nginx ----------
echo "[2/4] 检查 Nginx..."
if ! command -v nginx &> /dev/null; then
    echo "安装 Nginx..."
    apt update
    apt install -y nginx
    systemctl enable nginx
    systemctl start nginx
else
    nginx -v
fi

# ---------- 3. 安装 MySQL ----------
echo "[3/4] 检查 MySQL..."
if ! command -v mysql &> /dev/null; then
    echo "安装 MySQL..."
    apt update
    apt install -y mysql-server
    systemctl enable mysql
    systemctl start mysql

    echo ""
    echo "请手动执行以下命令初始化数据库："
    echo "  sudo mysql -u root"
    echo "  CREATE DATABASE personal_blog DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;"
    echo "  CREATE USER 'personal_blog'@'localhost' IDENTIFIED BY '123456';"
    echo "  GRANT ALL PRIVILEGES ON personal_blog.* TO 'personal_blog'@'localhost';"
    echo "  FLUSH PRIVILEGES;"
    echo "  EXIT;"
    echo ""
    echo "然后导入数据表："
    echo "  mysql -u personal_blog -p personal_blog < /home/boke/personal_blog_database.sql"
else
    mysql --version
fi

# ---------- 4. 创建用户和目录 ----------
echo "[4/4] 创建部署目录..."
mkdir -p /home/boke/blog-backend
mkdir -p /home/boke/blog-frontend
mkdir -p /home/boke/uploadPath

echo ""
echo "=========================================="
echo "  初始化完成！"
echo ""
echo "  接下来："
echo "  1. 初始化 MySQL 数据库（见上面的提示）"
echo "  2. 在本地运行 deploy.sh 上传项目文件"
echo "  3. 启动服务："
echo "     systemctl start blog-backend"
echo "     systemctl reload nginx"
echo "=========================================="
