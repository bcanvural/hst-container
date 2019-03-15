package client.packagename;

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.HttpMethod;

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
        setupHstRequest();
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
        //URI is the part before the query params so they should be stripped out. Setting this is mandatory
        getHstRequest().setRequestURI("/site/api/hello/" + user);
        getHstRequest().setMethod(HttpMethod.GET);
        String response = invokeFilter();
        Assert.assertEquals("Hello, World! " + user, response);
    }

    @Test
    public void testNewsEndpoint() throws Exception {
        getHstRequest().setRequestURI("/site/api/news");
        getHstRequest().setRequestURL(new StringBuffer("http://localhost:8080/site/api/news?_type=json"));
        getHstRequest().setMethod(HttpMethod.GET);
        String response = invokeFilter();
        ListItemPagination<NewsItemRep> pageable = new ObjectMapper().readValue(response, new TypeReference<ListItemPagination<NewsItemRep>>() {
        });

        Assert.assertEquals("Pageable didn't have enough results", 3, pageable.getItems().size());
    }

}
