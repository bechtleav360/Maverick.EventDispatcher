# EventDispatcher Subscriptions API

Provides a REST API to manage subscriptions.
It also updates the subscriptions in the filter.

## Environment variables

* `SERVER_PORT`: (default: `8080`) Port of the Webserver
* `DISPATCHER_ADDRESS`: (default: `127.0.0.1:9090`) GRPC address of the filter

## Build image
```bash
./mvnw spring-boot:build-image
```