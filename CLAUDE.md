# access-node

Umbrella Helm chart of the DOME Access Node (`charts/access-node`): Desmos, DLT adapters, Scorpio and dependencies.

## Updating the Desmos version

- Only change `desmos.image.tag` in `charts/access-node/values.yaml` (format `vX.Y.Z`, image
  `in2workspace/in2-desmos-api`). First check that the tag is already published on Docker Hub.
- Do not touch `version` in `Chart.yaml` or the table in `README.md`: the pre-release workflow sets
  `X.Y.Z-PRE-<PR number>` and regenerates the README with helm-docs in the same PR.
- Commit and PR title: `build(charts): update desmos image tag to vX.Y.Z`.

## Repo

- `main` is also updated by dependabot (`build(deps): ...`): run `git fetch` before creating a branch.
- `it/` holds the integration tests (Maven, k3s). `it/target` is generated output: do not use it as a reference for
  versions.
