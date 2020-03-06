#!/bin/bash

# https://www.wissel.net/blog/2018/03/letsencrypt-java-keystore.html

sudo certbot renew
cat /etc/letsencrypt/live/schedge.torchnyu.com/*.pem > fullcert.pem
openssl pkcs12 -export -out fullcert.pkcs12 -in fullcert.pem
rm fullcert.pem
cp src/main/resources/empty.jks keystore.jks
keytool -v -importkeystore -srckeystore fullcert.pkcs12 -destkeystore keystore.jks -deststoretype JKS
rm fullcert.pkcs12
mv keystore.jks src/main/resources

./gradlew build
./schedge db serve
