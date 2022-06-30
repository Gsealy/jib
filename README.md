# Jib (With ocicrypt)

## What is Jib (With ocicrypt)?

Jib builds encrypted [OCI](https://github.com/opencontainers/image-spec) images for your Java applications without a Containerd daemon. It is available as plugins for [Maven](jib-maven-plugin) and as a Java library.

[Maven](https://maven.apache.org/): See documentation for [jib-maven-plugin](jib-maven-plugin).\
[Jib Core](jib-core): A general-purpose container-building library for Java.

## Support

> **Note**
> just support `maven-plugin` with tarball

* maven-plugin version: 3.2.1
* build encrypted image Tarball(just maven plugin)

## Goals

* **Fast** - Deploy your changes fast. Jib separates your application into multiple layers, splitting dependencies from classes. Now you don’t have to wait for Docker to rebuild your entire Java application - just deploy the layers that changed.

* **Reproducible** - Rebuilding your container image with the same contents always generates the same image. Never trigger an unnecessary update again.

* **Daemonless** - Reduce your CLI dependencies. Build your Docker image from within Maven or Gradle and push to any registry of your choice. *No more writing Dockerfiles and calling docker build/push.*

## Quickstart

* **Maven** - See the jib-maven-plugin [Quickstart](jib-maven-plugin#quickstart).

## Examples

The [examples](examples) directory includes the following examples (and more).
* [helloworld](examples/helloworld)
* [Spring Boot](examples/spring-boot)
* [Micronaut](examples/micronaut)
* [Multi-module project](examples/multi-module)
* [Spark Java using Java Agent](examples/java-agent)
* [ocicrypt](examples/ocicrypt)

## Thanks

* [Jib](https://github.com/GoogleContainerTools/jib)
* [ocicrypt](https://github.com/containers/ocicrypt)
