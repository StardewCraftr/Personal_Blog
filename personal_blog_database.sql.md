# Personal Blog 数据库建表 SQL

## 一、创建数据库

``` sql
CREATE DATABASE IF NOT EXISTS personal_blog
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_0900_ai_ci;

USE personal_blog;
```

## 二、用户表

``` sql
CREATE TABLE t_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    nickname VARCHAR(50),
    email VARCHAR(100) UNIQUE,
    avatar VARCHAR(255),
    status TINYINT DEFAULT 1 COMMENT '1正常 0禁用',
    is_admin TINYINT DEFAULT 0 COMMENT '1管理员 0普通用户',
    last_login_time DATETIME,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0正常 1删除'
) ENGINE=InnoDB;
```

## 三、分类表

``` sql
CREATE TABLE t_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    parent_id BIGINT DEFAULT 0 COMMENT '父级分类',
    sort INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_category_name (name)
) ENGINE=InnoDB;
```

## 四、标签表

``` sql
CREATE TABLE t_tag (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_tag_name (name)
) ENGINE=InnoDB;
```

## 五、文章表

``` sql
CREATE TABLE t_article (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    summary VARCHAR(500),
    content LONGTEXT,
    status TINYINT DEFAULT 0 COMMENT '0草稿 1发布',
    is_private TINYINT DEFAULT 0 COMMENT '0公开 1私密',
    view_count INT DEFAULT 0,
    like_count INT DEFAULT 0,
    comment_count INT DEFAULT 0,
    category_id BIGINT,
    user_id BIGINT,
    publish_time DATETIME,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    CONSTRAINT fk_article_user FOREIGN KEY (user_id) REFERENCES t_user(id),
    CONSTRAINT fk_article_category FOREIGN KEY (category_id) REFERENCES t_category(id)
) ENGINE=InnoDB;
```

## 六、全文索引

``` sql
ALTER TABLE t_article 
ADD FULLTEXT INDEX ft_title_content (title, content);
```

## 七、文章标签关系表

``` sql
CREATE TABLE t_article_tag (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    article_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_article_tag (article_id, tag_id),
    CONSTRAINT fk_at_article FOREIGN KEY (article_id) REFERENCES t_article(id) ON DELETE CASCADE,
    CONSTRAINT fk_at_tag FOREIGN KEY (tag_id) REFERENCES t_tag(id) ON DELETE CASCADE
) ENGINE=InnoDB;
```

## 八、评论表

``` sql
CREATE TABLE t_comment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    article_id BIGINT NOT NULL,
    user_id BIGINT,
    parent_id BIGINT DEFAULT 0 COMMENT '父评论ID',
    content TEXT NOT NULL,
    like_count INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    CONSTRAINT fk_comment_article FOREIGN KEY (article_id) REFERENCES t_article(id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_user FOREIGN KEY (user_id) REFERENCES t_user(id)
) ENGINE=InnoDB;
```

## 九、文章版本表

``` sql
CREATE TABLE t_article_version (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    article_id BIGINT NOT NULL,
    title VARCHAR(200),
    content LONGTEXT,
    version_no INT NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_version_article FOREIGN KEY (article_id) REFERENCES t_article(id) ON DELETE CASCADE,
    UNIQUE KEY uk_article_version (article_id, version_no)
) ENGINE=InnoDB;
```

## 十、附件表

``` sql
CREATE TABLE t_attachment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    file_name VARCHAR(255),
    file_url VARCHAR(500),
    file_size BIGINT,
    file_type VARCHAR(50),
    user_id BIGINT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_attachment_user FOREIGN KEY (user_id) REFERENCES t_user(id)
) ENGINE=InnoDB;
```

## 十一、文章引用关系表

``` sql
CREATE TABLE t_article_reference (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    source_id BIGINT NOT NULL COMMENT '引用方文章',
    target_id BIGINT NOT NULL COMMENT '被引用文章',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_article_ref (source_id, target_id),
    CONSTRAINT fk_ref_source FOREIGN KEY (source_id) REFERENCES t_article(id) ON DELETE CASCADE,
    CONSTRAINT fk_ref_target FOREIGN KEY (target_id) REFERENCES t_article(id) ON DELETE CASCADE
) ENGINE=InnoDB;
```

## 十二、操作日志表

``` sql
CREATE TABLE t_operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    operation VARCHAR(100),
    method VARCHAR(200),
    params TEXT,
    ip VARCHAR(50),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_log_user FOREIGN KEY (user_id) REFERENCES t_user(id)
) ENGINE=InnoDB;
```

## 十三、索引优化

``` sql
CREATE INDEX idx_article_user ON t_article(user_id);
CREATE INDEX idx_article_category ON t_article(category_id);
CREATE INDEX idx_article_status ON t_article(status);
CREATE INDEX idx_article_publish_time ON t_article(publish_time);

CREATE INDEX idx_comment_article ON t_comment(article_id);
CREATE INDEX idx_comment_parent ON t_comment(parent_id);
```
