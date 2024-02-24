## Movecraft : Community Edition

======

![Java CI](https://github.com/ccorp2002/Movecraft-CommunityEdition/workflows/Java%20CI/badge.svg?branch=community-main)


This is **another** maintained fork of Movecraft, which aims to maintain compatibility with APDev/Mainstream-Movecraft as well as Performance Changes & Fixes, API Upgrades, Ticking/Updating Redstone Components.

**Movecraft : Community Edition requires at least**
## *Java 17 and MC-1.19.4*

## Download

Mainstream Public builds, as well as builds as old as v5.0 (for 1.9), are located on the [Spigot forums](https://www.spigotmc.org/resources/movecraft.31321/).

Preliminary builds (including 1.14+ support), can be found on the [releases tab](https://github.com/ccorp2002/Movecraft-CommunityEdition/releases).

Development builds can be found under the [actions tab](https://github.com/ccorp2002/Movecraft-CommunityEdition/actions?query=workflow%3A%22Java+CI%22).  Use at your own risk!

Legacy builds as old as v0.7.1 (for 1.0.0) can be found on the original [Bukkit page](https://dev.bukkit.org/projects/movecraft).

## Support
Please check the [Wiki](https://github.com/APDevTeam/Movecraft/wiki) and [FAQ](https://github.com/APDevTeam/Movecraft/wiki/Frequently-Asked-Questions) pages before asking for help!

[Github Issues](https://github.com/ccorp2002/Movecraft-CommunityEdition/issues)

[Discord](http://bit.ly/JoinAP-Dev)

## Development Environment
Movecraft uses multiple versions of the Spigot server software for legacy support.  As such, you need to run [BuildTools](https://www.spigotmc.org/wiki/buildtools/) for several versions before building the plugin.  It doesn't matter where you do this, but inside the Movecraft directory is probably a bad place.  We recommend building Spigot Java 17 to build 1.19.4 & 1.20.4. Alternatively, you can use GitHub codespaces and run the `setup.sh` script to build all the needed versions automatically.

```
java -jar BuildTools.jar --rev 1.19.4 --remapped
java -jar BuildTools.jar --rev 1.20.4 --remapped
```

Once you have compiled CraftBukkit, it should continue to exist in your local maven repository, and thus you should need to compile each version at most one time. Once complete, run the following to build Movecraft through `maven`.
```
mvn -T 1C clean install
```
Compiled jars can be found in the `/target` directory.

#### Movecraft is released under the GNU General Public License V3. 
