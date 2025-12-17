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


## LASDPC RUN
herminio@and08-vm2:$ source ~/.bashrc
herminio@and08-vm2:$ unset _JAVA_OPTIONS
herminio@and08-vm2:$ mvn clean package -DskipTests

$ docker compose down

$ docker compose up --build

### GIT PULL FORCE
git reset --hard HEAD
git pull










