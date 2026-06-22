CREATE TABLE wallet_address (
    id BIGSERIAL PRIMARY KEY,
    chain_id BIGINT NOT NULL,
    address VARCHAR(80) NOT NULL,
    label VARCHAR(120),
    remark VARCHAR(500),
    risk_level VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_wallet_chain_address UNIQUE (chain_id, address)
);

CREATE TABLE chain_config (
    chain_id BIGINT PRIMARY KEY,
    chain_name VARCHAR(80) NOT NULL,
    rpc_url VARCHAR(500) NOT NULL,
    native_symbol VARCHAR(20) NOT NULL,
    explorer_url VARCHAR(300),
    enabled BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE chain_transaction (
    id BIGSERIAL PRIMARY KEY,
    chain_id BIGINT NOT NULL,
    tx_hash VARCHAR(90) NOT NULL,
    block_number BIGINT NOT NULL,
    log_index BIGINT NOT NULL DEFAULT 0,
    from_address VARCHAR(80),
    to_address VARCHAR(80),
    token_address VARCHAR(80),
    token_symbol VARCHAR(30),
    amount NUMERIC(38, 18),
    native_token BOOLEAN NOT NULL DEFAULT FALSE,
    gas_used BIGINT,
    tx_status VARCHAR(30) NOT NULL,
    tx_time TIMESTAMP NOT NULL,
    risk_level VARCHAR(20),
    risk_reason VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_tx_hash_log_index UNIQUE (tx_hash, log_index)
);

CREATE INDEX idx_chain_transaction_address ON chain_transaction (from_address, to_address);
CREATE INDEX idx_chain_transaction_chain_id ON chain_transaction (chain_id);
CREATE INDEX idx_chain_transaction_risk_level ON chain_transaction (risk_level);

CREATE TABLE risk_blacklist (
    id BIGSERIAL PRIMARY KEY,
    address VARCHAR(80) NOT NULL UNIQUE,
    reason VARCHAR(300) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO chain_config (chain_id, chain_name, rpc_url, native_symbol, explorer_url, enabled) VALUES
(11155111, 'Ethereum Sepolia', 'https://rpc.sepolia.org', 'ETH', 'https://sepolia.etherscan.io', true),
(97, 'BSC Testnet', 'https://data-seed-prebsc-1-s1.binance.org:8545', 'BNB', 'https://testnet.bscscan.com', true),
(80002, 'Polygon Amoy', 'https://rpc-amoy.polygon.technology', 'POL', 'https://amoy.polygonscan.com', true);

INSERT INTO wallet_address (chain_id, address, label, remark, risk_level) VALUES
(11155111, '0x1111111111111111111111111111111111111111', 'Demo Treasury', 'Treasury wallet for mock transfers', 'LOW'),
(11155111, '0x2222222222222222222222222222222222222222', 'Demo User A', 'Retail wallet used in demos', 'LOW'),
(97, '0x3333333333333333333333333333333333333333', 'BSC Merchant', 'Merchant settlement wallet', 'LOW');

INSERT INTO risk_blacklist (address, reason) VALUES
('0x9999999999999999999999999999999999999999', 'Known scam collection address'),
('0x8888888888888888888888888888888888888888', 'Phishing address reported by community'),
('0x7777777777777777777777777777777777777777', 'Mixer-related suspicious address'),
('0x6666666666666666666666666666666666666666', 'Malware payment address'),
('0x5555555555555555555555555555555555555555', 'Sanctions screening demo address');

INSERT INTO chain_transaction (chain_id, tx_hash, block_number, log_index, from_address, to_address, token_address, token_symbol, amount, native_token, gas_used, tx_status, tx_time, risk_level, risk_reason) VALUES
(11155111, '0xmock000000000000000000000000000000000000000000000000000000000001', 500001, 0, '0x1111111111111111111111111111111111111111', '0x2222222222222222222222222222222222222222', null, 'ETH', 1.2, true, 21000, 'SUCCESS', CURRENT_TIMESTAMP, 'LOW', 'no elevated risk rule matched'),
(11155111, '0xmock000000000000000000000000000000000000000000000000000000000002', 500002, 0, '0x2222222222222222222222222222222222222222', '0x9999999999999999999999999999999999999999', '0xaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa', 'USDT', 2500, false, 65000, 'SUCCESS', CURRENT_TIMESTAMP, 'HIGH', 'toAddress hit blacklist'),
(11155111, '0xmock000000000000000000000000000000000000000000000000000000000003', 500003, 0, '0x3333333333333333333333333333333333333333', '0x1111111111111111111111111111111111111111', '0xaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa', 'USDT', 15000, false, 72000, 'SUCCESS', CURRENT_TIMESTAMP, 'MEDIUM', 'single transfer amount greater than 10000'),
(97, '0xmock000000000000000000000000000000000000000000000000000000000004', 600001, 0, '0x4444444444444444444444444444444444444444', '0x3333333333333333333333333333333333333333', null, 'BNB', 0.8, true, 21000, 'SUCCESS', CURRENT_TIMESTAMP, 'LOW', 'no elevated risk rule matched'),
(97, '0xmock000000000000000000000000000000000000000000000000000000000005', 600002, 0, '0x4444444444444444444444444444444444444444', '0x3333333333333333333333333333333333333333', '0xbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb', 'BUSD', 120000, false, 78000, 'SUCCESS', CURRENT_TIMESTAMP, 'HIGH', 'single transfer amount greater than 100000'),
(80002, '0xmock000000000000000000000000000000000000000000000000000000000006', 700001, 0, '0x1111111111111111111111111111111111111111', '0x3333333333333333333333333333333333333333', null, 'POL', 12, true, 21000, 'SUCCESS', CURRENT_TIMESTAMP, 'LOW', 'no elevated risk rule matched'),
(80002, '0xmock000000000000000000000000000000000000000000000000000000000007', 700002, 0, '0x2222222222222222222222222222222222222222', '0x8888888888888888888888888888888888888888', '0xcccccccccccccccccccccccccccccccccccccccc', 'USDC', 300, false, 82000, 'SUCCESS', CURRENT_TIMESTAMP, 'HIGH', 'toAddress hit blacklist'),
(11155111, '0xmock000000000000000000000000000000000000000000000000000000000008', 500004, 0, '0x2222222222222222222222222222222222222222', '0x1111111111111111111111111111111111111111', null, 'ETH', 0.01, true, 280000, 'SUCCESS', CURRENT_TIMESTAMP, 'MEDIUM', 'gasUsed is unusually high'),
(11155111, '0xmock000000000000000000000000000000000000000000000000000000000009', 500005, 0, '0x3333333333333333333333333333333333333333', '0x2222222222222222222222222222222222222222', '0xaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa', 'USDT', 42, false, 64000, 'FAILED', CURRENT_TIMESTAMP, 'LOW', 'contract call failed'),
(97, '0xmock000000000000000000000000000000000000000000000000000000000010', 600003, 0, '0x3333333333333333333333333333333333333333', '0x2222222222222222222222222222222222222222', null, 'BNB', 5, true, 21000, 'SUCCESS', CURRENT_TIMESTAMP, 'LOW', 'no elevated risk rule matched');
