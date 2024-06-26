
# uknw-auth-checker-api

This is the API microservice that provides service to CSPs to check that trader holds NOP waiver.

## Running the service

> `sbt run`

The service runs on port `9070` by default.

## Running dependencies

Using [service manager](https://github.com/hmrc/service-manager)
with the service manager profile `UKNW_AUTH_CHECKER_API` will start
the UKNW auth checker API.

> `sm --start UKNW_AUTH_CHECKER_API`

## Running tests

### Unit tests

> `sbt test`

### Integration tests

> `sbt it:test`


### All tests

This is a sbt command alias specific to this project. It will run a scala format
check, run a scala style check, run unit tests, run integration tests and produce a coverage report.
> `sbt runAllChecks`

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").