#######################################
#Using Docker compose (exec yml file)

$ mvn clean package -DskipTests

## RUN LOCAL HOST
$ java -jar target/MASProjectV25-0.0.1.jar

## RUN SERVER
$ docker-compose up --build

$ docker compose down

$ docker compose up --build

$ docker compose up



