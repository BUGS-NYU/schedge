#!/bin/bash

# https://www.wissel.net/blog/2018/03/letsencrypt-java-keystore.html

if [ "$1" = "" ]; then
  echo "Need to pass in argument 'domain', e.g. schedge.a1liu.com"
  exit
f

sudo certbot renew
cat "/etc/letsencrypt/live/$1/*.pem" > fullcert.pem
openssl pkcs12 -export -out fullcert.pkcs12 -in fullcert.pem
rm fullcert.pem
cp src/main/resources/empty.jks keystore.jks
keytool -v -importkeystore -srckeystore fullcert.pkcs12 -destkeystore keystore.jks -deststoretype JKS
rm fullcert.pkcs12
mv keystore.jks src/main/resources

./gradlew build
./schedge db serve
