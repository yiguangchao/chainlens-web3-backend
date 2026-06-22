# API 设计

所有 API 返回统一格式：

```json
{
  "code": 0,
  "message": "success",
  "data": {},
  "timestamp": "2026-06-21T12:00:00Z"
}
```

主要接口：

- `POST /api/wallets`：添加钱包地址。
- `GET /api/wallets`：查询钱包列表。
- `GET /api/wallets/{address}`：按地址查询钱包详情。
- `POST /api/indexer/mock-sync`：生成模拟交易并保存。
- `POST /api/indexer/sync-range`：同步指定区块范围。
- `GET /api/transactions/{txHash}`：按交易 Hash 查询详情。
- `GET /api/transactions`：分页查询，可按 chainId 或 riskLevel 过滤。
- `GET /api/transactions/address/{address}`：查询某个地址相关交易。
- `GET /api/transactions/risk/{riskLevel}`：查询某风险等级交易。
- `GET /api/ai/explain/{txHash}`：生成模板化交易解释。
- `GET /api/health`：健康检查。

Swagger 地址：`http://localhost:8080/swagger-ui/index.html`。
