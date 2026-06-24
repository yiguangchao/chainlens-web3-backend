# ChainLens Web3 Backend

[![CI](https://github.com/yiguangchao/chainlens-web3-backend/actions/workflows/ci.yml/badge.svg)](https://github.com/yiguangchao/chainlens-web3-backend/actions/workflows/ci.yml)

ChainLens is a Java 17 + Spring Boot 3 Web3 backend MVP for wallet address management, blockchain transaction indexing, ERC20 transfer parsing, risk assessment, and template-based AI transaction explanations.

It is designed as a GitHub portfolio project for Web3 Backend Engineer, Wallet Backend Engineer, Exchange Backend Engineer, and Crypto Payment Backend Engineer roles.

## Tech Stack

- Java 17
- Spring Boot 3.x
- Maven
- Spring Web, Validation, Data JPA
- PostgreSQL
- Redis
- Web3j
- Springdoc OpenAPI / Swagger
- Flyway
- Docker / Docker Compose
- JUnit 5
- Lombok

## Features

- Wallet address management with label, remark, and risk level fields
- Built-in chain configuration for Ethereum Sepolia, BSC Testnet, and Polygon Amoy
- Transaction indexer with default mock mode and optional RPC mode
- Native token transaction parsing in RPC mode, using transaction value and receipt gas/status
- ERC20 `Transfer(address,address,uint256)` log parsing in RPC mode, using receipt logs and real `logIndex`
- Transaction query by txHash, wallet address, chainId, and risk level
- Risk engine for large transfers, blacklist hits, high-frequency transfers, high gas usage, and failed contract calls
- Template-based AI explanation endpoint, ready for future OpenAI/Qwen/DeepSeek provider integration
- Unified API response shape: `code`, `message`, `data`, `timestamp`
- Swagger UI at `http://localhost:8080/swagger-ui/index.html`

## Project Structure

```text
chainlens-web3-backend
├── README.md
├── pom.xml
├── docker-compose.yml
├── Dockerfile
├── docs
├── sql/init.sql
└── src
    ├── main
    │   ├── java/com/example/chainlens
    │   │   ├── common
    │   │   ├── wallet
    │   │   ├── chain
    │   │   ├── transaction
    │   │   ├── indexer
    │   │   ├── risk
    │   │   └── ai
    │   └── resources
    │       ├── application.yml
    │       └── db/migration/V1__init.sql
    └── test
```

## Local Run

Start PostgreSQL and Redis first, then run:

```bash
mvn spring-boot:run
```

The project includes `.mvn/settings.xml` and `.mvn/maven.config`, so Maven dependencies are stored under the project-local `.m2/repository` directory.

## Docker Run

```bash
docker compose up -d
```

Services:

- PostgreSQL: `localhost:5432`
- Redis: `localhost:6379`
- ChainLens API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`

## API Examples

Health check:

```bash
curl http://localhost:8080/api/health
```

Create wallet:

```bash
curl -X POST http://localhost:8080/api/wallets \
  -H "Content-Type: application/json" \
  -d '{"chainId":11155111,"address":"0xabcdefabcdefabcdefabcdefabcdefabcdefabcd","label":"Demo","remark":"test wallet"}'
```

Run mock sync:

```bash
curl -X POST http://localhost:8080/api/indexer/mock-sync
```

Sync block range:

```bash
curl -X POST http://localhost:8080/api/indexer/sync-range \
  -H "Content-Type: application/json" \
  -d '{"chainId":11155111,"fromBlock":9000000,"toBlock":9000005}'
```

Query transactions:

```bash
curl "http://localhost:8080/api/transactions?chainId=11155111&page=0&size=10"
curl http://localhost:8080/api/transactions/address/0x1111111111111111111111111111111111111111
curl http://localhost:8080/api/transactions/risk/HIGH
```

AI explanation:

```bash
curl http://localhost:8080/api/ai/explain/0xmock000000000000000000000000000000000000000000000000000000000001
```

## Database

Flyway migration: `src/main/resources/db/migration/V1__init.sql`

Tables:

- `wallet_address`
- `chain_config`
- `chain_transaction`
- `risk_blacklist`

Seed data includes 3 wallet addresses, 3 chain configs, 5 blacklist addresses, and 10 mock transactions.

## Tests

```bash
mvn test
```

GitHub Actions runs the same Maven test suite on every push and pull request to `main`.

Current test coverage includes:

- `Erc20TransferParserTest`
- `RiskEngineServiceTest`
- `WalletControllerTest`
- `TransactionQueryServiceTest`

## Notes

The default indexer mode is `mock`, so the application can run without a real RPC endpoint. Set `chainlens.indexer.mode=rpc` and configure RPC URLs in `application.yml` to enable Web3j-backed syncing.

In RPC mode, ChainLens fetches each block with full transactions, then loads transaction receipts to parse:

- native token transfers from transaction `value`
- ERC20 transfers from receipt logs whose `topic0` matches `Transfer(address,address,uint256)`
- receipt `status`, `gasUsed`, token contract address, and log index

Native transfer rows use `logIndex = -1` to avoid colliding with ERC20 log rows from the same transaction.
