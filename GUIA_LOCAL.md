# Guia local - portfolio-service

## Que es
Servicio de portfolios y holdings (fuente de verdad de posiciones).

## Prerrequisitos
- Docker + Docker Compose, o Java 21
- PostgreSQL

## Opcion A: correr standalone con este repo
Usa `portfolio-service/docker-compose.yml`.

```bash
docker compose up -d
```

En este modo, por configuracion actual:
- app: `http://localhost:8081`
- db: `localhost:5432`

Apagar:

```bash
docker compose down
```

## Opcion B: correr en stack completo
Desde `gateway-api/`:

```bash
docker compose up -d portfolio-db portfolio-service
```

En stack completo:
- app: `http://localhost:8082`
- db: `localhost:5432`

## Opcion C: correr por Gradle
Configurar variables (`PORT`, `SPRING_DATASOURCE_URL`, `POSTGRES_USER`, `POSTGRES_PASSWORD`, JWT si `auth`):

```bash
set -a
source .env
set +a
./gradlew bootRun
```

## Uso directo
En stack completo (puerto 8082):

```bash
curl -H "Authorization: Bearer <jwt>" \
  "http://localhost:8082/portfolios"
```

## Uso via gateway

```bash
curl -H "Authorization: Bearer <jwt>" \
  "http://localhost:8080/api/portfolio/portfolios"
```

## Referencias
- `docs/API_ENDPOINTS.md`
- `docs/PORTFOLIO_SERVICE_OVERVIEW.md`
