# Jade

[Documentation](https://jademc.readthedocs.io/en/latest/)

*This is 1.19 branch*

Jade is a UI improvement mod which shows information about what you are looking at. Jade is a fork of [HWYLA](https://github.com/TehNut/HWYLA) by TehNut

## Add to Dependencies

In your `build.gradle`:

```
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

## 1.17 Upgrading Guide

### Registration

Some methods in `IRegistrar` are renamed:
 - `registerStackProvider` -> `registerIconProvider`
 - `registerEntityStackProvider` -> `registerIconProvider`

Component provider registration methods no longer accept interface or TileEntity classes. You can register with the base class and filter it inside the provider.

`IRegistrar#registerTooltipRenderer` is removed because you don't need to register your renderer any longer. 

`IRegistrar#registerOverrideEntityProvider` is removed. Use `WailaRayTraceEvent` instead to have more flexible control to the target.

### Component Providers

`appendHead`, `appendBody` and `appendTail` are now merged into one method `appendTooltip`. You can get current `TooltipPosition` using `accessor.getTooltipPosition()`

`ItemStack getStack(IDataAccessor accessor, IPluginConfig config)` is changed to `IElement getIcon(BlockAccessor/EntityAccessor accessor, IPluginConfig config, IElement currentIcon)`, which means you can render anything as an icon, not only item.

### UI Elements other than Text

`RenderableTextComponent` and `ITooltipRenderer` are removed. Instead of manipulating `List<Component>`, you are now manipulating `IElement`s, and text component is a type of them.

For example, to display an item:

``` java
IElementHelper helper = tooltip.getElementHelper();
tooltip.add(helper.item(itemStack));
```

#### Debug Your Elements

Open `jade.json` in `config/jade` folder. Set `general.debug` to true. Then press `numpad 0` in game to refresh configuration. Then it will render outline for every element to show its exact position.

### `ITaggableList`

`ITaggableList` is removed. Now the minimal taggable element is `IElement`.

For example, to tag a text element:

``` java
IElementHelper helper = tooltip.getElementHelper();
tooltip.add(helper.text(text).tag(identifier));
```

or use the simplified version:

``` java
tooltip.add(text, identifier);
```


