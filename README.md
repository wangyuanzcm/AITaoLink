# JeecgBoot AI低代码平台模板项目

源码地址：https://github.com/jeecgboot/JeecgBoot

本项目采用jeecg-booot的3.9.1版本进行精简

# 开发环境

Node.js 18.x   
JDK17   
Maven   
Redis3.2+   
Mysql5.7+   

# 启动命令

server启动命令：

```
// 在server文件夹下执行
mvn clean install  
mvn spring-boot:run -pl jeecg-module-system\jeecg-system-start

```

web启动命令

```
pnpm install
pnpm run dev
pnpm run build
```

# 新建后端模块 
比如我要新建tongnian模块

第一步：
在jeecg-boot-module目录下执行

```
mvn archetype:generate ^
   -DgroupId=org.jeecgframework.boot3 ^
   -DartifactId=jeecg-module-tongnian ^
   -Dversion=3.8.3 ^
   -DarchetypeGroupId=org.jeecgframework.archetype ^
   -DarchetypeArtifactId=jeecg-boot-archetype ^
   -DarchetypeVersion=3.0
```

多行命令不生效的话，命令会依次提示填写：

- archetype： 默认选择7，直接回车即可
- groupId： 填写org.jeecgframework.boot3
- artifactId： 填写jeecg-module-tongnian
- version： 填写3.8.3
- package： 填写org.jeecgframework.boot3.tongnian

第二步：
修改jeecg-boot-module\pom.xml的配置，在<modules>标签下添加如下配置：

```
<module>jeecg-module-tongnian</module>
```
第三步：
在jeecg-system-start/pom.xml中添加新模块的依赖
```
 <dependency>
    <groupId>org.jeecgframework.boot3</groupId>
    <artifactId>jeecg-module-tongnian</artifactId>
    <version>${jeecgboot.version}</version>
</dependency>
```
注意需要补充模块依赖：

```
    <dependency>
      <groupId>org.jeecgframework.boot3</groupId>
      <artifactId>jeecg-boot-base-core</artifactId>
    </dependency>
```

最后运行：mvn clean install

# 自动生成代码配置

### 首先修改生成代码路径

主要参数说明：

    project_path：后端 Java 项目模块路径
    ui_project_path：前端 VUE3 项目路径
    bussi_package：业务包路径-这里与上述生成包路径是一致的

配置示例：   
```
project_path=D:\\github_repo\\tongnian_order\\server\\jeecg-boot-module\\jeecg-module-tongnian
ui_project_path=D:\\github_repo\\tongnian_order\\web
bussi_package=org.jeecg
```

默认情况下，代码生成器与平台共用同一个数据库。所以这里的数据库不需要修改

### 配置代码模板
默认情况下，代码生成器会使用jeecg-boot的默认代码模板。 默认代码模板路径为：
```
server\jeecg-module-system\jeecg-system-biz\src\main\resources\jeecg\code-template-online
```

如果需要自定义代码模板，需要修改jeecg_config.properties文件中的templatepath参数。

配置示例：
```
templatepath=/jeecg/code-template
```

注意对应的代码位置在：
```
server\jeecg-module-system\jeecg-system-start\config
```


### 在低代码-online表单开发中进行建表

这里涉及单表，主表，附表。需要先进行设计，确定好表之间的关系。

注意生成代码后，要手动执行sql脚本。