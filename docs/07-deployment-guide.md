# 部署指南

本地开发推荐使用 Docker Compose：

```bash
docker compose up -d
```

Compose 会启动 PostgreSQL、Redis 和 ChainLens 后端服务。应用启动时 Flyway 自动创建表并插入测试数据。

如需本地手动运行：

```bash
mvn -s .mvn/settings.xml spring-boot:run
```

常用环境变量：

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `SPRING_DATA_REDIS_HOST`
- `CHAINLENS_INDEXER_MODE`
- `ETHEREUM_SEPOLIA_RPC_URL`
- `BSC_TESTNET_RPC_URL`
- `POLYGON_AMOY_RPC_URL`

默认 `CHAINLENS_INDEXER_MODE=mock`，不依赖真实 RPC。切换为 `rpc` 后，请配置可靠的测试网 RPC URL。
