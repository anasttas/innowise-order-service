DO
$$
BEGIN
    IF NOT EXISTS (
        SELECT FROM pg_roles WHERE rolname = 'order_service_app'
    ) THEN
        CREATE USER order_service_app WITH PASSWORD 'app_password';
END IF;
END
$$;

GRANT CONNECT ON DATABASE postgres TO order_service_app;

GRANT USAGE, CREATE ON SCHEMA public TO order_service_app;

GRANT SELECT, INSERT, UPDATE, DELETE
      ON ALL TABLES IN SCHEMA public
          TO order_service_app;

ALTER DEFAULT PRIVILEGES IN SCHEMA public
GRANT SELECT, INSERT, UPDATE, DELETE
      ON TABLES TO order_service_app;