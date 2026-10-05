# Hibernate ORM template

Provisioned from [`Qode-Fleet-Control/fleet-template-v1`](https://github.com/Qode-Fleet-Control/fleet-template-v1) — the fleet
lifecycle contract (`bin/`, `fleet.conf`, deploy workflows) with a
Hibernate ORM persistence starter laid on top.

Hibernate ORM 7.4 on Java 21, Maven build. Two entities (`Author`, `Book` with a `@ManyToOne`), and a job (`world.qode.app.Main`) that persists a few rows and queries them back with HQL. It runs against an in-memory **H2** database by default, or against the Postgres named by **`DATABASE_URL`** (`postgres://user:pass@host:port/db`, as the fleet injects it) when that is set.

## Origin

Hand-written — Hibernate ORM ships no project generator. Laid out after the Hibernate 7
"Introduction to Hibernate" guide: annotated entities, a `SessionFactory` built in code with
`HibernatePersistenceConfiguration` (no `persistence.xml`), work done in `inTransaction` /
`fromTransaction`, queries as HQL selection queries.

## Verified

**Not yet verified end to end on docker.** On 2026-10-05 the shared docker host's disk sat at
0-1 GB free for over 90 minutes (other builds were running), under the 6 GB floor this
scaffold's verification requires, so the `docker compose` build/run check was not run.
Run it before trusting the image:

    docker compose build && docker compose run --rm app              # must exit 0

What did pass, on 2026-10-05:

- `mvn -B package` **with the test suite** in `maven:3.9-eclipse-temurin-21` (the Dockerfile's
  build image) — compiles, tests green, artifacts produced.

## Run it

**This repo is not a service.** It is a job: the image's default command runs the Hibernate job (`java -jar /app/app.jar`)
and exits 0 on success (non-zero on failure). `START_CMD` and `DOCKER_START_CMD`
are empty and nothing listens on `$PORT`, so on the fleet `bin/run` builds the
image and stops there.

**With docker:**

    docker compose build
    docker compose run --rm app          # runs the job
    DATABASE_URL=postgres://u:p@host:5432/db docker compose run --rm app   # against Postgres

**Without docker** — a JDK 21 and Maven 3.9 (`mvn`) on `PATH`:

| step | command |
|---|---|
| install | `mvn -B -q dependency:go-offline` |
| build | `mvn -B -q package` |
| run the job | `java -jar target/app.jar` |

If you add an HTTP endpoint, listen on `0.0.0.0:$PORT` and serve at `/`, then set
`PORT`, `HEALTH_PATH`, `START_CMD` and `DOCKER_START_CMD` in `fleet.conf` and
publish the port in `compose.yaml` (see the HTTP templates).

## Layout

- `src/main/java/world/qode/app/Author.java`, `Book.java` — the entities.
- `src/main/java/world/qode/app/Database.java` — `DATABASE_URL` → JDBC URL + credentials (H2 when unset), and the `SessionFactory`. Schema: `create-drop` on H2; on a real database only `update` (never dropped) — use a migration tool for a real schema.
- `src/main/java/world/qode/app/Main.java` — the job.
- `src/test/java/...` — URL parsing and a persist/query round trip on H2 (run by the image build).
- `pom.xml` — `target/app.jar` with its runtime jars copied to `target/lib`.

## What differs from stock output

- No generator exists; everything above is hand-written (see Origin).
- No database service in `compose.yaml`: the job brings its own embedded H2, and the fleet's Postgres comes in through `DATABASE_URL`.
- Added the fleet harness: `bin/`, `fleet.conf`, `Dockerfile`, `compose.yaml`, `.dockerignore`, `.gitignore`, `.github/workflows/`, `docs/fleet-lifecycle.md`.
