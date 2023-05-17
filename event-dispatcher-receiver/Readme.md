# EventDispatcher Receiver

Provides an addressable POST endpoint `/webhook` expecting cloudevents and forwards them to an RabbitMQ Stream.

## Environment variables
* `SERVER_PORT`: (default: `8090`) Port of the Webserver
* `RABBITMQ_HOST`: (required)
* `RABBITMQ_STREAM`: (required) Name of the Stream Queue to forward to
* `RABBITMQ_USER`: (required)
* `RABBITMQ_PASSWORD`: (required)
* `RABBITMQ_VHOST`: (default: `/`)
* `RABBITMQ_STREAM_PORT`: (default: `5552`)

## Build image
```bash
./mvnw compile jib:dockerBuild
```