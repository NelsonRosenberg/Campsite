FROM adoptopenjdk/openjdk11:latest
VOLUME /tmp

# Create app directory
WORKDIR /opt/app

# Adding app jar
COPY campsite.jar /opt/app/campsite.jar

# Ports
EXPOSE 8080

# Run server
RUN ls -la
ENTRYPOINT ["java", "-jar", "-Duser.timezone=America/Toronto", "/opt/app/campsite.jar"]