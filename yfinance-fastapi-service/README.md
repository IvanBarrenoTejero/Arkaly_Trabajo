# yfinance-fastapi-service

Servicio FastAPI para exponer endpoints HTTP sobre `yfinance`, pensado para que una aplicación Java pueda consultar datos de mercado vía JSON.

## Endpoints

- `GET /health`
- `GET /api/v1/quote/{ticker}`
- `GET /api/v1/history/{ticker}`
- `GET /api/v1/options/{ticker}`
- `GET /api/v1/download`

## Configuración

1. Copia el fichero de ejemplo:

```bash
cp .env.example .env
```

2. Ajusta los valores según necesites.

## Ejecutar con uv

Instalar dependencias:

```bash
uv sync
```

Arrancar en desarrollo:

```bash
uv run fastapi dev app/main.py --host 0.0.0.0 --port 8000
```

Arrancar en modo más parecido a producción:

```bash
uv run uvicorn app.main:app --host 0.0.0.0 --port 8000
```

## Ejemplos

### Quote

```bash
curl "http://localhost:8000/api/v1/quote/AAPL"
```

### History

```bash
curl "http://localhost:8000/api/v1/history/AAPL?period=6mo&interval=1d"
```

### Options

```bash
curl "http://localhost:8000/api/v1/options/AAPL"
```

### Download múltiples tickers

```bash
curl "http://localhost:8000/api/v1/download?tickers=AAPL,MSFT,GOOG&period=1mo&interval=1d&group_by=column"
```

## Consumo desde Java

Tu app Java solo necesita hacer peticiones HTTP GET y parsear el JSON de respuesta.
