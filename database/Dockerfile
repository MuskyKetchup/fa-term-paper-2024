FROM mariadb:11.7.2-noble
ENV MARIADB_USER=krot 
ENV MARIADB_PASSWORD=1234 
ENV MARIADB_ROOT_PASSWORD=12345678
COPY ./1_db_chatdb.sql /docker-entrypoint-initdb.d/
COPY ./2_db_data.sql /docker-entrypoint-initdb.d/
