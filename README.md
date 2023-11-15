# Sidecar Library

The Sidecar library is a robust and efficient Java package that enables effortless deployment of a server for forwarding HTTP requests to an incremented port within the same host.

## Prerequisites

-   [Java](https://www.java.com/en/download/)

## Installation

To install the Sidecar library, Add this to pom.xm:

```xml
<dependency>
  <groupId>k8sidecar.java.lib</groupId>
  <artifactId>sidecarlib</artifactId>
  <version>1.0.0</version>
</dependency>
```

Ensure you have [Github read-package](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry) creadentials installed on your machine and your `JAVA CLASSPATH` is set.

## Usage

he Sidecar library provides two custom function types for handling HTTP requests and responses: `TriFunction` and `QuaFunction`.

-   `TriFunction` takes in an HTTP request, an HTTP response writer, and a FilterChain.
-   `QuaFunction` takes in an HTTP request, an HTTP response writer, a cloud event, and a FilterChain.

To use the Sidecar library, define your functions based on the `TriFunction` or `QuaFunction` type. Then, instantiate a `SidecarFilter` struct and assign your function to the `TriFunction` or `QuaFunction` field. Finally, call the Listen method on your SidecarFilter instance.

Here is a high-level example using classes:

```java
TriFunction<HttpServletRequest, HttpServletResponse, FilterChain, Void> userFunction =
  BasicAuthMiddleware::authenticate;
SidecarFilter server = new SidecarFilter(userFunction);
```

For a detailed [usage example](https://github/).

Here is a high-level example using lambdas:

```java
SidecarFilter server = new SidecarFilter((req, res, chain) -> {
    //rest of your code
})
```

For a detailed [usage example](https://github/).
