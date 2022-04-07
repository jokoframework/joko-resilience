# Joko Resilience
Joko resilience is a project offering a flexible rate limiting system based on the token bucket Algorithm.  
It uses the bucket4j library providing  a layer to make the integration with Spring Boot applications easy.

### Spring Boot compatibility
The library has been tested with the following Spring boot versions:

* Spring Boot 1.5.x and above
* Spring Boot 2.x and above

### Java compatibility
The library has been tested with the following JDK versions:

* JDK 8
* JDK 11

### Configuring the rate limit system
### Dependencies
joko resilience is based on Spring HandlerInterceptor, so first we need the **spring-boot-starter-web** dependency:

```
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

Then we can add the joko-resilience dependency:

```
//JAVA 11
<dependency>
    <groupId>io.github.jokoframework</groupId>
    <artifactId>joko-resilience</artifactId>
    <version>1.0.0</version>
</dependency>
```

```
//JAVA 8
<dependency>
    <groupId>io.github.jokoframework</groupId>
    <artifactId>joko-resilience</artifactId>
    <version>1.0.0-jdk8</version>
</dependency>
```
#### General configuration
We can configure the Rate limiting system using the following properties:

| Property                                                 | Description                                                                                                                                                                 | Default Value                             | Accepted Values       |
|----------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------|-----------------------|
| joko.resilience.ratelimiter.enabled                      | Activate or deactivate the whole rate limiting system.                                                                                                                      | **true**                                  | **true** or **false** |
| joko.resilience.ratelimiter.interceptor.patterns         | Configures the global pattern or patterns for which he interceptors will be activated. Affects the **JokoRateLimitInterceptor** and the **JokoGlobalRateLimitInterceptor**. | /**                                       | string                |
| joko.resilience.ratelimiter.interceptor.patterns.exclude | Configures the global pattern or patterns that will be excluded from the interceptors. Affects the **JokoRateLimitInterceptor** and the **JokoGlobalRateLimitInterceptor**. | empty string                              | string                |
| joko.resilience.ratelimiter.resources.exclude            | Exclude requests to resources like css or images from the interceptors scope.                                                                                               | **true**                                  | **true** or **false** |
| joko.resilience.ratelimiter.token.capacity               | The number of request available for a bucket in a range of time (refill time). The default value is 10 requests.                                                            | 10                                        | int number            |
| joko.resilience.ratelimiter.token.refill                 | The number of request to refill after the refill time passed. The default value is the same of the capacity.                                                                | The same of capacity                      | int number            |
| joko.resilience.ratelimiter.token.refill.wait            | The range of time in seconds for the token refill to happen. The default value is 60 seconds.                                                                               | 60                                        | int number            |
| joko.resilience.ratelimiter.auth.header-name             | The name of the header to be used o generate a bucket identifier. We recommend using an authorization token or api key.                                                     | Authorization                             | string                |
| joko.resilience.ratelimiter.auth.jsession.cookie.enabled | Use the JSESSIONID cookie value to generate a bucket identifier. Useful for single layer applications using Spring Security                                                 | **false**                                 | **true** or **false** |
| joko.resilience.ratelimiter.error.message                | Message to show in the response when the request quota is exhausted.                                                                                                        | You have exhausted your API Request Quota | string                |

#### Interceptors
The rate limiting system is based on Spring interceptors:
There are 2 interceptor available to implement a rate limiting functionality:
* **JokoRateLimitInterceptor**: Works in conjunction with annotations o limit specific Request
* **JokoGlobalRateLimitInterceptor**: Works based on a path limiting all request that falls under the given path

#### Configuring the interceptors
First we need to configure the interceptors. Doing this is slightly different depending on which version of Spring
Boot we're using.

#### Spring Boot 2.x
For Spring boot 2 we have 3 special config classes we can extend. All we need to do 
is to extend this classes in a configuration component, and we're done. Ex:

**JokoRateLimiterConfig**: Inject the **JokoRateLimitInterceptor** for specific requests.
```
@Configuration
public class RateLimitingConfig extends JokoRateLimiterConfig {
}
```
**JokoGlobalRateLimiterConfig**: Inject the **JokoGlobalRateLimitInterceptor** for request patterns.
```
@Configuration
public class RateLimitingConfig extends JokoGlobalRateLimiterConfig {
}
```
**JokoHybridRateLimiterConfig**: Inject both interceptors.

```
@Configuration
public class RateLimitingConfig extends JokoHybridRateLimiterConfig {
}
```

#### Spring Boot 1.5.x
For Spring boot 1.5 we need to do it slightly different because of the Spring Boot core differences between versions
For his we can use the **JokoInterceptorUtils** component. Ex:

```
@Configuration
@ComponentScan("io.github.jokoframework.resilience.ratelimiter.config") // Need to scan he config package to inject necessary components
public class RateLimiterConfig extends WebMvcConfigurerAdapter {

    @Autowired
    private JokoInterceptorUtils jokoInterceptorUtils;

    // We add the inerceptor using he JokoInterceptorUtils component
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        jokoInterceptorUtils.addJokoRateLimitInterceptor(registry);
        jokoInterceptorUtils.addJokoGlobalRateLimitInterceptor(registry);
    }

}
```

#### Limiting Specific request
To limit specific request, we need to make use of the **@RateLimit** annotation in conjunction
with the **JokoRateLimitInterceptor**.

After the configuration is ready all we need to do is annotate the desired controller request:

```
@PutMapping("password/change")
@RateLimit
public ResponseEntity<ChangePasswordResponse> changePassword(@PathVariable("userId") Long userId,
                                                             @Valid @RequestBody PasswordChangeRequest passwordChangeRequest) {
    return ResponseEntity.ok(authenticationService.changePassword(userId, passwordChangeRequest));
}
```

We can also pass the rate limit attributes in the annotation itself to configure a request wih specific values:

* **tokenCapacity**: The number of request available for a bucket in a range of time (refill time).
* **tokenRefill**:The number of request to refill after the refill time passed.
* **refillWaitTime**:The range of time in seconds for the token refill to happen;

```
@PutMapping("password/change")
@RateLimit(tokenCapacity = 5, tokenRefill = 5, refillWaitTime = 30)
public ResponseEntity<ChangePasswordResponse> changePassword(@PathVariable("userId") Long userId,
                                                             @Valid @RequestBody PasswordChangeRequest passwordChangeRequest) {
    return ResponseEntity.ok(authenticationService.changePassword(userId, passwordChangeRequest));
}
```

## Building from source
The library can be generated from source for java 8 and 11 using prebuild gradle
tasks:

### JAVA 8
To build the jar for java 8 we can use the following gradle task:

```
./gradlew generateJava8Artifact
```

This will generate a jar file for the jdk 8 under the folder **build/libs** with the
**-jdk8** suffix. For example if the version is **1.0.0** the jar will be generated with
the following name:

```
/build/libs/joko-resilience-1.0.0-jdk8.jar
```

### JAVA 11
To build the jar for java 11 we can use the following gradle task:

```
./gradlew generateJava11Artifact
```

This will generate a jar file for the jdk 11 under the folder **build/libs**.
For example if the version is **1.0.0** the jar will be generated with
the following name:

```
/build/libs/joko-resilience-1.0.0.jar
```

## Publishing to maven repositories

### Publishing to local repository
The jar can be published directly to the default local maven repository using the
following gradle tasks:

###### JAVA 8
```
./gradlew publishJava8Local
```
###### JAVA 11
```
./gradlew publishJava11Local
```

>OBS: Publishing directly to a custom local maven repository folder is not yet supported

### Publishing to an artifactory
For publishing to an artifactory repository we need first to create a **gradle.properties** file in
the project **root**, containing the following props:

```
artifactory_contextUrl=https://yourartifactoryurl
artifactory_user=artifactoryuser
artifactory_password=artifactorypassword
```

Then the jar can be published directly to an artifactory repository using the
following gradle tasks:

###### JAVA 8
```
./gradlew publishJava8Artifactory
```
###### JAVA 11
```
./gradlew publishJava11Artifactory
```

>OBS: The jar files will be published under **libs-release-local** in the artifactory.
> This is not yet configurable and there is no direct snapshots support.
