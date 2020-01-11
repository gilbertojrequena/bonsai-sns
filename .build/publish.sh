#!/usr/bin/env bash

openssl aes-256-cbc -d -a -in .build/sec.enc -out sec.gpg -pass pass:"${SIGNING_DECRYPT_KEY}"

./gradlew uploadArchives -PossrhUsername="${CI_DEPLOY_USERNAME}" -PossrhPassword="${CI_DEPLOY_PASSWORD}" -Psigning.keyId="${SIGNING_KEY}" -Psigning.password="${SIGNING_PASSWORD}" -Psigning.secretKeyRingFile=sec.gpg