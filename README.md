# Smallhands

A simple mod that makes item hands smaller.

This project is currently being reworked.

Official release on Modrinth:  
https://modrinth.com/mod/small-items-mod

Everything in this GitHub repository is a work in progress.

Info:  
This branch is for Minecraft 1.21–1.21.11. One jar supports all of them and needs Java 21.  
Newer Minecraft versions have their own branches.

## Building

Requires Java 25 to run the build. The mod itself targets Java 21.

```sh
./gradlew build
```

The mod jar is written to `build/libs/smallhands-<version>.jar`. Run `./gradlew runClient` to start a test client with Mod Menu installed.

Each Minecraft version has its own branch (e.g. `26.3`, `26.2`, `26.1.2`, `1.21`). Versions are set in `gradle.properties`.

## Testing

`./gradlew runClientGameTest` starts a client, checks that the config screen closes properly, and saves first-person screenshots of the hands and held items to `build/run/clientGameTest/screenshots`, along with a `review.md` that shows vanilla and modded side by side with the expected result for each scene. It is not part of `build`; run it locally when changing rendering code and open `review.md` in a Markdown preview. Fabric's client game test API needs Minecraft 1.21.4 or later.
