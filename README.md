![DIF Logo](https://raw.githubusercontent.com/decentralized-identity/universal-resolver/master/docs/logo-dif.png)

# Universal Resolver Driver: did:bid

This is a [Universal Resolver](https://github.com/decentralized-identity/universal-resolver/) driver for **did:bid** identifiers.

## Specifications

* [Decentralized Identifiers](https://w3c.github.io/did-core/)

## Example DIDs

```
did:bid:6cc796b8d6e2fbebc9b3cf9e
```
 
## Build and Run (Docker)

```
docker build -f ./docker/Dockerfile . -t universalresolver/driver-did-bid
docker run -p 8080:8080 universalresolver/driver-did-bid
curl -X GET http://localhost:8080/1.0/identifiers/did:bid:3acdafe161ef702033bdf895
```

## Build (native Java)
 
 Maven build:

	mvn clean install

## Driver Metadata

The driver returns the following metadata in addition to a DID document:

* `name`: The nickName of DID.
* `type`: The nickName of DID.
* `extra`: The extra of DID.
* `isEnable`: The isEnable of DID.
* `created`: The created of DID.
* `updated`: The updated of DID.
* `balance`: The balance of DID.
* `creation`: The transaction information for creating DID.
* `update`:  The transaction information for updating DID.
* `currentBlock`:  The current block information for BIF CHAIN.
* `authentication`:  The authentication information for DID.
