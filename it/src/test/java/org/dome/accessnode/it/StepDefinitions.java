package org.dome.accessnode.it;

import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.dome.accessnode.ApiException;
import org.dome.accessnode.api.ProductOfferingApi;
import org.dome.accessnode.api.ProductSpecificationApi;
import org.dome.accessnode.model.ProductOfferingCreateVO;
import org.dome.accessnode.model.ProductSpecificationCreateVO;
import org.dome.accessnode.model.ProductSpecificationRefVO;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collection;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class StepDefinitions {

    /**
     * Replication has to clear the verifier auth chain before anything crosses between nodes,
     * so this is well above the round-trip itself.
     */
    private static final Duration REPLICATION_TIMEOUT = Duration.of(120, ChronoUnit.SECONDS);

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

    @When("a product specification is created at the providers marketplace.")
    public void aProductSpecificationIsCreatedAtTheProvidersMarketplace() throws ApiException {
        ProductSpecificationCreateVO productSpecificationCreateVO = getProductSpecificationCreateVO();
        productSpecification = productSpecificationProvider.createProductSpecification(productSpecificationCreateVO);
    }

    @And("a product offering related to the previous product specification is created at the providers marketplace.")
    public void aProductOfferingRelatedToThePreviousProductSpecificationIsCreatedAtTheProvidersMarketplace() throws ApiException {
        ProductOfferingCreateVO productOfferingCreateVO = getProductOfferingCreateVO();
        productOffering = productOfferingProvider.createProductOffering(productOfferingCreateVO);
    }

    @Then("they should be available at the consumer marketplace, too.")
    public void checkCatalogAtConsumer() throws ApiException {
        await().atMost(REPLICATION_TIMEOUT).until(() -> checkProductOfferingExistence(productOffering.getId()));
        await().atMost(REPLICATION_TIMEOUT).until(() -> checkProductSpecificationExistence(productSpecification.getId()));

        assertEquals(flattenEmptyCollections(productOffering),
                flattenEmptyCollections(productOfferingConsumer.retrieveProductOffering(productOffering.getId(), null)),
                "The product offering should be available at the consumers TMForum api.");
        assertEquals(flattenEmptyCollections(productSpecification),
                flattenEmptyCollections(productSpecificationConsumer.retrieveProductSpecification(productSpecification.getId(), null)),
                "The product specification should be available at the consumers TMForum api.");

    }

    /**
     * The NGSI-LD round trip normalises an absent collection into an empty one, so the replicated
     * copy comes back with {@code []} where the original held {@code null}. Both mean "nothing
     * here", so flatten empty collections away on both sides and keep comparing the whole entity.
     * <p>
     * Only <em>empty</em> collections are flattened: if replication actually dropped the contents
     * of a populated list, the comparison still fails.
     */
    private static <T> T flattenEmptyCollections(T vo) {
        for (Field field : vo.getClass().getDeclaredFields()) {
            if (!Collection.class.isAssignableFrom(field.getType())) {
                continue;
            }
            field.setAccessible(true);
            try {
                Collection<?> value = (Collection<?>) field.get(vo);
                if (value != null && value.isEmpty()) {
                    field.set(vo, null);
                }
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Could not normalise field " + field.getName(), e);
            }
        }
        return vo;
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
