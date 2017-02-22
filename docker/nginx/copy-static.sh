#!/bin/sh

rm -rf root
mkdir root

cp -r ../../web/src/main/webapp/static ./root/
cp ../../web/src/main/webapp/robots.txt ./root/
cp ../../web/src/main/webapp/favicon.ico ./root/
cp ../../web/src/main/webapp/crossdomain.xml ./root/
