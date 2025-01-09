Feature: A product offering and product specification created at a federated marketplace are also available on other
  marketplaces.

  Scenario: A provider creates a product offering and product specification, that can be accessed at a consumer.
    Given a provider and a consumer have deployed there access nodes.
    When a product specification is created at the providers marketplace.
    And a product offering related to the previous product specification is created at the providers marketplace.
    Then they should be available at the consumer marketplace, too.