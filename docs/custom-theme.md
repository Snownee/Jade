# Custom Theme

All the themes are defined in `jade_themes` folder in the resource pack. Each json file in the folder is a theme that can be selected in the config screen. The file name is the theme id.

You can refer to [this example](https://modrinth.com/resourcepack/stardew-valley-theme-for-jade) to create your own theme.

## Theme Example

```json
{
	"version": 200,
	"tooltipStyle": {
		"sprite": "jade_stardew:stardew_valley",
		"boxProgressColors": {
			"normal": "#FFFFFF",
			"failure": "#FF4444"
		},
		"padding": [
			4,
			4,
			4,
			4
		]
	},
	"nestedBoxStyle": {
		"sprite": "jade_stardew:nested_box"
	},
	"viewGroupStyle": {
		"sprite": "jade_stardew:view_group"
	},
	"text": {
		"colors": {
			"title": "#000000",
			"normal": "#444444",
			"info": "#000000",
			"success": "#198754",
			"warning": "#9C5C03",
			"danger": "#DC3545",
			"failure": "#AB296A"
		},
		"shadow": true,
		"modNameStyle": {
			"italic": false
		}
	},
	"changeOpacity": 1,
	"autoEnable": true,
	"lightColorScheme": true,
	"iconSlotSprite": "jade_stardew:stardew_valley",
	"iconSlotInflation": -2
}
```

## Child Theme

Child themes should be placed in a subfolder whose name matches the parent theme's ID.

```
/jade_themes/
│
├── dark.json
│
└── dark/
    └── slim.json
```

## Theme Format

| Name              | Description                                                                             | Type / Literal                    |
| ----------------- | --------------------------------------------------------------------------------------- | --------------------------------- |
| version           | fixed version number                                                                    | 200                               |
| styleName         | \[optional] language key for a child theme's style name                                 | string                            |
| tooltipStyle      | the main box's style                                                                    | BoxStyle                          |
| nestedBoxStyle    | \[optional] mainly used for the background of progress bars and status effect boxes     | BoxStyle                          |
| viewGroupStyle    | \[optional] mainly used for the background when there are multiple item or fluid groups | BoxStyle                          |
| text              | \[optional] text settings                                                               | TextSetting                       |
| changeOpacity     | \[optional] change opacity when this theme is enabled                                   | number(0-1)                       |
| lightColorScheme  | \[optional] let plugins know this theme is a light theme so they can use proper colors  | boolean(default: false)           |
| iconSlotSprite    | \[optional] icon background                                                             | string(Identifier)                |
| iconSlotInflation | \[optional] icon background inflation                                                   | int                               |
| sneakyDetails     | \[optional] sneaky details (the down arrow when lite display mode is on) settings       | SneakyDetails                     |
| spriteMapping     | \[optional] remap used sprites                                                          | dictionary<Identifier,Identifier> |

## `BoxStyle` Format

| Name              | Description                                                                                           | Type / Literal          |
| ----------------- | ----------------------------------------------------------------------------------------------------- | ----------------------- |
| boxProgressOffset | \[optional]                                                                                           | number\[4]              |
| padding           | \[optional]                                                                                           | int\[4]                 |
| borderWidth       | \[optional]                                                                                           | int(default: 1)         |
| sprite            | \[optional]                                                                                           | string(Identifier)      |
| withIconSprite    | \[optional] use a different sprite when there is an icon for this box                                 | string(Identifier)      |
| tooltip           | \[optional] use [tooltip sprite format](https://minecraft.wiki/w/Data_component_format#tooltip_style) | boolean(default: false) |

## `TextSetting` Format

| Name            | Description                                | Type / Literal            |
| --------------- | ------------------------------------------ | ------------------------- |
| colors          | \[optional]                                | ColorPalette              |
| shadow          | \[optional]                                | boolean(default: true)    |
| modNameStyle    | \[optional] only available in tooltipStyle | Style                     |
| itemAmountColor | \[optional]                                | Color(default: #FFFFFFFF) |

## `ColorPalette` Format

| Name    | Description | Type / Literal            |
| ------- | ----------- | ------------------------- |
| normal  | \[optional] | Color(default: #FFA0A0A0) |
| info    | \[optional] | Color(default: #FFFFFFFF) |
| title   | \[optional] | Color(default: #FFFFFFFF) |
| success | \[optional] | Color(default: #FF55FF55) |
| warning | \[optional] | Color(default: #FFFFC107) |
| danger  | \[optional] | Color(default: #FFFF5555) |
| failure | \[optional] | Color(default: #FFAA0000) |

## `Color` Format

```
"#AARRGGBB"
```

## `Style` Format

[Minecraft Wiki](https://minecraft.wiki/w/Text_component_format#Java_Edition), see the *Formatting* part.

## `SneakyDetails` Format

| Name              | Description                          | Type / Literal      |
| ----------------- | ------------------------------------ | ------------------- |
| type              |                                      | "simple"            |
| sprite            |                                      | string(Identifier)  |
| width             |                                      | int                 |
| height            |                                      | int                 |
| offsetX           | \[optional]                          | number              |
| offsetY           | \[optional]                          | number              |
| animation         | \[optional] can be empty or "breath" | string              |
| animationDistance | \[optional]                          | number(default: 1)  |
| animationPeriod   | \[optional]                          | number(default: 12) |

## Lastly, don't forget to name your theme!

```json title="lang/en_us.json"
{
  "jade.theme.jade_stardew.stardew_valley": "Stardew Valley"
}
```