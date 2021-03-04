<p align="center">
<img src="https://leonardobishop.com/artwork/CommandToItem%20Ribbon%20Thinner%20Lines.png" width="200" height="200"><br>
<img src="http://isitmaintained.com/badge/resolution/LMBishop/CommandToItem.svg">
<img src="http://isitmaintained.com/badge/open/LMBishop/CommandToItem.svg">
<img src="https://mc-download-badges.herokuapp.com/services/spigotsongoda/downloads.php?spigot=19937&songoda=commandtoitem-commandtoitem"><br>
<h1 align="center">CommandToItem</h1>
</p>

A lightweight plugin which allows you to create consumable command items.

## Building
Release versions can be found primarily on [Spigot](https://www.spigotmc.org/resources/19937/).

You can build CommandToItem yourself using Gradle by cloning the repo and using the command `gradlew build`.

You can include CommandToItem in your project using [JitPack](https://jitpack.io/#LMBishop/CommandToItem) as a repository.

### Maven
```xml
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>
```
```xml
<dependency>
    <groupId>com.github.LMBishop</groupId>
    <artifactId>CommandToItem</artifactId>
    <version>master-SNAPSHOT</version>
    <scope>provided</scope>
</dependency>
```

### Gradle
```groovy
repositories {
    maven { url = uri('https://jitpack.io') }
}  
dependencies {
    compileOnly 'com.github.LMBishop:CommandToItem:master-SNAPSHOT'
}
```

## Contributing 
We welcome all contributions, we will check out all pull requests and determine if it should be added to CommandToItem. 

Assistance of all forms is appreciated 🙂

By contributing to CommandToItem you agree to license your code under the [GNU General Public License v3.0](https://github.com/LMBishop/CommandToItem/blob/master/LICENSE.txt).
