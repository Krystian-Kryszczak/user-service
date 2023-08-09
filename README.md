## User Microservice
![CodeQL](https://github.com/Krystian-Kryszczak/user-service/workflows/CodeQL/badge.svg)
> This microservice is responsible for user management.
##### Programing language: Kotlin
##### Used technologies:

- Framework » [Micronaut (v. 4.0.3)](https://docs.micronaut.io/4.0.3/guide/index.html)

- DataBase » [Apache Cassandra](https://cassandra.apache.org)


- Communication » [REST](https://en.wikipedia.org/wiki/Representational_state_transfer)


- Containerization » [Docker](https://www.docker.com/)


- Testing
    - [Kotest](https://kotest.io/)
    - [Mockk](https://mockk.io/)
---

## Push To Docker Registry Workflow

Workflow file: [`.github/workflows/gradle.yml`](.github/workflows/gradle.yml)

### Workflow description
For pushes to the `master` branch, the workflow will:
1. Setup the build environment with respect to the selected java/graalvm version.
2. Login to docker registry based on provided configuration.
3. Build, tag and push Docker image with Micronaut application to the Docker container image.

### Dependencies on other GitHub Actions
- [Docker login](`https://github.com/docker/login-action`)(`docker/login`)
- [Setup GraalVM](`https://github.com/DeLaGuardo/setup-graalvm`)(`DeLaGuardo/setup-graalvm`)

### Setup
Add the following GitHub secrets:

| Name | Description |
| ---- | ----------- |
| DOCKER_USERNAME | Username for Docker registry authentication. |
| DOCKER_PASSWORD | Docker registry password. |
| DOCKER_REPOSITORY_PATH | Path to the docker image repository inside the registry, e.g. for the image `foo/bar/micronaut:0.1` it is `foo/bar`. |
| DOCKER_REGISTRY_URL | Docker registry url. |

#### DockerHub

- `DOCKER_USERNAME` - DockerHub username
- `DOCKER_PASSWORD` - DockerHub password or personal access token
- `DOCKER_REPOSITORY_PATH` - DockerHub organization or the username in case of personal registry
- `DOCKER_REGISTRY_URL` - No need to configure for DockerHub

> See [docker/login-action for DockerHub](https://github.com/docker/login-action#dockerhub)
