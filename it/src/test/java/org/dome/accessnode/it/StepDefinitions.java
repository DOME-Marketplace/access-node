package org.dome.accessnode.it;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import lombok.extern.slf4j.Slf4j;
import org.dome.accessnode.ApiException;
import org.dome.accessnode.api.ServiceCatalogApi;
import org.dome.accessnode.model.ServiceCatalogVO;
import org.junit.jupiter.api.Test;

import java.util.List;

@Slf4j
public class StepDefinitions {

    @Before
    public void waitForTheEnvironment() {
    }

    @Given("Test")
    public void test() throws ApiException {
        ServiceCatalogApi sca = new ServiceCatalogApi();
        sca.setCustomBaseUrl("http://localhost:8080");
        List<ServiceCatalogVO> serviceCatalogVOS = sca.listServiceCatalog(null, null, null);
    }

}
