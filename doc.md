docker run --network mariadb-network --name mariadb-container -e MYSQL_ROOT_PASSWORD=my-secret-pw -d mariadb:latest
sudo docker build -t social-network-v1 .
sudo docker tag social-network-v1 nlgbao1340/social-network-v1:0.0.2
sudo docker push nlgbao1340/social-network-v1:0.0.2
sudo docker run -d --network mariadb-network  --name social-network-container -p 8080:8080 --env-file .env social-network-v1
