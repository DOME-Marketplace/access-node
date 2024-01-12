package org.dome.accessnode.it;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.Awaitility;
import org.dome.accessnode.ApiException;
import org.dome.accessnode.api.ServiceCatalogApi;
import org.dome.accessnode.model.ServiceCatalogCreateVO;
import org.dome.accessnode.model.ServiceCatalogVO;
import org.junit.jupiter.api.Test;

import java.time.temporal.*;
import java.time.Duration;
import java.time.temporal.TemporalUnit;
import java.util.List;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class StepDefinitions {

    ServiceCatalogApi scaProvider;
    ServiceCatalogApi scaConsumer;

    ServiceCatalogVO scv;

    @Before
    public void waitForTheEnvironment() {
        scaProvider = new ServiceCatalogApi();
        scaProvider.setCustomBaseUrl("http://localhost:8080/tmf-api/serviceCatalogManagement/v4");

        scaConsumer = new ServiceCatalogApi();
        scaConsumer.setCustomBaseUrl("http://localhost:8081/tmf-api/serviceCatalogManagement/v4");
    }

    @Given("Test this")
    public void test() throws ApiException {
        log.warn("Hello");
        ServiceCatalogCreateVO scc = new ServiceCatalogCreateVO();
        scc.setName("my-catalog");
        scv = scaProvider.createServiceCatalog(scc);
    }

    @When("When it")
    public void when() {
        await().atMost(Duration.of(30, ChronoUnit.SECONDS)).until(() -> scaConsumer.listServiceCatalog(null, null, null).size() > 0);
    }

    @Then("Then it")
    public void then() throws ApiException {
        var scvC = scaConsumer.retrieveServiceCatalog(scv.getId(), null);
        assertEquals(scv, scvC);
    }

}
