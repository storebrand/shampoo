# Shampoo

A simple SOAP client.
This support both SOAP 1.1 and SOAP 1.2. 

Make sure you create the request using the correct version.

## Status
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/no.storebrand.shampoo/shampoo/badge.svg)](https://maven-badges.herokuapp.com/maven-central/no.storebrand.shampoo/shampoo/)

 
# Community

## Adopters

Are you using shampoo? Please consider opening a pull request to list your organization here:

* Storebrand
* Your Organization here

# Example

```java
package org.example;

import no.storebrand.shampoo.*;
import no.storebrand.shampoo.jdk.UrlConnectionSoapClient;

import static JDOM2Utils.*;

public class Main {
    public static void main(String[] args){        
        SoapClient client = new UrlConnectionSoapClient(URI.create(String.format("http://example.com/echo", port)));
        Result<SoapFault, SoapDocument> result = client.execute(SoapRequest.soap11(
                new SoapBody(elem("echo", "value")),
                java.util.Collections.emptyList(),
                SoapAction.of("http://example.com/echo")                 
        ));      
        
        //use result
    }
}

```
 
### Maven


```xml
<dependency>
  <groupId>no.storebrand.shampoo</groupId>
  <artifactId>shampoo</artifactId>
  <version>VERSION</version>
</dependency>
```


### Gradle
```groovy
compile 'no.storebrand.shampoo:shampoo:VERSION'

```

### SBT
```scala
libraryDependencies += "no.storebrand.shampoo" % "shampoo" % "VERSION"
```
