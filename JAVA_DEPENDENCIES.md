# Java依赖说明

## 必需的外部库

### 1. Gson (Google JSON库)
用于Java和Python之间的JSON通信

**Maven依赖**:
```xml
<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
    <version>2.10.1</version>
</dependency>
```

**或者手动下载JAR**:
- 下载地址: https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar
- 将JAR文件放入项目的 `lib/` 目录
- 在IntelliJ中: File → Project Structure → Libraries → + → Java → 选择gson-2.10.1.jar

### 2. SQLite JDBC Driver
用于Java访问SQLite数据库

**Maven依赖**:
```xml
<dependency>
    <groupId>org.xerial</groupId>
    <artifactId>sqlite-jdbc</artifactId>
    <version>3.44.1.0</version>
</dependency>
```

**或者手动下载JAR**:
- 下载地址: https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.44.1.0/sqlite-jdbc-3.44.1.0.jar

## 在IntelliJ中配置

如果不使用Maven/Gradle，请按以下步骤手动添加库：

1. 创建 `lib/` 目录在项目根目录
2. 下载上述两个JAR文件到 `lib/`
3. File → Project Structure → Modules → Dependencies → + → JARs or directories
4. 选择 `lib/` 目录下的所有JAR文件
