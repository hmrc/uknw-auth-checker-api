We use standard HTTP status codes to show whether an API request succeeded or not. They are usually in the range:

- 200 if it suceeded
- 400 for Validation failure
- 401 for Authorisation failure
- 403 for Forbidden request
- 405 for Method Not Allowed
- 406 for Not Acceptable
- 429 for request in excess of rate limit
- 500 for Internal Server error
- 503 for Service Unavailable

Errors specific to each API are shown in the Endpoints section, under Response. See our [reference guide](/api-documentation/docs/reference-guide#errors) for more on errors.
