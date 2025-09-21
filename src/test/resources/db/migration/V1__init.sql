CREATE TABLE holdings
(
    id           UUID                     NOT NULL,
    portfolio_id UUID                     NOT NULL,
    symbol       VARCHAR(15)              NOT NULL,
    quantity     DECIMAL(24, 8),
    avg_cost     DECIMAL(24, 8),
    note         VARCHAR(200),
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    version      BIGINT,
    CONSTRAINT pk_holdings PRIMARY KEY (id)
);

CREATE TABLE portfolios
(
    id            UUID                     NOT NULL,
    user_id       UUID                     NOT NULL,
    name          VARCHAR(80)              NOT NULL,
    base_currency VARCHAR(3)               NOT NULL,
    status        VARCHAR(16)              NOT NULL,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at    TIMESTAMP WITH TIME ZONE NOT NULL,
    deleted_at    TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_portfolios PRIMARY KEY (id)
);

ALTER TABLE holdings
    ADD CONSTRAINT uq_holdings_portfolio_symbol UNIQUE (portfolio_id, symbol);

CREATE INDEX ix_holdings_symbol ON holdings (symbol);

CREATE INDEX ix_portfolios_user ON portfolios (user_id);

ALTER TABLE holdings
    ADD CONSTRAINT FK_HOLDINGS_ON_PORTFOLIO FOREIGN KEY (portfolio_id) REFERENCES portfolios (id);

CREATE INDEX ix_holdings_portfolio ON holdings (portfolio_id);