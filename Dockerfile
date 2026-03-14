FROM gradle:8.5-jdk21
WORKDIR /app
COPY . .
RUN ./gradlew distZip
WORKDIR /app/build/distributions/
RUN unzip steamgifts-1.0-SNAPSHOT.zip -d /app
WORKDIR /app/steamgifts-1.0-SNAPSHOT/bin
CMD ["./steamgifts"]
