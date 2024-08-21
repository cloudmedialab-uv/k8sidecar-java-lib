FROM adoptopenjdk/openjdk11:jre-11.0.24_8-alpine
RUN mkdir /app
COPY logging*.jar /app/app.jar
WORKDIR /app
CMD ["java","-jar","app.jar"]