# Displaying a Block Created by Datapack

Here is a page for datapack developers about how to display the correct information of a custom block.

## Block Name and Icon

Here is a few requirements:

 - The fake block should be displayed by an item frame or a glow item frame
 - The item frame should be invisible

Then Jade will use the item inside the item frame, or the stored item in the `storedItem` tag of the said item.

!!! example

    ```
    {
      id: "minecraft:barrel",
      tag: {
        CustomModelData: 1001,
        storedItem: {
          id: "minecraft:barrel",
          tag: {
            CustomModelData: 1002
          }
        }
      }
    }
    ```

In the above case, it will show a barrel with model **1002**.

## Contents and Background Items

If you want Jade to ignore an content item, you need to tag the item with an entry that:

  - Key ends with "clear"
  - Value is `1b`

!!! example

    ```
    {
      id: "minecraft:firework_star",
      tag: {
        exClear: 1b
      }
    }
    ```

## Creator Pack Name

You can show your pack's name at the bottom of the HUD, instead of "Minecraft". To do so, you need to tag the icon item with your block id:

!!! example

    ```
    {
      id: "minecraft:barrel",
      tag: {
        CustomModelData: 1002,
        id: "cpp:all_in_one_machine"
      }
    }
    ```

Then add a translation for your pack name:

``` json title="en_us.json"
{
  "jade.modName.cpp": "Crafting++"
}
```
