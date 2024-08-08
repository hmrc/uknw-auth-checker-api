
# uknw-auth-checker-api

This is the API microservice that provides service to CSPs to check that trader holds NOP waiver.

## Usage

This service should be used in the [Developer Hub](https://developer.service.hmrc.gov.uk/api-documentation).

Access to this service requires:

* Sandbox or Production application on the [Developer Hub](https://developer.service.hmrc.gov.uk/api-documentation).
* An OAuth access token from the [API platform](https://developer.service.hmrc.gov.uk/api-documentation/docs/authorisation/application-restricted-endpoints#getting-access-token) with the client credentials grant type.

## Internals

Internally the service consists of several types of components:

* Action controllers classes
* External http services connectors
* Http endpoints defined in conf/*.routes files
* Request and response model classes
* Services

## API endpoints

| Method | Path            | Description                             |
|--------|-----------------|-----------------------------------------|
| POST   | /authorisations | Check authorisations status for EORI(s) |

## External APIs

The service calls the following external APIs:

* PDS via EIS/IF
  * `/cau/validatecustomsauth/v1`

## Development

This service is built using [Play Framework](https://www.playframework.com/)
and [Scala language](https://www.scala-lang.org/).

### Prerequisites

* [Java 11+](https://adoptium.net/)
* [SBT 1.9.9](https://www.scala-sbt.org/download/)

## Running the service

> `sbt run`

The service runs on port `9070` by default.

## Running locally

Using [service manager](https://github.com/hmrc/service-manager)
with the service manager profile `UKNW_AUTH_CHECKER_API` will start
the UKNW auth checker API.

> `sm --start UKNW_AUTH_CHECKER_API`

## Running tests

### Unit tests

> `sbt test`

### Integration tests

> `sbt it/test`

### Custom commands

#### All tests

This is a sbt command alias specific to this project. It will run a scala format
check, run a scala style check, run unit tests, run integration tests, and produce a coverage report.
> `sbt runAllChecks`

#### Pre-Commit

This is a sbt command alias specific to this project. It will run a scala format , run a scala fix, 
run unit tests, run integration tests and produce a coverage report.
> `sbt runAllChecks`

#### Format all

This is a sbt command alias specific to this project. It will run a scala format
check in the app, tests, and integration tests
> `sbt fmtAll`

#### Fix all

This is a sbt command alias specific to this project. It will run the scala fix 
linter/reformatter in the app, tests, and integration tests
> `sbt fixAll`

### Requesting data from the Stub API

To request data from the stub API please use the `.bru` files that
can be found in `.bruno`. Do not change any of the .bru files, as the stub API
returns an error `500` INTERNAL_SERVER_ERROR on any unexpected requests.

Furthermore, the `Local` environment in bruno should be used, which enables a pre-script
in the collection to run, which automatically requests a bearer token from AUTH_LOGIN_API
and stores it in the `bearerToken` environment variable.

To add a new expected request please refer to the [uknw-auth-checker-api-stub
/README.md](https://github.com/hmrc/uknw-auth-checker-api-stub/blob/main/README.md)

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").