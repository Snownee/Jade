# Custom Theme

!!! note

    This document is for Jade 11.4 and above.

All the themes are defined in `jade_themes` folders in the resource pack. Each json file in the folder is a theme that
can be selected in the config screen. The file name is the theme name.

You can refer to [this example](https://modrinth.com/resourcepack/stardew-valley-theme-for-jade) to create your own
theme.

There are two types of themes: gradient borders like the built-in themes, or with a background image.

## Gradient Borders

```json5 title="jade_themes/dark.json"
{
  "backgroundColor": "#131313",
  "borderColor": [
    // top-left
    "#383838",
    // top-right
    "#383838",
    // bottom-right
    "#FF242424",
    // bottom-left
    "#FF242424"
  ]
}
```

Note that you can't comment in json files, the above is just for explanation.

All the color values can also accept format like `rgba(0, 0, 0, 0.5)` or `hsla(270deg, 60%, 70%, 0.5)`.

## Background Image

```json5 title="jade_themes/stardew_valley.json"
{
  "backgroundImage": [
    // nine-patch image path, must be 256x256
    "jade_stardew:textures/gui/stardew_valley.png",
    // top region
    4,
    // right region
    4,
    // bottom region
    4,
    // left region
    4,
    // width of the source image
    64,
    // height of the source image
    64,
    // left offset in the source image
    0,
    // top offset in the source image
    0
  ]
}
```

## Common Properties

```json5 title="jade_themes/stardew_valley.json"
{
  // background color is light
  "lightColorScheme": true,
  // change the text color scheme
  "titleColor": "#000000",
  "normalColor": "#444444",
  "infoColor": "#000000",
  "successColor": "#198754",
  "warningColor": "#FFC107",
  "dangerColor": "#DC3545",
  "failureColor": "#AB296A",
  // border color of the default box, used in status effects, block states, etc.
  "boxBorderColor": "#913E00",
  // render text shadow
  "textShadow": false,
  "padding": [
    // top
    6,
    // right
    4,
    // bottom
    0,
    // left
    5
  ],
  // default background opacity
  "opacity": 1,
  // turn on/off the square border after switching to this theme
  // do limited effect on theme with background image. in that case, it's recommended to set this to false
  "squareBorder": false,
  // automatically enable this theme when the resource pack is enabled
  "autoEnable": true,
  // move the breaking progress bar
  "bottomProgressOffset": [
    // top
    -1,
    // right
    -2,
    // bottom
    -1,
    // left
    2
  ],
  "bottomProgressNormalColor": "#FFFFFF",
  "bottomProgressFailureColor": "#FF4444"
}
```

## Finally, don't forget to name your theme!

```json5 title="lang/en_us.json"
{
  "jade.theme.jade_stardew.stardew_valley": "Stardew Valley"
}
```
