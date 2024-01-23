package org.dome.accessnode.it;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.dome.accessnode.ApiException;
import org.dome.accessnode.api.ServiceCatalogApi;
import org.dome.accessnode.model.ServiceCatalogCreateVO;
import org.dome.accessnode.model.ServiceCatalogVO;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class StepDefinitions {

    ServiceCatalogApi scaProvider;
    ServiceCatalogApi scaConsumer;

    ServiceCatalogVO serviceCatalog;

    @Before
    public void waitForTheEnvironment() {
        scaProvider = new ServiceCatalogApi();
        scaProvider.setCustomBaseUrl("http://localhost:8080/tmf-api/serviceCatalogManagement/v4");

        scaConsumer = new ServiceCatalogApi();
        scaConsumer.setCustomBaseUrl("http://localhost:8081/tmf-api/serviceCatalogManagement/v4");
    }

    @Given("a provider and a consumer have deployed there access nodes.")
    public void checkProviderAndConsumerAvailable() throws ApiException {
        assertDoesNotThrow(() -> scaProvider.listServiceCatalog(null, null, null),
                "The service catalog api should be available at the provider.");
        assertDoesNotThrow(() -> scaConsumer.listServiceCatalog(null, null, null),
                "The service catalog api should be available at the consumer.");

    }

    @When("a catalog is created at the providers marketplace.")
    public void createServiceCatalogAtProvider() throws ApiException {
        ServiceCatalogCreateVO scc = new ServiceCatalogCreateVO();
        scc.setName("provider-catalog");
        serviceCatalog = scaProvider.createServiceCatalog(scc);
    }

    @Then("it should be available at the consumer marketplace, too.")
    public void checkServiceCatalogAtConsumer() throws ApiException {
        await().atMost(Duration.of(30, ChronoUnit.SECONDS)).until(() -> checkCatalogExistence(serviceCatalog.getId()));

        assertEquals(serviceCatalog, scaConsumer.retrieveServiceCatalog(serviceCatalog.getId(), null), "The catalog should be available at the consumers tmforum api.");
    }

    private boolean checkCatalogExistence(String id) {
        try {
            return scaConsumer.retrieveServiceCatalog(id, null) != null;
        } catch (ApiException e) {
            return false;
        }

    }

}
