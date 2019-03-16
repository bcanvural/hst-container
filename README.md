# Hst-TestContainer

Start an hst-container from scratch and potentially run tests against different hst pipelines

### Example with pagemodel api (under test directory):

```java
package client.packagename;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.bcanvural.AbstractPageModelTest;

/**
 * A user (client) of the testing library providing his/her own config/content
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PageModelTest extends AbstractPageModelTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(PageModelTest.class);

    @BeforeAll
    public void init() {
        super.init();
    }

    @AfterAll
    public void destroy() {
        super.destroy();
    }

    @Override
    protected String getAnnotatedHstBeansClasses() {
        return "classpath*:client/packagename/beans/*.class,";
    }

    @Override
    protected List<String> contributeSpringConfigurationLocations() {
        return Arrays.asList("/client/packagename/*.xml");
    }

    @Override
    protected List<String> contributeAddonModulePaths() {
        return null;
    }

    @Test
    public void test() throws IOException {
        getHstRequest().setRequestURI("/site/resourceapi/news");
        getHstRequest().setQueryString("_hn:type=component-rendering&_hn:ref=r5_r1_r1");
        String response1 = invokeFilter();
        String response2 = invokeFilter();
    }
}

```

### Example with Jaxrsrestplainpipeline (under test directory):

```java
package client.packagename;

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bcanvural.AbstractJaxrsTest;

import client.packagename.model.ListItemPagination;
import client.packagename.model.NewsItemRep;
import junit.framework.Assert;

/**
 * A user (client) of the testing library providing his/her own config/content
 */

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JaxrsTest extends AbstractJaxrsTest {

    @BeforeAll
    public void init() {
        super.init();
    }

    @BeforeEach
    public void beforeEach() {
        setupForNewRequest();
    }

    private void setupForNewRequest() {
        setupHstRequest();
        getHstRequest().setHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
        getHstRequest().setMethod(HttpMethod.GET);
        setupServletContext();
        setupHstResponse();
    }


    @Override
    protected String getAnnotatedHstBeansClasses() {
        return "classpath*:client/packagename/model/*.class,";
    }

    @Override
    protected List<String> contributeSpringConfigurationLocations() {
        return Arrays.asList("/client/packagename/custom-jaxrs.xml", "/client/packagename/rest-resources.xml");
    }

    @Override
    protected List<String> contributeAddonModulePaths() {
        return null;
    }

    @Test
    public void testUserEndpoint() {
        String user = "baris";
        getHstRequest().setRequestURI("/site/api/hello/" + user);
        getHstRequest().setMethod(HttpMethod.GET);
        String response = invokeFilter();
        Assert.assertEquals("Hello, World! " + user, response);
    }

    @Test
    public void testNewsEndpoint() throws Exception {
        getHstRequest().setRequestURI("/site/api/news");
        getHstRequest().setMethod(HttpMethod.GET);
        String response = invokeFilter();
        ListItemPagination<NewsItemRep> pageable = new ObjectMapper().readValue(response, new TypeReference<ListItemPagination<NewsItemRep>>() {
        });
        Assert.assertEquals("Pageable didn't have enough results", 3, pageable.getItems().size());
    }

}
```


### Example spring config

```xml

<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-4.1.xsd">

  <bean id="contributedCndResourcesPatterns" class="java.util.ArrayList">
    <constructor-arg>
      <list>
        <value>classpath*:client/packagename/namespaces/**/*.cnd</value>
      </list>
    </constructor-arg>
  </bean>

  <bean id="contributedYamlResourcesPatterns" class="java.util.ArrayList">
    <constructor-arg>
      <list>
        <value>classpath*:client/packagename/imports/**/*.yaml</value>
      </list>
    </constructor-arg>
  </bean>

</beans>

```