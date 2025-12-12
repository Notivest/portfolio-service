CREATE TABLE holding_movements
(
    id           UUID                     NOT NULL,
    portfolio_id UUID                     NOT NULL,
    holding_id   UUID,
    symbol       VARCHAR(15)              NOT NULL,
    type         VARCHAR(4)               NOT NULL,
    quantity     DECIMAL(24, 8)           NOT NULL,
    price        DECIMAL(24, 8)           NOT NULL,
    note         VARCHAR(200),
    executed_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_holding_movements PRIMARY KEY (id)
);

ALTER TABLE holding_movements
    ADD CONSTRAINT fk_holding_movements_portfolio
        FOREIGN KEY (portfolio_id) REFERENCES portfolios (id);

CREATE INDEX ix_holding_movements_portfolio ON holding_movements (portfolio_id);
CREATE INDEX ix_holding_movements_symbol ON holding_movements (symbol);

