# Dockerfile for universalresolver/driver-did-bid

FROM universalresolver/base-ubuntu

# build driver-did-bid

ADD . /opt/driver-did-bid

RUN cd /opt/driver-did-bid && mvn clean install package -N -DskipTests

# done

EXPOSE 8080

RUN chmod a+rx /opt/driver-did-bid/docker/run-driver-did-bid.sh
CMD "/opt/driver-did-bid/docker/run-driver-did-bid.sh"
