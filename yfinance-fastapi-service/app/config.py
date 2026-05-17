from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    app_name: str = "yfinance-fastapi-service"
    app_host: str = "0.0.0.0"
    app_port: int = 8000
    app_reload: bool = True
    app_log_level: str = "info"
    api_prefix: str = "/api/v1"

    default_period: str = "1mo"
    default_interval: str = "1d"
    default_group_by: str = "column"
    default_auto_adjust: bool = True
    default_prepost: bool = False
    default_repair: bool = False
    default_threads: bool = True
    default_proxy: str | None = None
    default_timeout: int = 20

    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        case_sensitive=False,
        extra="ignore",
    )


settings = Settings()
