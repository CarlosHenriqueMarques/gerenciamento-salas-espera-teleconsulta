FROM quay.io/wildfly/wildfly:34.0.1.Final-jdk21

#USER jboss
#ENV JBOSS_HOME=/opt/jboss/wildfly

# 1) Instala o módulo do driver SQLite
#RUN mkdir -p ${JBOSS_HOME}/modules/system/layers/base/org/sqlite/main
#COPY target/sqlite/sqlite-jdbc.jar ${JBOSS_HOME}/modules/system/layers/base/org/sqlite/main/sqlite-jdbc.jar
#COPY src/main/docker/modules/org/sqlite/main/module.xml ${JBOSS_HOME}/modules/system/layers/base/org/sqlite/main/module.xml

# 2) Deploy do WAR
#COPY target/teleconsulta.war ${JBOSS_HOME}/standalone/deployments/

#FROM quay.io/wildfly/wildfly:34.0.1.Final


USER root

# --- Módulo do SQLite ---
RUN mkdir -p ${JBOSS_HOME}/modules/system/layers/base/org/sqlite/main
COPY target/sqlite/sqlite-jdbc.jar ${JBOSS_HOME}/modules/system/layers/base/org/sqlite/main/sqlite-jdbc.jar
COPY src/main/docker/modules/org/sqlite/main/module.xml ${JBOSS_HOME}/modules/system/layers/base/org/sqlite/main/module.xml
RUN chown -R jboss:jboss ${JBOSS_HOME}/modules/system/layers/base/org/sqlite

# --- Script CLI para registrar driver + DataSource ---
COPY src/main/docker/sqlite-ds.cli /opt/jboss/sqlite-ds.cli
RUN sed -i 's/\r$//' /opt/jboss/sqlite-ds.cli && chown jboss:jboss /opt/jboss/sqlite-ds.cli

# Executa o CLI em modo embedded (altera standalone.xml na imagem)
USER jboss
RUN ${JBOSS_HOME}/bin/jboss-cli.sh --echo-command --file=/opt/jboss/sqlite-ds.cli

# --- Deploy do WAR ---
USER root
COPY target/teleconsulta.war ${JBOSS_HOME}/standalone/deployments/teleconsulta.war
RUN chown jboss:jboss ${JBOSS_HOME}/standalone/deployments/teleconsulta.war
USER jboss

