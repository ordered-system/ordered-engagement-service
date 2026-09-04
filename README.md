# ordered-engagement-service

[![CI](https://github.com/ordered-system/ordered-engagement-service/actions/workflows/ci.yml/badge.svg)](https://github.com/ordered-system/ordered-engagement-service/actions/workflows/ci.yml)

Reviews and browsing history for [ordered-system](https://github.com/ordered-system), extracted from the [`ordered-backend`](https://github.com/ordered-system/ordered-backend) monolith. The one service in the system backed by **MongoDB** instead of PostgreSQL — a deliberate choice to practice running a relational and a document store side by side in the same system, since both review documents and history entries are naturally schema-loose, high-write, low-relational data.

## What it does

- **Verified-purchase reviews**: a review can only be left on a product the buyer actually received. `OrderDeliveredListener` consumes the `order-delivered` Kafka event published by `order-service`'s outbox (`InternalBrowsingHistoryController`'s sibling flow) and records a `VerifiedPurchase`; `ReviewService` checks against that before accepting a review, rejecting both un-purchased products (`ProductNotPurchasedException`) and duplicate reviews (`DuplicateReviewException`).
- **Browsing history**, recorded per user as they view products — `RecordViewRequest` from the public API, plus an `InternalBrowsingHistoryController` used service-to-service.
- **Idempotent Kafka consumption**: like `product-service`, tracks `ProcessedEvent` IDs so a redelivered `order-delivered` message can't create a duplicate `VerifiedPurchase`.

## API

Base path `/api/v1/reviews` and `/api/v1/browsing-history`, reached through [`ordered-gateway`](https://github.com/ordered-system/ordered-gateway). Reading a product's reviews (`GET /api/v1/reviews/product/**`) is public; recording history and posting reviews requires auth. OpenAPI docs at `/v3/api-docs`.

## Stack

Java 21 · Spring Boot 4.1.0 · MongoDB (Spring Data MongoDB) · Kafka · Eureka Client · Spring Cloud Config Client · Micrometer / Prometheus / OpenTelemetry tracing · [`ordered-commons`](https://github.com/ordered-system/ordered-commons)

## Running it locally

```bash
git clone https://github.com/ordered-system/ordered-commons.git
(cd ordered-commons && make install)

git clone https://github.com/ordered-system/ordered-engagement-service.git
cd ordered-engagement-service
make up      # this service's own MongoDB
make run
```

Runs on **port 9094**. Needs [`ordered-eureka`](https://github.com/ordered-system/ordered-eureka), [`ordered-config-server`](https://github.com/ordered-system/ordered-config-server), and Kafka reachable. Full stack: [`ordered-infra`](https://github.com/ordered-system/ordered-infra).

### Docker

The `Dockerfile` needs `ordered-commons` supplied as an additional build context — build via `ordered-infra`'s compose files rather than a bare `docker build .`.

## Testing

```bash
make test-unit
make test-integration    # Testcontainers MongoDB
```

`OrderDeliveredFlowIntegrationTest` and `ReviewFlowIntegrationTest` cover the "can't review what you didn't buy" rule end-to-end.

## Where this fits

| Service | Database | Role |
|---|---|---|
| [ordered-order-service](https://github.com/ordered-system/ordered-order-service) | PostgreSQL | Orders, cart checkout, payments (Stripe) |
| [ordered-product-service](https://github.com/ordered-system/ordered-product-service) | PostgreSQL + Redis | Product catalog, stock reservation |
| [ordered-user-service](https://github.com/ordered-system/ordered-user-service) | PostgreSQL | Users, auth, JWT issuance |
| **ordered-engagement-service** | MongoDB | Reviews, browsing history |

Part of the [ordered-system](https://github.com/ordered-system) organization.

## License

MIT — see [LICENSE](LICENSE).
