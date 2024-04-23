FROM openjdk:17
VOLUME /tmp
EXPOSE 8021
ADD ./target/spring-boot-webflu-ms-cuenta-banco-0.0.1-SNAPSHOT.jar ms.bank.account.jar
ENTRYPOINT ["java","-jar","/ms.bank.account.jar"]