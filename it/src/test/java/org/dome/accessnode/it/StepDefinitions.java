package org.dome.accessnode.it;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.dome.accessnode.ApiException;
import org.dome.accessnode.api.ProductOfferingApi;
import org.dome.accessnode.api.ProductSpecificationApi;
import org.dome.accessnode.model.ProductSpecificationCreateVO;
import org.dome.accessnode.model.ProductSpecificationRefVO;
import org.dome.accessnode.model.ProductOfferingCreateVO;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class StepDefinitions {

    ProductOfferingApi productOfferingProvider;
    ProductOfferingApi productOfferingConsumer;
    org.dome.accessnode.model.ProductOfferingVO productOffering;

    ProductSpecificationApi productSpecificationProvider;
    ProductSpecificationApi productSpecificationConsumer;
    org.dome.accessnode.model.ProductSpecificationVO productSpecification;

    @Before
    public void waitForTheEnvironment() {
        productOfferingProvider = new ProductOfferingApi();
        productOfferingProvider.setCustomBaseUrl("http://localhost:8080/tmf-api/productCatalogManagement/v4");

        productOfferingConsumer = new ProductOfferingApi();
        productOfferingConsumer.setCustomBaseUrl("http://localhost:8081/tmf-api/productCatalogManagement/v4");

        productSpecificationProvider = new ProductSpecificationApi();
        productSpecificationProvider.setCustomBaseUrl("http://localhost:8080/tmf-api/productCatalogManagement/v4");

        productSpecificationConsumer = new ProductSpecificationApi();
        productSpecificationConsumer.setCustomBaseUrl("http://localhost:8081/tmf-api/productCatalogManagement/v4");

    }

    @Given("a provider and a consumer have deployed there access nodes.")
    public void checkProviderAndConsumerAvailable() {
        assertDoesNotThrow(() -> productOfferingProvider.listProductOffering(null, null, null),
                "The product catalog api should be available at the provider.");
        assertDoesNotThrow(() -> productOfferingConsumer.listProductOffering(null, null, null),
                "The product catalog api should be available at the consumer.");
    }

    @When("a product offering and product specification are created at the providers marketplace.")
    public void createCatalogAtProvider() throws ApiException {
        ProductSpecificationCreateVO productSpecificationCreateVO = getProductSpecificationCreateVO();
        productSpecification = productSpecificationProvider.createProductSpecification(productSpecificationCreateVO);

        ProductOfferingCreateVO productOfferingCreateVO = getProductOfferingCreateVO();
        productOffering = productOfferingProvider.createProductOffering(productOfferingCreateVO);
    }

    @Then("it should be available at the consumer marketplace, too.")
    public void checkCatalogAtConsumer() throws ApiException {
        await().atMost(Duration.of(30, ChronoUnit.SECONDS)).until(() -> checkProductOfferingExistence(productOffering.getId()));
        await().atMost(Duration.of(30, ChronoUnit.SECONDS)).until(() -> checkProductSpecificationExistence(productSpecification.getId()));

        assertEquals(productOffering, productOfferingConsumer.retrieveProductOffering(productOffering.getId(), null), "The product offering should be available at the consumers TMForum api.");
        assertEquals(productSpecification, productSpecificationConsumer.retrieveProductSpecification(productSpecification.getId(), null), "The product specification should be available at the consumers TMForum api.");

    }

    @NotNull
    private static ProductSpecificationCreateVO getProductSpecificationCreateVO() {
        ProductSpecificationCreateVO productSpecificationCreateVO = new ProductSpecificationCreateVO();
        productSpecificationCreateVO.setName("provider-product-specification");
        return productSpecificationCreateVO;
    }

    @NotNull
    private ProductOfferingCreateVO getProductOfferingCreateVO() {
        ProductSpecificationRefVO productSpecificationRef = new ProductSpecificationRefVO();
        productSpecificationRef.setId(productSpecification.getId());
        productSpecificationRef.setName(productSpecification.getName());
        productSpecificationRef.href(productSpecification.getHref());

        ProductOfferingCreateVO productOfferingCreateVO = new ProductOfferingCreateVO();
        productOfferingCreateVO.setName("provider-product-offering");
        productOfferingCreateVO.setLifecycleStatus("Launched");
        productOfferingCreateVO.setProductSpecification(productSpecificationRef);
        return productOfferingCreateVO;
    }

    private boolean checkProductOfferingExistence(String id) {
        try {
            return productOfferingConsumer.retrieveProductOffering(id, null) != null;
        } catch (ApiException e) {
            return false;
        }
    }

    private boolean checkProductSpecificationExistence(String id) {
        try {
            return productSpecificationConsumer.retrieveProductSpecification(id, null) != null;
        } catch (ApiException e) {
            return false;
        }
    }

}
