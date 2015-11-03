# report-storage
report-storage is a [TestNG](http://testng.org) plugin which can be used to submit testing results to a user-defined remote storage case by case, with some case marking features as well.

This is a branch version target for iFlyTek internal use.

Note: report-storage requires a JAVA SE 1.8 or above environment to run.

## Download
report-storage now provides a SNAPSHOT download from ossrh repository:

```xml
<repository>
    <id>ossrh</id>
    <url>https://oss.sonatype.org/content/repositories/snapshots</url>
</repository>

<dependency>
    <groupId>com.github.yongchristophertang</groupId>
    <artifactId>report-storage</artifactId>
    <version>0.1-SNAPSHOT</version>
</dependency>
```

## Upload test results to a remote storage
The remote storage should provide an RESTful HTTP URL for accepting upcoming test results case by case. The configuration of this URL can be set in an application.conf/application.properties/application.json file residing in the classpath. All test contexts regarding each test case (including configuration) will be packed in a json string and be posted to the configured URL.

Take application.conf as an example, more information about how to set other format can be found in documents of [Typesage Config](https://github.com/typesafehub/config):

```
report {
  storage {
    protocol=http
    host=127.0.0.1
    port=8080
    path=/api
  }
}
```

## Enhanced test method annotations
Two more specific annotations which can be applied to a test method are developed in this project as well:

```java
@Bug("bug_id")
@TestCase(value="case description", expected="expected results")
@Test
public void testCaseMethod() {}
```

The Bug annotation is used to mark the Bug ID which this test case identifies. The TestCase annotation is used to add case description and expected results of the annotated test case method. All the three messages will be integrated with original test case information and sent to remote storage.
