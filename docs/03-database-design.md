# 数据库设计

核心表包括 `wallet_address`、`chain_config`、`chain_transaction`、`risk_blacklist`。

`wallet_address` 保存关注的钱包地址。一个地址在不同链上可能有不同业务含义，所以唯一约束使用 `chain_id + address`。

`chain_config` 保存链配置。RPC URL 在 `application.yml` 中也有一份，用于应用启动时读取真实 RPC 配置。

`chain_transaction` 是索引后的交易事实表。`txHash` 标识一笔链上交易，`blockNumber` 标识交易所在区块，`logIndex` 标识交易日志在该交易 receipt 中的位置。原生币转账通常使用 `logIndex=0`，ERC20 Transfer 事件会使用真实日志下标。

为什么 `txHash + logIndex` 可以避免重复？因为同一笔交易可能包含多个事件日志，单靠 txHash 会丢失多个 ERC20 Transfer；加上 logIndex 后，每条日志事件都有稳定唯一位置，重复同步同一区块时可以幂等写入。

`risk_blacklist` 保存风险地址，风控引擎会在交易保存前检查 `toAddress` 是否命中。
