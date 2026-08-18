# Notification Service

This service consumes user registration events from Kafka and sends email verification messages.

## Setup

### 1. Copy the environment template
```bash
cp .env.example .env
```

### 2. Fill in your credentials in `.env`
```bash
MAIL_USER=your-email@gmail.com
MAIL_PASS=your-app-specific-password
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

### 3. Build the service
```bash
mvn -DskipTests package
```

### 4. Run the service
```bash
java -jar target/notification-service-1.0.0.jar
```

Or from the IDE: right-click `NotificationServiceApplication.java` → Run.

## Environment Variables

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `MAIL_USER` | Yes | - | SMTP username (Gmail email address) |
| `MAIL_PASS` | Yes | - | SMTP password (Gmail app-specific password) |
| `KAFKA_BOOTSTRAP_SERVERS` | No | `localhost:9092` | Kafka broker address |

## Testing

```bash
mvn test
```

The service starts a Kafka consumer on the `user.registered` topic. If Kafka is not running locally, you'll see connection warnings—these are harmless for testing.

## Architecture

- **EventConsumer**: Listens to Kafka `user.registered` events
- **EmailSenderService**: Sends HTML verification emails via SMTP
- **KafkaConfig**: Configures Kafka consumer with JsonDeserializer
- **NotificationServiceApplication**: Spring Boot entry point

## Notes

- Credentials are read from environment variables for security
- Never commit `.env` to git (use `.env.example` as a template)
- Server runs on port `8082` by default (configurable via `spring.server.port`)

