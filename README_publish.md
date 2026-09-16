# Momento Development

To publish to the Sonatype Central Portal snapshot repo, you will need a Central Portal user token
(https://central.sonatype.com/usertoken), and snapshot publishing must be enabled for the
`software.momento` namespace in the Portal. Snapshots are purged after ~90 days.

**IMPORTANT**: You will need to publish using JDK 17 or newer (Spring 7 requires it).  e.g.:

```
sdk use java 17.0.2-open
mvn deploy
```

For a full release build (`-DperformRelease=true`, which attaches sources, javadoc and signatures) also pass
`-Pxmemcached`.  The integration-test modules supply their test sources from an `activeByDefault` profile, and Maven
turns that off as soon as another profile in the same pom activates -- which `performRelease` does.


For auth, you need to set up a `~/.m2/settings.xml` that looks like this
(replace YOUR_TOKEN_USERNAME and YOUR_TOKEN_PASSWORD with the user token values.  should also be
possible to do this with env vars for CICD):

```
<?xml version="1.0" encoding="UTF-8"?>
<settings xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.2.0 http://maven.apache.org/xsd/settings-1.2.0.xsd" xmlns="http://maven.apache.org/SETTINGS/1.2.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <servers>
    <server>
      <username>YOUR_TOKEN_USERNAME</username>
      <password>YOUR_TOKEN_PASSWORD</password>
      <id>central</id>
    </server>
  </servers>
</settings>

```
