# Security policy

## Official Minecraft artifacts

Worldline never redistributes official Beta 1.7.3 client or dedicated-server JARs, original assets, or decompiled game sources. Hash-frozen oracles are acquired locally or on the self-hosted runtime lane. Public GitHub-hosted workflows must not run `tools/artifacts/Acquire.java client` or copy `minecraft-b1.7.3-client.jar`.

## Reporting a vulnerability

Email the maintainer through the GitHub account that owns this repository. Do not open a public issue for undisclosed oracle, credential, or supply-chain defects.

## Supply chain

- Dependabot watches GitHub Actions and the Gradle plugin.
- CodeQL scans Java on pull requests and `main`.
- `tokei` is installed from a versioned source tarball whose SHA-256 is pinned in `quality/tokei-pins.properties`.
- JDK pins are a closed set in `quality/jdk-pins.properties`.

## Branch protection

Enabling GitHub `main` branch protection is an operator action (repository administration). This tree cannot turn it on. Verify live protection with:

```text
gh api repos/lucasrgt/Worldline/branches/main/protection
```

A local markdown note is not proof that protection is enabled.
