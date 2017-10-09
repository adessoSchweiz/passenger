FROM airhacks/glassfish:v5

ADD target/passenger.war ${DEPLOYMENT_DIR}