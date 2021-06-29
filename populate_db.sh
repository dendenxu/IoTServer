#! /bin/bash

Red=$(tput setaf 1);
Green=$(tput setaf 2);
Yellow=$(tput setaf 3);
Blue=$(tput setaf 4);
Magenta=$(tput setaf 5);
Cyan=$(tput setaf 6);
Bold=$(tput bold);
NoColor=$(tput sgr0);

DB_FILE_BASE=${DB_FILE_BASE:-"mongodb_backup_20210629"};
DB_FILE_EXT=${DB_FILE_EXT:-".tar.gz"};

echo -e "${Yellow}$0:\nset variable DB_FILE_BASE to change backup file base name, currently using ${DB_FILE_BASE}${NoColor}\n";
echo -e "${Yellow}Checking mongodb status...${NoColor}\n";
INDEX_IOTSERVER=$(mongo --quiet --eval "db.getMongo().getDBNames().indexOf('iotserver');");

echo -e "${Yellow}If you later want a fresh restart, you can use";
echo -e "${Blue}mongo iotserver --eval "'"db.dropDatabase()"';
echo -e "${Yellow}to remove the current iotserver database and run this script again${NoColor}\n";

if [ "${INDEX_IOTSERVER}" != "-1" ]; then
    echo -e "${Red}You've already populated the database iotserver! Exiting...${NoColor}";
    exit 1;
fi

echo -e "${Yellow}Extracting database backup from ${DB_FILE_BASE}${DB_FILE_EXT} ...${NoColor}\n";
tar -xvf "${DB_FILE_BASE}${DB_FILE_EXT}";
echo -e "${Yellow}Using mongorestore to restore the database...${NoColor}\n";
mongorestore "${DB_FILE_BASE}";

echo -e "${Green}All OK, you can run your iotserver now.${NoColor}\n";
