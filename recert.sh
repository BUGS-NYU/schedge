#!/bin/bash

sudo certbot renew

cat /etc/letsencrypt/live/schedge.torchnyu.com/*.pem > fullcert.pem
keytool -genkey -keyalg RSA -alias keystore -keystore empty.jks
keytool -delete -alias sfdcsec -keystore empty.jks
cp empty.jks keystore.jks
keytool -v -importkeystore -srckeystore fullcert.pkcs12 -destkeystore keystore.jks -deststoretype JKS

cp keystore.jks src/main/resources

./gradlew build
./schedge db serve
