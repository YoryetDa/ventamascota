FROM openjdk:21-ea-24-oracle

WORKDIR /app
# Copiar el jar empaquetado desde el directorio de construcción local al filesystem del contenedor
COPY target/ventas-0.0.1-SNAPSHOT.jar app.jar
COPY Wallet_GD7WQOFG0B27KH77 /app/oracle_wallet


# Exponer el puerto que utiliza tu aplicación, generalmente es el 8080 para aplicaciones web Spring Boot
EXPOSE 8080

CMD ["java","-jar","app.jar"]