# Cambio: marketValue -> bookValue

Este cambio es **breaking** para todos los clientes del portfolio-service.

## Que cambio
- Todos los responses reemplazan `marketValue` por `bookValue`.
- El sort por defecto ahora es `bookValue,desc` (antes `marketValue,desc`).
- La semantica no cambia: sigue siendo valor de libro `quantity * avgCost`.
- No hay cambios de schema en DB.

## Endpoints afectados
- `GET /portfolios/{portfolioId}/holdings`
- `POST /portfolios/{portfolioId}/holdings`
- `POST /portfolios/{portfolioId}/holdings/buy`
- `POST /portfolios/{portfolioId}/holdings/sell`
- `PATCH /portfolios/{portfolioId}/holdings/{holdingId}`
- `GET /portfolios/{portfolioId}/holdings/summary`
- `POST /internal/v1/holdings/search`
- `GET /internal/v1/portfolios?userId=...&includeHoldings=...`
- `GET /internal/v1/portfolios/{portfolioId}/holdings/summary`

## Payloads
### Antes
```json
{
  "symbol": "AAPL",
  "quantity": 10.5,
  "avgCost": 185.32,
  "marketValue": 1945.86
}
```

### Ahora
```json
{
  "symbol": "AAPL",
  "quantity": 10.5,
  "avgCost": 185.32,
  "bookValue": 1945.86
}
```

## Query params
- `sort=bookValue,desc|asc` (antes `marketValue,desc|asc`)

## Acciones para otros servicios
- Renombrar `marketValue` -> `bookValue` en DTOs y mappers.
- Actualizar `sort` y cualquier logica que ordene por `marketValue`.
- Ajustar tests/fixtures.
