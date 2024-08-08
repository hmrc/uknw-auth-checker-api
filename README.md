
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

### POST authorisations request

A POST request to `/authorisations` should have the following example `application/json` body:

```json
{
  "eoris": [
    "GB000000000200"
  ]
}
```

Each EORI must match the following pattern:

```regexp
^(GB|XI)[0-9]{12}|(GB|XI)[0-9]{15}$
```

You also must include an Accept header of `application/vnd.hmrc.1.0+json` as it is validated by 
the API. Not including it will result in a 406 `NOT_ACCEPTABLE` response.

#### Curl sample

```shell
curl --request POST \
  --url http://localhost:9070/authorisations \
  --header 'Accept: application/vnd.hmrc.1.0+json' \
  --header 'Authorization: Bearer PFZBTElEX1RPS0VOPg==' \
  --data '{
  "eoris": [
    "GB000000000200"
  ]
}'
```

The fake bearer token (base64 decoded as `<VALID_TOKEN>`) is stored in the [application.conf](https://github.com/hmrc/uknw-auth-checker-api-stub/blob/main/conf/application.conf) file under `microservices.services.integration-framework.bearerToken`.

The stub [uknw-auth-checker-api-stub](https://github.com/hmrc/uknw-auth-checker-api-stub) has been set up to validate that token fake token in each request, so not included will respond with 401 `UNAUTHORIZED`.

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
in the [collection](https://github.com/hmrc/uknw-auth-checker-api/blob/main/.bruno/collection.bru)
to run, which automatically requests a bearer token from AUTH_LOGIN_API and stores it in
the `bearerToken` environment variable.

To add a new expected request please refer to the [uknw-auth-checker-api-stub
/README.md](https://github.com/hmrc/uknw-auth-checker-api-stub/blob/main/README.md)

### Bruno files

| Bruno file               | Description                                                                                   | Location                                                                                    |
|--------------------------|-----------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------|
| 200-1-Eori               | Valid request with 1 EORI matching `^(GB&#124;XI)[0-9]{12}&#124;(GB&#124;XI)[0-9]{15}$`       | https://github.com/hmrc/uknw-auth-checker-api/blob/main/.bruno/200-1-Eori.bru               |
| 200-100-Eori             | Valid request with 100 EORI matching `^(GB&#124;XI)[0-9]{12}&#124;(GB&#124;XI)[0-9]{15}$`     | https://github.com/hmrc/uknw-auth-checker-api/blob/main/.bruno/200-100-Eori.bru             |
| 200-500-Eori             | Valid request with 500 EORI matching `^(GB&#124;XI)[0-9]{12}&#124;(GB&#124;XI)[0-9]{15}$`     | https://github.com/hmrc/uknw-auth-checker-api/blob/main/.bruno/200-500-Eori.bru             |
| 200-1000-Eori            | Valid request with 1000 EORI matching `^(GB&#124;XI)[0-9]{12}&#124;(GB&#124;XI)[0-9]{15}$`    | https://github.com/hmrc/uknw-auth-checker-api/blob/main/.bruno/200-1000-Eori.bru            |
| 200-3000-Eori            | Valid request with 3000 EORI matching `^(GB&#124;XI)[0-9]{12}&#124;(GB&#124;XI)[0-9]{15}$`    | https://github.com/hmrc/uknw-auth-checker-api/blob/main/.bruno/200-3000-Eori.bru            |
| 201-Invalid-Bearer-Token | Unauthorized request with an invalid bearer token                                             | https://github.com/hmrc/uknw-auth-checker-api/blob/main/.bruno/201-Invalid-Bearer-Token.bru |
| 201-No-Bearer-Token      | Unauthorized request with no bearer token                                                     | https://github.com/hmrc/uknw-auth-checker-api/blob/main/.bruno/201-No-Bearer-Token.bru      |
| 400-0-Eori               | Invalid request with zero EORI                                                                | https://github.com/hmrc/uknw-auth-checker-api/blob/main/.bruno/400-0-Eori.bru               |
| 400-3001-Eori            | Invalid request with greater than 3000 EORI                                                   | https://github.com/hmrc/uknw-auth-checker-api/blob/main/.bruno/400-3001-Eori.bru            |
| 400-Invalid-Eori         | Invalid request with 1 EORI not matching `^(GB&#124;XI)[0-9]{12}&#124;(GB&#124;XI)[0-9]{15}$` | https://github.com/hmrc/uknw-auth-checker-api/blob/main/.bruno/400-Invalid-Eori.bru         |
| 400-Invalid-Json-Empty   | Invalid request with an empty JSON object                                                     | https://github.com/hmrc/uknw-auth-checker-api/blob/main/.bruno/400-Invalid-Json-Empty.bru   |
| 400-Invalid-Json-Format  | Invalid request with invalid JSON                                                             | https://github.com/hmrc/uknw-auth-checker-api/blob/main/.bruno/400-Invalid-Json-Format.bru  |
| 406-No-Accept-Header     | Invalid request with no `application/vnd.hmrc.1.0+json` Accept header set                     | https://github.com/hmrc/uknw-auth-checker-api/blob/main/.bruno/406-No-Accept-Header.bru     |
| 406-No-Body.bru          | Invalid request with no JSON body                                                             | https://github.com/hmrc/uknw-auth-checker-api/blob/main/.bruno/406-No-Body.bru              |

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").