#!/bin/sh

DB="shici"
cd ..
mkdir -p backup
cd backup
OUTPUT="dump-shici-$(date +"%Y-%m-%d_%H_%M_%S").sql"

echo "db dump require MySQL root password."
mysqldump --user=root -p --skip-opt --add-drop-table --default-character-set=utf8 --quick $DB > $OUTPUT

tar czvf $OUTPUT.tar.gz $OUTPUT

rm -f $OUTPUT

echo "db dumped in ../backup"

