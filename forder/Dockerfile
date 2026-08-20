FROM maven:3.9-eclipse-temurin-21-jammy

WORKDIR /app

RUN apt-get update && \
    DEBIAN_FRONTEND=noninteractive apt-get install -y xpra && \
    rm -rf /var/lib/apt/lists/*

COPY pom.xml .

RUN mvn dependency:go-offline

COPY src ./src

RUN mvn clean compile

EXPOSE 10000

CMD ["xpra", "start", ":100", \
     "--start-child=mvn javafx:run", \
     "--bind-tcp=0.0.0.0:10000", \
     "--html=on", \
     "--exit-with-children=yes", \
     "--daemon=no"]