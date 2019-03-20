package client.packagename;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.mock.web.DelegatingServletInputStream;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeCreator;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.POJONode;
import com.github.bcanvural.AbstractJaxrsTest;

import client.packagename.model.ListItemPagination;
import client.packagename.model.NewsDocument;
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

    @Test
    public void testCreateNewNews() throws Exception {
        getHstRequest().setRequestURI("/site/api/news/create");
        getHstRequest().setMethod(HttpMethod.POST);
        getHstRequest().setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode newsNode = mapper.createObjectNode();
        newsNode.put("title", "This is title");
        newsNode.put("introduction", "This is introduction");

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(mapper.writeValueAsBytes(newsNode));
        getHstRequest().setInputStream(new DelegatingServletInputStream(byteArrayInputStream));

        String response = invokeFilter();
        Assert.assertNotNull(response);
    }

}
