# MAVEN

For developers that want to add World Blender to their mod's workspace:

<blockquote>repositories {

&nbsp; maven {

&nbsp; &nbsp; url "https://nexus.resourcefulbees.com/repository/maven-public/"

&nbsp; }

}</blockquote>

&nbsp;

Don't forget to change 3.1.4 with the actual latest version of this mod.

<blockquote>dependencies {


&nbsp; ...


&nbsp; implementation fg.deobf("com.telepathicgrunt:WorldBlender:1.16.5-3.1.4-forge")


}</blockquote>

&nbsp;

**Add these two properties to both of your run configs in the build.gradle file. These will allow WB's mixins to work. After you add the properties lines, refresh Gradle and run `genEclipseRuns` or `genIntellijRuns` or `genVSCodeRuns` based on what IDE you are using.**

<blockquote>minecraft {


&nbsp; ...


&nbsp; runs {


&nbsp; &nbsp; client {


&nbsp; &nbsp; &nbsp; ...


&nbsp; &nbsp; &nbsp; property 'mixin.env.remapRefMap', 'true'


&nbsp; &nbsp; &nbsp; property 'mixin.env.refMapRemappingFile', "${projectDir}/build/createSrgToMcp/output.srg"


&nbsp; &nbsp; }

&nbsp; &nbsp; server {


&nbsp; &nbsp; &nbsp; ...


&nbsp; &nbsp; &nbsp; property 'mixin.env.remapRefMap', 'true'


&nbsp; &nbsp; &nbsp; property 'mixin.env.refMapRemappingFile', "${projectDir}/build/createSrgToMcp/output.srg"


&nbsp; &nbsp; }


&nbsp; }


}</blockquote>

**____________________________________________________________________________**

&nbsp;

![The banner logo for World Blender with a picture of the crazy landscape behind the title.](https://i.imgur.com/HLLklJ6.png)

## **CURRENTLY FOR 1.16.5 MC**
**Needs to be on both client and server to work**

###### **(Play this mod with [get-it-together-drops](https://www.curseforge.com/minecraft/mc-mods/get-it-together-drops) for best performance!)**

###### **[[Click here for the 1.16.5 Fabric version of this mod!]](https://modrinth.com/mod/worldblender-fabric)**

**Past News:** Optimized portal rendering!  Reduced floating blocks and item entity spam!

**____________________________________________________________________________**

## **WHAT IS WORLD BLENDER?**

World Blender is a dimension filled with every single biome's features, structures, natural spawns, carvers (caves), and terrain surfaces shoved in! Even modded features and structures will be added to the dimension! However, some mods have hardcoded their features or structures to only spawn in their own dimensions or biomes but I will try and work out alternatives methods to get their stuff in this awesome dimension! But sometimes, those feature uses a config option to determine what dimension their stuff spawns in so if a mod's feature or structure is not spawning in World Blender's dimension, then take a look at the mod's config options first. If you still can't get it to spawn in the dimension, let me know so I can take a look and add it to my list of mods to try and be compatible with if it is possible. :)

&nbsp;

Inside the dimension, you will find massive webs of different kinds of surfaces all filled to the brim with trees, grass, and structures. But take notice of the Nether surface path that has End surfaces on each side. That Nether path is the main walkway through the dimension as no other surfaces can interrupt that pathway. So follow that red road and explore the insanity of the world! Also, if you got modded ores mod on, all the ores will be added as well as long as they are able to spawn in Stone! And lastly, the biomes will have imported every mob's spawn entry so there's no telling what will spawn! Though animals seem to not spawn very often due to all the trees and plants covering all the Grass Blocks so you may have to burn down some forests or blacklist many plant features in the config to increase animal spawning.

&nbsp;

And yes, the more worldgen mods you have on, the crazier World Blender's dimension becomes! It gets insane real fast! Though too many mods on and the dimension may become much slower to load. At that point, use my config and blacklist the most resource intensive worldgen stuff from other mods from being imported. World Blender requires customizing in the config for large modpacks in order to remain playable. Do note, the creation of the chunks at world origin in the dimension takes a bit of time to be generated as the Enderdragon stuff requires many chunks to be generated in a single tick.

&nbsp;

**Also, be sure to check out the images page for several screenshots of what the world looks like! And yes, you can use this mod in a modpack or download and modify the source code. It is all under LGPLv3 License**

&nbsp;

**____________________________________________________________________________**

## **HOW DO I ENTER THIS WORLD?**

To enter this highly, overpowered and broken dimension, you have to prove that you had traveled quite a lot and that you are in the endgame by placing 8 chests in a 2x2x2 area and then filling every slot with 1 of every block you can find! (Slots with the same item does not count. Items without block forms does not count. Having more than 1 kind of one block will not count as extra either.) Then when you are done, hold a Nether Star in your hand. Now crouch and right click any of the chests with the star to create the portal! The portal will consume the chests and the blocks inside as sacrifice! If this is too much to do, **you can easily reduce the number of unique blocks required or what activation item is needed to make the portal by going into the config file.** But once the portal is made, just walk into the portal after it has cooled down (not red anymore) and you'll enter the dimension with a new portal block at your feet to let you exit easily!

&nbsp;

If you create the portal in a bad spot, you can right click the portal blocks while crouching and holding no items to vaporize the portal. Also in this crazy dimension, you can always leave it by heading to world origin as there will be quartz altar with an unbreakable portal block to always allow you to escape. The portal will always place you back to the Overworld when leaving the dimension so keep that in mind! And also, the portal can teleport any entity or dropped item but only one at a time and it will have a cooldown phase.

&nbsp;

With commands active, you can enter the dimension faster by doing this command **/execute in world_blender:world_blender run tp ~ 70 ~**. And by default, features that contain lava or fire will not be imported so they do not create out of control fire spreading. You can turn off this config so fire features gets imported but if you do, I highly recommend you to do **/gamerule doFireTick false** so that fire does not go crazy rampant in this dimension and cause quite a bit of lag!

&nbsp;

**____________________________________________________________________________**

## **HOW CAN I CONFIGURE THIS MOD?**

This mod has 29 configs options total to allow you to customize this mod. The options are:

&nbsp;

<details>

**-turn on or off importing vanilla or mod's features, structures, carvers, surfaces, and natural mob spawns.**

&nbsp;

**-disable bamboo, fire, and lava features from spawning to help reduce lag.**

&nbsp;

**-surround floating liquid with solid blocks and placing solid blocks under blocks that can fall. This helps reduce lag from liquid flowing or blocks falling.**

&nbsp;

**-place Obsidian blocks to separate lava tagged fluids from water tagged fluids underground.**

&nbsp;

**-blacklist certain mod's or vanilla features, structures, carvers, surfaces, and natural mob spawns by using keywords, using the resource location (ID) of the thing itself, or using the resource location of the mod itself.**

&nbsp;

**-print out all resource locations of everything registered to help you with making a blacklist easily.**

&nbsp;

**-change how many kinds of blocks are needed to make the portal, specify required blocks that needs to be in the chests, what activation item(s) is needed, and whether the portal should drop the chests and its contents instead of consuming it.**

&nbsp;

**-how thick the surface bands are and whether vanilla ravine and cave carvers can carve through modded blocks, Netherrack, and End Stone underground.**

&nbsp;

**-spawn the Enderdragon at world origin in the dimension! (Set to off by default as I want people to explore this dimension safely without worrying about the dragon guarding the custom altar at world origin. That way you always have an easy escape from the dimension at anytime)**

</details>

&nbsp;

The config file has more info on each of the options. Got more ideas or suggestions? Leave a comment below and I'll see if it can be done. The config file is located in the config folder outside of the world save folders. **Any changes you do to the config requires a complete restart with Minecraft due to how it imports stuff from vanilla and other mod's biomes. And also, don't forget you can modify World Blender's dimension or biomes by Datapack too! Here's what you can override with datapacks:**
**[ https://github.com/TelepathicGrunt/WorldBlender/tree/master/src/main/resources/data/world_blender](https://github.com/TelepathicGrunt/WorldBlender/tree/master/src/main/resources/data/world_blender)**

 

**____________________________________________________________________________**

_For the list of changes in this mod, click the Source tab and check out Changelog.md file. This helps me keep track of what I had done so far._

&nbsp;

**If you find an issue, glitch, or have a suggestion about my mod, let me know! But if you don't have a GitHub account to report in the Issue tab above, just comment what the problem is below and I'll try and get back to you ASAP! :)**

&nbsp;

**Discord Link to #telepathicgrunt-mods channel for my mods! :**

**[https://discord.gg/SM7WBT6FGu](https://discord.gg/SM7WBT6FGu "https://discord.gg/SM7WBT6FGu")**

<a class="anchor-3Z-8Bb anchorUnderlineOnHover-2ESHQB" style="font-size: 24px;" tabindex="0" title="https://discord.gg/SM7WBT6FGu" role="button" href="https://discord.gg/SM7WBT6FGu" target="_blank" rel="noopener noreferrer"><img src="https://www.freepnglogos.com/uploads/discord-logo-png/concours-discord-cartes-voeux-fortnite-france-6.png" alt="discord-logo-png-free-transparent-png-logos-discord-png-logo-300_300 (PNG)  | BeeIMG" width="112" height="112" /></a>

