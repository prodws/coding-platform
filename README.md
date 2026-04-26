## Configuration

Create the PostgreSQL database:
```bash
psql -U postgres
CREATE DATABASE coding_platform;
\q
```

Set the required environment variables:
```bash
export DB_PASSWORD=your_database_password
export JWT_SECRET=$(openssl rand -base64 32)
```

Build the test-runner image:
> ⚠️ Docker must be installed and running locally
```bash
docker build -t java-test-runner .
```