#!/bin/bash

arrIN=(${1//,/ })

echo "POST RENDER +++++++++++++ " ${arrIN[0]} "-"  ${arrIN[1]}
mkdir -p ${arrIN[1]}

cat <&0 > ${arrIN[0]}/all.yaml
kustomize build ${arrIN[0]} -o ${arrIN[1]}