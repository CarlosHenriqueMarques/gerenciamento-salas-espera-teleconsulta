# WildFly Jakarta EE 10 com JDK 21
FROM quay.io/wildfly/wildfly:34.0.1.Final-jdk21

# Copia o WAR para a pasta de deploys
COPY target/teleconsulta.war /opt/jboss/wildfly/standalone/deployments/

# (Opcional) garante bind em todas as interfaces
# ENTRYPOINT ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0"]
