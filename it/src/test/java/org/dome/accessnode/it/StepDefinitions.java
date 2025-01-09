package org.dome.accessnode.it;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.dome.accessnode.ApiException;
import org.dome.accessnode.api.CatalogApi;
import org.dome.accessnode.model.CatalogCreateVO;
import org.dome.accessnode.model.CatalogVO;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class StepDefinitions {

    CatalogApi catalogProvider;
    CatalogApi catalogConsumer;
    CatalogVO catalog;

    @Before
    public void waitForTheEnvironment() {
        catalogProvider = new CatalogApi();
        catalogProvider.setCustomBaseUrl("http://localhost:8080/tmf-api/catalogManagement/v4");

        catalogConsumer = new CatalogApi();
        catalogConsumer.setCustomBaseUrl("http://localhost:8081/tmf-api/catalogManagement/v4");
    }

    @Given("a provider and a consumer have deployed there access nodes.")
    public void checkProviderAndConsumerAvailable() {
        assertDoesNotThrow(() -> catalogProvider.listCatalog(null, null, null),
                "The product catalog api should be available at the provider.");
        assertDoesNotThrow(() -> catalogConsumer.listCatalog(null, null, null),
                "The product catalog api should be available at the consumer.");
    }

    @When("a catalog is created at the providers marketplace.")
    public void createCatalogAtProvider() throws ApiException {
        CatalogCreateVO catalogCreateVO = new CatalogCreateVO();
        catalogCreateVO.setName("provider-catalog");
        catalog = catalogProvider.createCatalog(catalogCreateVO);
    }

    @Then("it should be available at the consumer marketplace, too.")
    public void checkCatalogAtConsumer() throws ApiException {
        await().atMost(Duration.of(30, ChronoUnit.SECONDS)).until(() -> checkCatalogExistence(catalog.getId()));

        assertEquals(catalog, catalogConsumer.retrieveCatalog(catalog.getId(), null), "The catalog should be available at the consumers TMForum api.");
    }

    private boolean checkCatalogExistence(String id) {
        try {
            return catalogConsumer.retrieveCatalog(id, null) != null;
        } catch (ApiException e) {
            return false;
        }
    }

}
