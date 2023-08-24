# Production Files
This folder is copied by `/.github/workflows/deploy.yml`
and added to the branch `prod-config`, which can then be cloned by the production
server to deploy changes to the infrastructure of Schedge.

### Production layout
- Runs on a AWS Lightsail instance w/ 1GB RAM and 1vCPU
- Project at `/home/ubuntu/schedge`
- Cloned using `git clone -b prod-config --single-branch https://github.com/A1Liu/schedge`
- Folders/files that the environment uses:
  - `.env` file for environment variables
  - `.letsencrypt` folder for ACME (this is created for you)
  - `.build` folder for postgres DB (this is created for you)
 
## Useful Commands
All of these are running from `/home/ubuntu/schedge`

- Spin up the docker environment (also potentially update it if its already running)
  ```
  docker-compose -f docker-compose.yml up -d
  ```
- Print logs for a service in the docker compose
  ```
  docker-compose -f docker-compose.yml logs <service-name>
  ```
