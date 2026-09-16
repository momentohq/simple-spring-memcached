SIMPLE SPRING MEMCACHED 5.0.0
------------------------------------
https://github.com/momentohq/simple-spring-memcached


To build project and execute (integration) tests two memcached instances are required (on localhost, ports 11211 and 11212). By default two embedded 
memcached (jmemcached) instances are used. Those instances are started on ports 11211 and 11212 at the beginning of integration tests and stopped after 
by maven plugin (jmemcached-maven-plugin). No need to install external memcached to build project and run integration tests. 
To use external memcached set maven property: -Djmemcached.disable=true.
 memcached -d -m 256 -l 127.0.0.1 -p 11211
 memcached -d -m 256 -l 127.0.0.1 -p 11212

Currently project can use one of four available providers:
 for xmemcached use: 
   mvn clean package -Pxmemcached
 for spymemcached use:
   mvn clean package -Pspymemcached
 for aws-elasticache use:
   mvn clean package -Paws-elasticache
 for momento use:
   mvn clean package (the Momento provider tests require a Momento API key, see momento-provider)
Above maven and spring profile settings only define what provider will be used in integration tests. 
In all cases created artifacts support all providers.
   
   
The project requires JDK 17 or newer to build (Spring 7 requires it).

Core modules of SSM: simple-spring-memcached, spymemcached-provider, xmemcached-provider, aws-elasticache-provider and momento-provider require Spring 7.0.x.
The spring-cache module which provides integration with Spring Cache abstraction requires Spring 7.0.x as well.
