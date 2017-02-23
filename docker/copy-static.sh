#!/bin/sh

rm -rf ./nginx/root
mkdir ./nginx/root

cp -r ../web/src/main/webapp/static ./nginx/root/
cp ../web/src/main/webapp/robots.txt ./nginx/root/
cp ../web/src/main/webapp/favicon.ico ./nginx/root/
cp ../web/src/main/webapp/crossdomain.xml ./nginx/root/
