# Displaying a Block Created by Datapack

Here is a page for datapack developers about how to display the correct information of a server-side block. Jade supports server-side blocks that are displayed by an item display entity. Jade will use the displayed item as the icon and name of the block.

## Assigning the Block ID

You can assign an ID to your block so Jade can:

- Show the correct mod name
- Look up recipes in-world with Polydex

To do so, you need to add a `$jade:stack` tag to the item's custom data:

```json
{
	"$jade:stack": {
		"id": "mymod:my_block"
	}
}
```

Then add a translation for your mod's name:

```json title="en_us.json"
{
  "jade.modName.mymod": "My Mod",
  "itemGroup.mymod": "My Mod (alternative)"
}
```