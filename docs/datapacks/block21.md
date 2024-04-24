# Displaying a Block Created by Datapack

Here is a page for datapack developers about how to display the correct information of a custom block.

## Block Name and Icon

Here is a few requirements:

- The fake block must be displayed by an item frame or a glow item frame
- The item frame must be invisible

Then Jade will use the item inside the item frame as the name and icon.

## Contents and Background Items

If you want Jade to ignore a content item, you need to tag the item with an entry that:

- Key ends with "clear"
- Value is `1b`

!!! example

    ```
    {
      exClear: 1b
    }
    ```

## Creator Pack Name

You can show your pack's name at the bottom of the HUD, instead of "Minecraft" through the translations.

For example, the model data of your custom item is `1002`, and the namespace is `cpp`, you can add the following translation to the `en_us.json` file:

```json title="en_us.json"
{
  "jade.customModelData.1002.namespace": "cpp",
  "jade.modName.cpp": "Crafting++"
}
```
