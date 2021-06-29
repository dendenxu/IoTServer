Red=$(tput setaf 1)
Green=$(tput setaf 2)
Yellow=$(tput setaf 3)
Blue=$(tput setaf 4)
Magenta=$(tput setaf 5)
Cyan=$(tput setaf 6)
Bold=$(tput bold)
NoColor=$(tput sgr0)


DB_FILE_BASE=${DB_FILE_BASE:-"mongodb_backup_20210629"}
DB_FILE_EXT=${DB_FILE_EXT:-".tar.gz"}

echo "${Yellow}DB_POPULATOR: set variable DB_FILE_BASE to change backup file base name, currently using ${DB_FILE_BASE}${NoColor}"
echo "${Yellow}Checking mongodb status...${NoColor}"
mongo --eval "db.getMongo().getDBNames().indexOf('iotserver');" > /dev/null

if [ $? -eq 0 ]; then
    echo "${Red}You've already populated the database iotserver! Exiting...${NoColor}"
    exit 1;
fi

echo "${Yellow}Extracting database backup from ${DB_FILE_BASE}${DB_FILE_EXT} ...${NoColor}"
tar -xvf "${DB_FILE_BASE}${DB_FILE_EXT}"
echo "${Yellow}Using mongorestore to restore the database...${NoColor}"
mongorestore "${DB_FILE_BASE}"
