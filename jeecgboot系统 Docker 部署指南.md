# 同年订单系统 Docker 部署指南

## 一、镜像构建

### 1.1 前端镜像构建

```bash
# 进入前端目录
cd d:\github_repo\tongnian_order\web

# 安装依赖
npm install

# 构建生产环境
pnpm run build:docker

# 构建Docker镜像（使用语义化标签）
docker build -f Dockerfile.cloud -t tongnian-web:v1.7.8 .

# 保留latest标签
docker tag tongnian-web:v1.7.8 tongnian-web:latest
```

### 1.2 后端镜像构建

```bash
# 进入后端目录
cd d:\github_repo\tongnian_order\server

# 打包Java项目
mvn clean package -DskipTests

# 构建Docker镜像（使用语义化标签）
cd jeecg-module-system\jeecg-system-start
docker build -t tongnian-system:v1.7.8 .

# 保留latest标签
docker tag tongnian-system:v1.7.8 tongnian-system:latest
```

### 1.3 查看本地镜像

```bash
docker images
```

## 二、导出镜像

```bash
# 导出前端镜像
docker save -o tongnian-web-v1.7.8.tar tongnian-web:v1.7.8

# 导出后端镜像
docker save -o tongnian-system-v1.7.8.tar tongnian-system:v1.7.8
```

## 三、导入镜像

```bash
# 导入前端镜像
docker load -i tongnian-web-v1.7.8.tar

# 导入后端镜像
docker load -i tongnian-system-v1.7.8.tar
```

## 四、上传Docker镜像到服务器

### 4.1 准备工作

1. 在宝塔面板中创建网站目录：`/www/wwwroot/tongnian`
2. 上传项目文件到该目录（包括docker-compose.yml）

### 4.2 上传镜像文件

- 将导出的镜像文件上传到服务器：`/www/wwwroot/tongnian`
- 使用 SCP 或宝塔面板的文件管理功能上传

### 4.3 在服务器上加载镜像

```bash
cd /www/wwwroot/tongnian

# 加载前端镜像
docker load -i tongnian-web-v1.7.8.tar

# 加载后端镜像
docker load -i tongnian-system-v1.7.8.tar
```

## 五、Docker Compose配置

### 5.1 使用已上传的镜像（推荐）

如果已经上传了镜像文件，使用以下配置：

### Windows/Mac 环境（Docker Desktop）

```yaml
version: '2'
services:
  jeecg-boot-system:
    image: tongnian-system:v1.7.8
    restart: on-failure
    container_name: jeecg-boot-system
    hostname: jeecg-boot-system
    ports:
      - 8080:8080
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - SPRING_DATASOURCE_DYNAMIC_DATASOURCE_MASTER_URL=jdbc:mysql://host.docker.internal:3306/tongnian?characterEncoding=UTF-8&useUnicode=true&useSSL=false&tinyInt1isBit=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai
      - SPRING_DATASOURCE_DYNAMIC_DATASOURCE_MASTER_USERNAME=root
      - SPRING_DATASOURCE_DYNAMIC_DATASOURCE_MASTER_PASSWORD=你的MySQL密码
      - SPRING_DATA_REDIS_HOST=host.docker.internal
      - SPRING_DATA_REDIS_PORT=6379
      - SPRING_DATA_REDIS_PASSWORD=你的Redis密码

  jeecg-vue:
    image: tongnian-web:v1.7.8
    container_name: jeecgboot-vue3-nginx
    depends_on:
      - jeecg-boot-system
    ports:
      - 8800:80
    environment:
      - BACKEND_SERVER=host.docker.internal
      - BACKEND_PORT=8080
```

### Linux 环境（宝塔面板）

```yaml
version: '2'
services:
  jeecg-boot-system:
    image: tongnian-system:v1.7.8
    restart: on-failure
    container_name: jeecg-boot-system
    hostname: jeecg-boot-system
    ports:
      - 8080:8080
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - SPRING_DATASOURCE_DYNAMIC_DATASOURCE_MASTER_URL=jdbc:mysql://172.17.0.1:3306/tongnian?characterEncoding=UTF-8&useUnicode=true&useSSL=false&tinyInt1isBit=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai
      - SPRING_DATASOURCE_DYNAMIC_DATASOURCE_MASTER_USERNAME=root
      - SPRING_DATASOURCE_DYNAMIC_DATASOURCE_MASTER_PASSWORD=你的MySQL密码
      - SPRING_DATA_REDIS_HOST=172.17.0.1
      - SPRING_DATA_REDIS_PORT=6379
      - SPRING_DATA_REDIS_PASSWORD=你的Redis密码

  jeecg-vue:
    image: tongnian-web:v1.7.8
    container_name: jeecgboot-vue3-nginx
    depends_on:
      - jeecg-boot-system
    ports:
      - 8800:80
    environment:
      - BACKEND_SERVER=172.19.0.2
      - BACKEND_PORT=8080
```

**配置说明**：

- **使用已上传镜像**：使用 `image` 字段指定镜像名称和标签

### Windows/Mac 环境（Docker Desktop）

- **数据库配置**：使用 `host.docker.internal` 访问宿主机的 MySQL 服务
  - 修改 `SPRING_DATASOURCE_DYNAMIC_DATASOURCE_MASTER_URL` 中的数据库名为你的实际数据库名
  - 修改 `SPRING_DATASOURCE_DYNAMIC_DATASOURCE_MASTER_PASSWORD` 为你的 MySQL 实际密码
- **Redis配置**：使用 `host.docker.internal` 访问宿主机的 Redis 服务
  - 修改 `SPRING_DATA_REDIS_PASSWORD` 为你的 Redis 实际密码
- **环境准备**：确保宿主机上的 MySQL 和 Redis 服务已启动并允许 Docker 容器访问

### Linux 环境（宝塔面板）

- **数据库配置**：使用 `172.17.0.1`（Docker默认网关）访问宿主机的 MySQL 服务
  - 修改 `SPRING_DATASOURCE_DYNAMIC_DATASOURCE_MASTER_URL` 中的数据库名为你的实际数据库名
  - 修改 `SPRING_DATASOURCE_DYNAMIC_DATASOURCE_MASTER_PASSWORD` 为你的 MySQL 实际密码
- **Redis配置**：使用 `172.17.0.1`（Docker默认网关）访问宿主机的 Redis 服务
  - 修改 `SPRING_DATA_REDIS_PASSWORD` 为你的 Redis 实际密码
- **环境准备**：确保宿主机上的 MySQL 和 Redis 服务已启动并允许 Docker 容器访问
  - MySQL 默认端口：3306
  - Redis 默认端口：6379
- **注意事项**：
  - `host.docker.internal` 在 Linux 环境中不可用，必须使用 `172.17.0.1` 或宿主机实际IP
  - 如果 `172.17.0.1` 无法访问，可以使用 `ip addr show docker0` 命令查看 Docker 网桥IP

## 六、上线测试

### 6.1 启动服务

```bash
cd /www/wwwroot/tongnian
docker-compose up -d
```

### 6.2 查看服务状态

```bash
docker-compose ps
```

### 6.3 查看日志

```bash
docker-compose logs -f jeecg-boot-system
docker-compose logs -f jeecg-vue
```

### 6.4 测试访问

- **前端**：`http://服务器IP` 或 `http://域名`
- **后端**：`http://服务器IP:8080/jeecg-boot`
- **数据库**：`服务器IP:3306`（宝塔MySQL）

### 6.5 测试接口

访问 `http://服务器IP:8080/jeecg-boot/sys/login` 测试登录接口，访问前端页面测试登录功能。

## 七、数据库初始化

### 7.1 在宝塔中创建数据库

1. 登录宝塔面板
2. 进入"数据库"菜单
3. 创建数据库：
    - 数据库名：`tongnian`
    - 用户名：`root` 或创建新用户
    - 密码：设置密码
    - 字符集：`utf8mb4`

### 7.2 导入数据库结构

**方式一：通过宝塔面板导入**

1. 在宝塔面板中点击"导入"按钮
2. 上传 `server/db/tongnian.sql` 文件
3. 点击"导入"完成初始化

**方式二：通过命令行导入**

```bash
# 进入宝塔MySQL容器（如果使用Docker）
docker exec -i <宝塔MySQL容器名> mysql -uroot -proot tongnian < /path/to/tongnian.sql

# 或直接在服务器上导入
mysql -uroot -proot tongnian < /path/to/tongnian.sql
```

### 7.3 验证初始化

```bash
# 连接数据库
mysql -uroot -proot

# 在MySQL命令行中
USE tongnian;
SHOW TABLES;
```

## 八、常见问题与解决方案

### 8.1 端口冲突

**问题**：端口被占用导致容器启动失败

**解决**：

- 检查端口占用：`netstat -tlnp | grep 8080`
- 修改 `docker-compose.yml` 中的端口映射
- 停止占用端口的服务

### 8.2 数据库连接失败

**问题**：后端服务无法连接数据库

**解决**：

- **Windows/Mac环境**：检查 `host.docker.internal` 是否可访问
- **Linux环境（宝塔面板）**：
  - 检查 `172.17.0.1` 是否可访问，使用命令：`ping 172.17.0.1`
  - 如果无法访问，使用 `ip addr show docker0` 查看Docker网桥IP
  - 或者使用宿主机实际IP地址（如 `192.168.x.x`）
- 确认数据库连接配置正确
- 检查数据库服务是否正常运行
- 检查网络连接：`docker network inspect bridge`

### 8.3 前端访问后端404

**问题**：前端无法访问后端接口

**解决**：

- **Windows/Mac环境**：检查 `BACKEND_SERVER` 环境变量是否设置为 `host.docker.internal`
- **Linux环境（宝塔面板）**：
  - 检查 `BACKEND_SERVER` 环境变量是否设置为 `172.17.0.1` 或宿主机实际IP
  - 确认后端服务端口映射正确（默认8080:8080）
- 检查Nginx反向代理配置
- 确认后端服务是否正常运行
- 检查前端API地址配置

### 8.4 镜像上传失败

**问题**：无法上传镜像到服务器

**解决**：

- 检查网络连接
- 确保Docker服务正常运行
- 使用断点续传工具传输大文件

### 8.5 版本回退

**问题**：新版本部署后出现问题需要回退

**解决**：

1. 修改 `docker-compose.yml` 中的镜像标签：

    ```yaml
    services:
      jeecg-vue:
        image: tongnian-web:v1.7.8
      jeecg-boot-system:
        image: tongnian-system:v1.7.8
    ```
2. 重新部署：

    ```bash
    docker-compose down
    docker-compose up -d
    ```

## 九、宝塔Linux环境配置详解

### 9.1 获取Docker网桥IP

在宝塔Linux环境中，Docker容器访问宿主机需要使用Docker网桥IP。获取方法：

```bash
# 方法1：查看docker0网桥IP
ip addr show docker0

# 方法2：查看默认网关
ip route | grep default

# 方法3：使用hostname -I查看所有IP
hostname -I
```

**常见Docker网桥IP**：

- `172.17.0.1` - Docker默认网桥IP
- `172.18.0.1` - 如果使用了自定义网络
- 宿主机实际IP - 如 `192.168.x.x`

### 9.2 验证网络连接

```bash
# 在容器内测试数据库连接
docker exec -it jeecg-boot-system ping 172.17.0.1

# 测试数据库端口
docker exec -it jeecg-boot-system nc -zv 172.17.0.1 3306

# 测试Redis端口
docker exec -it jeecg-boot-system nc -zv 172.17.0.1 6379
```

### 9.3 宝塔MySQL配置

确保宝塔MySQL允许远程连接：

1. 登录宝塔面板
2. 进入"数据库"菜单
3. 点击"权限"或"远程"设置
4. 添加允许访问的IP：
    - `172.17.0.0/16` - 允许所有Docker容器访问
    - 或具体IP：`172.17.0.1`

### 9.4 宝塔Redis配置

如果使用宝塔Redis，确保配置允许远程连接：

1. 登录宝塔面板
2. 进入"软件商店" -> "Redis" -> "设置"
3. 修改配置文件 `redis.conf`：
    ```conf
    bind 0.0.0.0
    protected-mode no
    ```
4. 重启Redis服务

### 9.5 防火墙配置

确保防火墙允许容器访问宿主机服务：

```bash
# 检查防火墙状态
systemctl status firewalld

# 如果防火墙开启，添加规则
firewall-cmd --permanent --add-rich-rule='rule family="ipv4" source address="172.17.0.0/16" accept'
firewall-cmd --reload

# 或者关闭防火墙（仅用于测试环境）
systemctl stop firewalld
```

---

**文档版本**：v1.1
**更新时间**：2026-02-07
**适用环境**：腾讯云CVM + 宝塔面板 + Docker
