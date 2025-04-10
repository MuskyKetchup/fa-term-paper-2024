#/bin/sh
CHAT_PODNAME=chat-pod
CHAT_IMAGE=docker.io/openjdk:23-slim-bookworm
CHAT_NAME=chat-server
CHAT_BUILD_DIR=./target/
CHAT_BUILD_NAME=chat-server.jar
# FORGEJO_USERID=$(id -u)
#FORGEJO_USERID=1065537
# FORGEJO_VOLUME=/mnt/raid_front/forgejo/data
DB_NAME=chat-db
# DB_IMAGE=mariadb:11.7.2-noble
DB_IMAGE=mariadb-chat-test
# DB_VOLUME=./target/db-data/
DB_VOLUME=chat-db-volume
DB_QUERY=./database/
# mkdir -p $DB_VOLUME
#

if [[ "$1" == *"--prune"* ]]; then
        podman pod stop $CHAT_PODNAME; podman pod rm $CHAT_PODNAME 
        podman volume rm $DB_VOLUME
        # podman stop $DB_NAME;podman rm $DB_NAME
        # podman stop $CHAT_NAME;podman rm $CHAT_NAME
fi
if podman pod exists $CHAT_PODNAME; then
        echo You previously created the pod
        echo HINT!:
        echo    If you want to prun it, just run
        echo    podman pod stop $CHAT_PODNAME; podman pod rm $CHAT_PODNAME 
        echo Starting the previously create POD
        podman pod start $CHAT_PODNAME
        exit 0
fi

podman pod create --name $CHAT_PODNAME \
        -p 12345:12345/udp \
        -p 12345:12345/tcp \

#podman volume create $FORGEJO_VOLUME
podman build ./database --tag $DB_IMAGE
podman run\
        --name $DB_NAME \
        -d --restart=always --pod=$CHAT_PODNAME \
        -e MARIADB_USER=krot \
        -e MARIADB_PASSWORD=1234 \
        -e MARIADB_ROOT_PASSWORD=12345678 \
        -e MYSQL_TCP_PORT=3307 \
        --volume=$DB_VOLUME:/var/lib/mysql \
        localhost/$DB_IMAGE
# We sleep a bit to give time for database to init
sleep 20;
podman run\
        --name $CHAT_NAME \
        -d --restart=always --pod=$CHAT_PODNAME \
        --volume=$CHAT_BUILD_DIR:/data:U \
        --volume=/etc/timezone:/etc/timezone:ro \
        --volume=/etc/localtime:/etc/localtime:ro \
        $CHAT_IMAGE java -jar /data/$CHAT_BUILD_NAME
