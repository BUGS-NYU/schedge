# How to deploy the server
On Ubuntu:

```
sudo apt install docker-compose
sudo apt install default-jre
curl -s https://packagecloud.io/install/repositories/github/git-lfs/script.deb.sh | sudo bash
sudo apt install git-lfs
git clone https://github.com/A1Liu/schedge
git lfs pull
./gradlew build
sudo service docker start
sudo docker-compose build
sudo docker-compose -f src/build/docker/production.docker-compose.yml up -d
```
