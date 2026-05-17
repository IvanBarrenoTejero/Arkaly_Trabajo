from datetime import date
from typing import Any
import logging

import pandas as pd
import yfinance as yf
from fastapi import FastAPI

from app.config import settings
from app.logging_config import setup_logging

setup_logging()
logger = logging.getLogger(__name__)

app = FastAPI(title=settings.app_name)


# ---------------------------------------------------------------------------
# Helpers de conversión
# ---------------------------------------------------------------------------

def _scalar_to_native(value: Any) -> Any:
    """Convierte un escalar a tipo nativo Python. Nunca lanza excepción."""
    if value is None:
        return ""
    if isinstance(value, (pd.Timestamp, date)):
        return value.isoformat()
    if isinstance(value, bool):
        return value
    if isinstance(value, float):
        try:
            return "" if pd.isna(value) else value
        except Exception:
            return ""
    if isinstance(value, int):
        return value
    if isinstance(value, str):
        return value
    if hasattr(value, "item"):
        try:
            return _scalar_to_native(value.item())
        except Exception:
            return ""
    return value


def _to_native(value: Any) -> Any:
    """Convierte recursivamente cualquier valor a tipos JSON-serializables."""
    if isinstance(value, dict):
        return {k: _to_native(v) for k, v in value.items()}
    if isinstance(value, (list, tuple)):
        return [_to_native(v) for v in value]
    return _scalar_to_native(value)


def _frame_to_records(df: pd.DataFrame) -> list[dict[str, Any]]:
    if df is None or df.empty:
        return []
    df = df.reset_index()
    records = df.to_dict(orient="records")
    return [_to_native(record) for record in records]


def _frame_to_nested(df: pd.DataFrame) -> list[dict[str, Any]]:
    if df is None or df.empty:
        return []
    df = df.reset_index()
    if isinstance(df.columns, pd.MultiIndex):
        normalized = []
        for _, row in df.iterrows():
            item: dict[str, Any] = {}
            for col in df.columns:
                col_key = ".".join([str(c) for c in col if c not in (None, "")])
                item[col_key] = _scalar_to_native(row[col])
            normalized.append(item)
        return normalized
    return _frame_to_records(df)


# ---------------------------------------------------------------------------
# Helpers de acceso seguro a yfinance
# ---------------------------------------------------------------------------

def _safe_dict(fn) -> dict[str, Any]:
    """Ejecuta fn() y devuelve un dict. Si falla, devuelve {} y loguea."""
    try:
        result = fn()
        return result if isinstance(result, dict) else {}
    except Exception as exc:
        logger.warning("Safe dict access failed: %s", exc)
        return {}


def _get(d: dict[str, Any], *keys: str, default: Any = "") -> Any:
    """Devuelve el primer valor no vacío encontrado entre las claves dadas."""
    for key in keys:
        val = d.get(key)
        if val is not None:
            if isinstance(val, float):
                try:
                    if pd.isna(val):
                        continue
                except Exception:
                    pass
            return val
    return default


def _build_quote(ticker: str, info: dict, fast_info: dict) -> dict[str, Any]:
    """Construye la respuesta de quote con claves siempre presentes."""
    g = lambda *keys, **kw: _scalar_to_native(_get(info, *keys, **kw))
    gf = lambda *keys, **kw: _scalar_to_native(_get(fast_info, *keys, **kw))

    return {
        "ticker":               ticker,
        "symbol":               g("symbol", default=ticker),
        "shortName":            g("shortName"),
        "longName":             g("longName"),
        "currency":             g("currency", default=gf("currency")),
        "exchange":             g("exchange"),
        "quoteType":            g("quoteType"),
        "marketCap":            g("marketCap", default=gf("marketCap")),
        "currentPrice":         g("currentPrice", default=gf("lastPrice")),
        "previousClose":        g("previousClose", default=gf("previousClose")),
        "open":                 g("open"),
        "dayHigh":              g("dayHigh", default=gf("dayHigh")),
        "dayLow":               g("dayLow", default=gf("dayLow")),
        "volume":               g("volume", default=gf("lastVolume")),
        "averageVolume":        g("averageVolume"),
        "fiftyTwoWeekHigh":     g("fiftyTwoWeekHigh", default=gf("yearHigh")),
        "fiftyTwoWeekLow":      g("fiftyTwoWeekLow", default=gf("yearLow")),
        "fiftyDayAverage":      g("fiftyDayAverage", default=gf("fiftyDayAverage")),
        "twoHundredDayAverage": g("twoHundredDayAverage", default=gf("twoHundredDayAverage")),
        "bid":                  g("bid"),
        "ask":                  g("ask"),
        "bidSize":              g("bidSize"),
        "askSize":              g("askSize"),
        "sector":               g("sector"),
        "industry":             g("industry"),
        "country":              g("country"),
        "website":              g("website"),
        "longBusinessSummary":  g("longBusinessSummary"),
        "dividendRate":         g("dividendRate"),
        "dividendYield":        g("dividendYield"),
        "exDividendDate":       g("exDividendDate"),
        "trailingPE":           g("trailingPE"),
        "forwardPE":            g("forwardPE"),
        "beta":                 g("beta"),
        "trailingEps":          g("trailingEps"),
        "forwardEps":           g("forwardEps"),
    }


# ---------------------------------------------------------------------------
# Eventos
# ---------------------------------------------------------------------------

@app.on_event("startup")
def on_startup() -> None:
    logger.info("Starting %s on prefix %s", settings.app_name, settings.api_prefix)


# ---------------------------------------------------------------------------
# Endpoints
# ---------------------------------------------------------------------------

@app.get("/health")
def health() -> dict[str, str]:
    logger.info("Health check called")
    return {"status": "ok", "service": settings.app_name}


@app.get(f"{settings.api_prefix}/quote/{{ticker}}")
def quote(ticker: str) -> dict[str, Any]:
    logger.info("Quote request for ticker=%s", ticker)

    tk = yf.Ticker(ticker)
    info      = _safe_dict(lambda: tk.info)
    fast_info = _safe_dict(lambda: dict(tk.fast_info))

    result = _build_quote(ticker, info, fast_info)
    logger.info("Quote response ready for ticker=%s", ticker)
    return result


@app.get(f"{settings.api_prefix}/history/{{ticker}}")
def history(
    ticker: str,
    period:      str  = settings.default_period,
    interval:    str  = settings.default_interval,
    prepost:     bool = settings.default_prepost,
    repair:      bool = settings.default_repair,
    auto_adjust: bool = settings.default_auto_adjust,
    timeout:     int  = settings.default_timeout,
) -> dict[str, Any]:
    logger.info(
        "History request ticker=%s period=%s interval=%s prepost=%s repair=%s auto_adjust=%s timeout=%s",
        ticker, period, interval, prepost, repair, auto_adjust, timeout,
    )

    try:
        tk = yf.Ticker(ticker)
        df = tk.history(
            period=period,
            interval=interval,
            prepost=prepost,
            repair=repair,
            auto_adjust=auto_adjust,
            timeout=timeout,
        )
    except Exception as exc:
        logger.warning("History fetch failed for ticker=%s: %s", ticker, exc)
        df = pd.DataFrame()

    logger.info("History rows=%s for ticker=%s", len(df.index), ticker)
    return {
        "ticker":   ticker,
        "period":   period,
        "interval": interval,
        "rows":     _frame_to_records(df),
    }


@app.get(f"{settings.api_prefix}/search/{{query}}")
def search(query: str, limit: int = 10) -> dict[str, Any]:
    logger.info("Search request for query=%s limit=%s", query, limit)
    
    try:
        import requests
        url = f"https://query2.finance.yahoo.com/v1/finance/search?q={query}&newsCount=0&listsCount=0&quotesCount={limit}"
        headers = {"User-Agent": "Mozilla/5.0"}
        resp = requests.get(url, headers=headers, timeout=10)
        data = resp.json()
        
        results = []
        for item in data.get("quotes", []):
            results.append({
                "symbol": item.get("symbol", ""),
                "shortname": item.get("shortname", ""),
                "longname": item.get("longname", ""),
                "quoteType": item.get("quoteType", ""),
                "exchange": item.get("exchange", ""),
            })
        
        logger.info("Search results=%s for query=%s", len(results), query)
        return {"query": query, "results": results}
        
    except Exception as exc:
        logger.warning("Search failed for query=%s: %s", query, exc)
        return {"query": query, "results": []}


@app.get(f"{settings.api_prefix}/options/{{ticker}}")
def options(ticker: str) -> dict[str, Any]:
    logger.info("Options request for ticker=%s", ticker)

    tk = yf.Ticker(ticker)

    try:
        expirations = list(tk.options)
    except Exception as exc:
        logger.warning("Could not load options for ticker=%s: %s", ticker, exc)
        expirations = []

    chains = []
    for expiration in expirations:
        logger.debug("Fetching option chain ticker=%s expiration=%s", ticker, expiration)
        try:
            chain = tk.option_chain(expiration)
            chains.append({
                "expiration": expiration,
                "calls":      _frame_to_records(chain.calls),
                "puts":       _frame_to_records(chain.puts),
            })
        except Exception as exc:
            logger.warning(
                "Could not load option chain ticker=%s expiration=%s: %s",
                ticker, expiration, exc,
            )
            chains.append({
                "expiration": expiration,
                "calls":      [],
                "puts":       [],
            })

    logger.info("Options response ready for ticker=%s expirations=%s", ticker, len(expirations))
    return {
        "ticker":      ticker,
        "expirations": expirations,
        "chains":      chains,
    }


@app.get(f"{settings.api_prefix}/download")
def download(
    tickers:     str,
    period:      str  = settings.default_period,
    interval:    str  = settings.default_interval,
    group_by:    str  = settings.default_group_by,
    auto_adjust: bool = settings.default_auto_adjust,
    prepost:     bool = settings.default_prepost,
    repair:      bool = settings.default_repair,
    threads:     bool = settings.default_threads,
    timeout:     int  = settings.default_timeout,
) -> dict[str, Any]:
    logger.info(
        "Download request tickers=%s period=%s interval=%s group_by=%s "
        "auto_adjust=%s prepost=%s repair=%s threads=%s timeout=%s",
        tickers, period, interval, group_by, auto_adjust, prepost, repair, threads, timeout,
    )

    try:
        df = yf.download(
            tickers=tickers,
            period=period,
            interval=interval,
            group_by=group_by,
            auto_adjust=auto_adjust,
            prepost=prepost,
            repair=repair,
            threads=threads,
            timeout=timeout,
        )
    except Exception as exc:
        logger.warning("Download fetch failed for tickers=%s: %s", tickers, exc)
        df = pd.DataFrame()

    logger.info("Download response rows=%s tickers=%s", len(df.index), tickers)
    return {
        "tickers":  [t.strip() for t in tickers.split(",") if t.strip()],
        "period":   period,
        "interval": interval,
        "group_by": group_by,
        "rows":     _frame_to_nested(df),
    }

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        "app.main:app",
        host=settings.app_host,
        port=settings.app_port,
        reload=settings.app_reload,
        log_level=settings.app_log_level.lower(),
    )