FROM openjdk:8-jre-alpine

ARG BONSAI_SNS_VERSION
RUN apk add --no-cache curl ca-certificates
RUN mkdir -p /opt/bonsai-sns
RUN curl -sfLo /opt/bonsai-sns/bonsai-sns.jar https://repo1.maven.org/maven2/io/github/gilbertojrequena/bonsai-sns/${BONSAI_SNS_VERSION}/bonsai-sns-${BONSAI_SNS_VERSION}.jar
COPY ./bonsai-sns.conf /opt/bonsai-sns/bonsai-sns.conf

WORKDIR /opt/bonsai-sns

EXPOSE 7979

ENTRYPOINT [ "/usr/bin/java", "-Dconfig.file=/opt/bonsai-sns/bonsai-sns.conf", "-jar", "bonsai-sns.jar" ]
