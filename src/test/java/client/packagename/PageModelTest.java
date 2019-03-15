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
