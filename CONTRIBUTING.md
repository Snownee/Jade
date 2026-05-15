# Contributing

## Git Branches

There are 3 types of branches:
* `*-fabric`
* `*-neoforge`
* `*-docs`

If you are making code changes, you should only need to submit a pull request to main branch (latest fabric).
A maintainer will cherry pick your patch to other branches as necessary.

If you are making documentation changes, submit a pull request to the `-docs` branch.

Only bugfixes are typically added to older versions of Jade.
Non-bugfix pull requests to any but the latest branches are likely to be rejected.

## Development Environment

For best results, use the [IntelliJ](https://www.jetbrains.com/idea/) IDE.
Other IDEs will work but might have more sharp edges with the build tooling.

## Troubleshooting Build Errors

Some things to try:

* run these gradle tasks:
    * `clean` (Gradle > Jade > Tasks > build)
    * `dependencies` (Gradle > Jade > Tasks > help)
    * `javaToolchains` (Gradle > Jade > Tasks > help)
* if using IntelliJ, make sure that your project SDK is the right version of Java
    * `Project Structure > Project Settings > Project > SDK`
* if using IntelliJ, make sure that your Gradle JVM matches your project SDK
    * `Settings > Build, Execution, Deployment > Build Tools > Gradle > Gradle Projects > Jade > Gradle > Gradle JVM`
