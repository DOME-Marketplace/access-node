# Access Node

The access node has a set of environment variables and parameters that the user must configure. Below is a list of
mandatory configuration variables.

| Key                                                    | Comment                                                                                                                                                                                   | Possible Values                                                             |
|--------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------|
| access-node.desmos.app.operator.organizationIdentifier | DID of the operator, should follow the format `did:elsi`                                                                                                                                  | Example: did:elsi:VATFR-696240139                                           |
| access-node.desmos.app.externalDomain                  | Desmos external domain                                                                                                                                                                    | Example: https://desmos.example.org                                         |
| access-node.desmos.app.privateKey                      | Private key to sign transactions. You can add it in the field value or you can add it configuring it as sealed secret setting 'enabled' to true and adding the name and key of the secret | Example: 0x4c88c1c84e65e82b9ed6b49313c6a624d58b2b11e40b4b64e3b9d0a1d5e4dfaj |
| access-node.dlt-adapter.env.PRIVATE_KEY                | Private key to sign transactions. You can add it in the field value or you can add it configuring it as sealed secret setting 'enabled' to true and adding the name and key of the secret | Example: 0x4c88c1c84e65e82b9ed6b49313c6a624d58b2b11e40b4b64e3b9d0a1d5e4dfaj |
| access-node.dlt-adapter.env.RPC_ADDRESS                | Node address, typically a URL that points to a blockchain node                                                                                                                            | Example: https://red-t.alastria.io/v0/9461d9f4292b41230527d57ee90652a6      |
| access-node.dlt-adapter.env.ISS                        | Organization identifier hashed with SHA-256, derived from the `organizationIdentifier`                                                                                                    | Example: 0x43b27fef24cfe8a0b797ed8a36de2884f9963c0c2a0da640e3ec7ad6cd0c493d |

**Optional Configuration Changes**

The following variables can be modified by advanced users who are aware of the impact of these changes. It is
recommended to only change these settings if you are confident in your configuration and understand the dependencies
involved.

| Key                                              | Comment                                                                                                                          | Possible Values                                    |
|--------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------|
| access-node.desmos.app.broker.internalDomain     | Internal address of the context broker, can be modified if using a different DNS                                                 | Default: http://scorpio:9090                       |
| access-node.desmos.app.broker.externalDomain     | External address of the context broker, can be modified if using a different DNS                                                 | Default: http://scorpio:9090                       |
| access-node.desmos.app.db.name                   | Name of the database used by the blockchain-connector, can be changed to a custom database name                                  | Default: mktdb                                     |
| access-node.desmos.app.db.port                   | Port of the database host, can be changed if the database is running on a non-default port                                       | Default: 5432                                      |
| access-node.desmos.app.dltAdapter.internalDomain | Internal address of the DLT adapter, can be modified if using a different DNS                                                    | Default: http://dlt-adapter:8080                   |
| access-node.desmos.app.dltAdapter.externalDomain | External address of the DLT adapter, can be modified if using a different DNS                                                    | Default: http://dlt-adapter:8080                   |
| access-node.externalAccessNodesUrls              | If you enabled the setting 'enable' to true, you can add in customUrls, a comma-separated list of urls to do the synchronization | Example: "https://desmos1.org,https://desmos2.org" |
