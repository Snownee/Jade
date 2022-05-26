# Getting Started

## Setup

In your `build.gradle`:

``` groovy
repositories {
    maven {
        url "https://www.cursemaven.com"
        content {
            includeGroup "curse.maven"
        }
    }
}

dependencies {
    // Visit https://www.curseforge.com/minecraft/mc-mods/jade/files/all
    // to get the latest version's jade_id
    implementation fg.deobf("curse.maven:jade-324717:${jade_id}")
}
```

Visit [CurseMaven](https://www.cursemaven.com/) to find more information about how to set up your workspace.

## Registering

//TODO
