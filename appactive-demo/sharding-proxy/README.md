# 创建 product 表
```
SET DEFAULT SINGLE TABLE STORAGE UNIT = ds_0;

CREATE TABLE `product` (
  `id` char(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` varchar(2000) COLLATE utf8mb4_unicode_ci NOT NULL,
  `img` char(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` char(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `number` int NOT NULL,
  `price` int NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

# 创建 t_order 分库分表

```
REGISTER STORAGE UNIT ds_0 (
    HOST="mysql",
    PORT=3306,
    DB="demo_ds_0",
    USER="root",
    PASSWORD="demo_appactiive_pw"
),ds_1 (
    HOST="mysql",
    PORT=3306,
    DB="demo_ds_1",
    USER="root",
    PASSWORD="demo_appactiive_pw"
);

CREATE SHARDING TABLE RULE t_order(
	STORAGE_UNITS(ds_0,ds_1),
	SHARDING_COLUMN=user_id,
	TYPE(NAME="hash_mod",PROPERTIES("sharding-count"="4")),
	KEY_GENERATE_STRATEGY(COLUMN=order_id,TYPE(NAME="snowflake"))
);

CREATE TABLE `t_order` (
  `order_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `name` varchar(100) NOT NULL,
  `order_date` date NOT NULL COMMENT "下单时间",
  `status` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```
