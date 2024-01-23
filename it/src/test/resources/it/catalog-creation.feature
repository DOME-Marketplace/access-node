Feature: A catalog created at a federated marketplace is also available on other marketplaces.

  Scenario: A provider creates a catalog, that can be accessed at a consumer.
    Given a provider and a consumer have deployed there access nodes.
    When a catalog is created at the providers marketplace.
    Then it should be available at the consumer marketplace, too.
