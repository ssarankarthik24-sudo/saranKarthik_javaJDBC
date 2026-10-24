# KEC Academic System

## Database configuration

The application connects to a local MySQL database named `kec_academic` by default.
Set `KEC_DB_PASSWORD` before running the application. Optionally set `KEC_DB_USER`
and `KEC_DB_URL` to override the default MySQL user (`root`) and JDBC URL
(`jdbc:mysql://localhost:3306/kec_academic`).

## Build and test

Run `mvn test` to compile the project and execute its tests.
