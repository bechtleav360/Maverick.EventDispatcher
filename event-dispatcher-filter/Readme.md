# EventDispatcher Publisher

Listens to a RabbitMQ Stream and forwards the events to separate AMQP queues.
Provides an addressable GRPC endpoint for the subscriptions module to update the subscriptions.

## Environment variables

* `GRPC_SERVER_PORT`: (default: `9090`) Port of the GRPC Server
* `SUBSCRIBING_URL`: (required) URL of the subscriptions API
* `RABBITMQ_HOST`: (required)
* `RABBITMQ_STREAM`: (required) Name of the Stream Queue to listen to
* `RABBITMQ_USER`: (required)
* `RABBITMQ_PASSWORD`: (required)
* `RABBITMQ_VHOST`: (default: `/`)
* `RABBITMQ_STREAM_PORT`: (default: `5552`)
* `RABBITMQ_AMQP_PORT`: (default: `5672`)

## Build image
```bash
./mvnw spring-boot:build-image
```