## Configuration

Create the PostgreSQL database:
```bash
psql -U postgres
CREATE DATABASE coding_platform;
\q
```

Copy `application.properties.example` to `application.properties` in the same directory:
```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

Fill in your database credentials and generate a JWT secret:
```bash
openssl rand -base64 32
```