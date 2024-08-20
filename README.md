# Sidecar Library

The Sidecar library is a robust and efficient Java package that enables effortless deployment of a server for forwarding HTTP requests to an incremented port within the same host.

This repo contains the library and two examples of usage. You can inspect the code of the library, modify and build locally or you can go directly to the examples (as the artifact is available in the Github Apache Maven repository).

## Prerequisites

-   [Java](https://www.java.com/en/download/)


The Sidecar library provides two custom function types for handling HTTP requests and responses: `TriFunction` and `QuaFunction`.

-   `TriFunction` takes in an HTTP request, an HTTP response writer, and a FilterChain.
-   `QuaFunction` takes in an HTTP request, an HTTP response writer, a cloud event, and a FilterChain.

To use the Sidecar library, define your functions based on the `TriFunction` or `QuaFunction` type. Then, instantiate a `SidecarFilter` passing the `TriFunction` or `QuaFunction` object in the constructor. Finally, call the `listen` method on your `SidecarFilter` instance.

Here is a high-level example:

```java
TriFunction<HttpServletRequest, HttpServletResponse, FilterChain, Void> userFunction =
  BasicAuthMiddleware::authenticate;
SidecarFilter server = new SidecarFilter(userFunction);
```
In that code, authenticate is a function that receives three arguments of type HttpServletRequest, HttpServletResponse, and FilterChain (thus compatible with the functional interface TriFunction).

Here is a high-level example using lambdas:

```java
SidecarFilter server = new SidecarFilter((req, res, chain) -> {
    // Code to deal with the request, pass the request to the next object in the chain,
    // and do something with the response before returning
})
```
## Build the library and install locally:

The library can be built and installed locally with the following command:

```bash
mvn clean package install
```
### Use the local library in a project

To use the library as a dependency in you project, add this to the file pom.xml:

```xml
<dependency>
  <groupId>k8sidecar.java.lib</groupId>
  <artifactId>sidecarlib</artifactId>
  <version>1.0.2</version>
</dependency>
```

## Use the library from Github Apache Maven repo

Ensure you have [Github read-package](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry) creadentials installed on your machine and your `JAVA CLASSPATH` is set.

Add this code to the pom.xml file:
```xml
  <dependencies>
   <dependency>
      <groupId>k8sidecar.java.lib</groupId>
      <artifactId>sidecarlib</artifactId>
      <version>1.0.2</version>
    </dependency>
  </dependencies>

  <repositories>
    <repository>
      <id>github</id>
      <url>https://maven.pkg.github.com/cloudmedialab-uv/k8sidecar-java-lib</url>
      <snapshots>
        <enabled>true</enabled>
      </snapshots>
    </repository>
  </repositories>
```

# Examples that use the library to implement proxy sidecars

## Authentication sidecar
This is an example of the use of a TriFunction to check if the request is authenticated.
This sidecar can be configured with environment variables:
 - AUTH_TOKEN_NAME: with the name of the header
 - AUTH_TOKEN_VALUE: with the value of that header

If the header is present, and its value matches the configured value then the request is
passed to the next sidecar (or application). Otherwise, this sidecar returns a 401 response code
(unauthorized).

We provide an image of this sidecar in dockerhub `cloudmedialab/sidecar-authentication:1.0.0`

The sample code is in in [folder](examples/authentication).

## Logging

This is an example of the use of a TriFunction that can be used to show information about the requests.

The level of logging performed in this sidecar can be controlled with the following environment variable:
 - LOGGING_LEVEL: the value can be DEBUG, INFO or PROD.

Information about the request is logged (in this simple example it is shown in the standard output) and then the request is passed to the next sidecar (or application).

We provide an image of this sidecar in dockerhub `cloudmedialab/sidecar-logging:1.0.0`

The sample code is in in [folder](examples/logging).
