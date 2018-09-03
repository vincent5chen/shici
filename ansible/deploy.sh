#!/bin/bash

PROFILE=production

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
echo "working dir is set to: $DIR"

cd $DIR
cd ../web

echo "start maven build..."
mvn clean package

echo "will deploy shici for profile $PROFILE"
cd $DIR
ansible-playbook -i environments/$PROFILE/hosts.yml playbook.yml
