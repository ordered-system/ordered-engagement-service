-include .env
export

.PHONY: help up down restart logs build test test-unit test-integration format format-check clean run

help:
	@echo Available commands:
	@echo   up               - Start engagement-service's own MongoDB in Docker
	@echo   down             - Stop it
	@echo   restart          - Restart it
	@echo   logs             - Tail logs from local infrastructure
	@echo   build            - Compile the project (no tests)
	@echo   test-unit        - Run unit tests only
	@echo   test-integration - Run unit + integration tests
	@echo   test             - Alias for test-integration
	@echo   format           - Auto-format code with Spotless
	@echo   format-check     - Check code formatting without modifying files
	@echo   clean            - Remove build artifacts
	@echo   run              - Run the application locally

up:
	docker compose up -d

down:
	docker compose down

restart: down up

logs:
	docker compose logs -f

build:
	mvn --batch-mode --no-transfer-progress compile

test-unit:
	mvn --batch-mode --no-transfer-progress test

test-integration:
	mvn --batch-mode --no-transfer-progress verify

test: test-integration

format:
	mvn --batch-mode --no-transfer-progress spotless:apply

format-check:
	mvn --batch-mode --no-transfer-progress spotless:check

clean:
	mvn --batch-mode --no-transfer-progress clean

run:
	mvn spring-boot:run
