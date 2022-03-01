# Momento Development

To publish to artifactory, you will need to get your encrypted password
from the artifactory console under "User Profile".

You will need to publish using JDK 11, not JDK 17.  Also, there are
some failing int tests at the time of this writing so you may need to
skip the tests.  e.g.:

```
sdk use java 11.0.11.hs-adpt
mvn deploy -DskipTests
```


For auth, you need to set up a `~/.m2/settings.xml` that looks like this
(replace YOUR_USERNAME and YOUR_ENCRYPTED_PASSWORD.  should also be
possible to do this with env vars for CICD):

```
<?xml version="1.0" encoding="UTF-8"?>
<settings xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.2.0 http://maven.apache.org/xsd/settings-1.2.0.xsd" xmlns="http://maven.apache.org/SETTINGS/1.2.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <servers>
    <server>
      <username>YOUR_USERNAME</username>
      <password>YOUR_ENCRYPTED_PASSWORD</password>
      <id>momento</id>
    </server>
    <server>
      <username>YOUR_USERNAME</username>
      <password>YOUR_ENCRYPTED_PASSWORD</password>
      <id>momento-snapshots</id>
    </server>
  </servers>
  <profiles>
    <profile>
      <repositories>
        <repository>
          <snapshots>
            <enabled>false</enabled>
          </snapshots>
          <id>momento</id>
          <name>maven-local</name>
          <url>https://momento.jfrog.io/artifactory/maven-local</url>
        </repository>
        <repository>
          <snapshots />
          <id>momento-snapshots</id>
          <name>maven-local</name>
          <url>https://momento.jfrog.io/artifactory/maven-local</url>
        </repository>
      </repositories>
      <pluginRepositories>
        <pluginRepository>
          <snapshots>
            <enabled>false</enabled>
          </snapshots>
          <id>momento</id>
          <name>maven-local</name>
          <url>https://momento.jfrog.io/artifactory/maven-local</url>
        </pluginRepository>
        <pluginRepository>
          <snapshots />
          <id>momento-snapshots</id>
          <name>maven-local</name>
          <url>https://momento.jfrog.io/artifactory/maven-local</url>
        </pluginRepository>
      </pluginRepositories>
      <id>artifactory</id>
    </profile>
  </profiles>
  <activeProfiles>
    <activeProfile>artifactory</activeProfile>
  </activeProfiles>
</settings>

```
