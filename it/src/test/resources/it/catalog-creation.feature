Feature: A product offering and product specification created at a federated marketplace are also available on other
  marketplaces.

  Scenario: A provider creates a catalog, that can be accessed at a consumer.
    Given a provider and a consumer have deployed there access nodes.
    When a product offering and product specification are created at the providers marketplace.
    Then it should be available at the consumer marketplace, too.
