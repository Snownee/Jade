# Plugin Configuration

## Registering and Getting Value

``` java
@Override
public void registerClient(IWailaClientRegistration registration) {
    var optionId = new ResourceLocation("examplemod:boolean_option");
	registration.addConfig(optionId, true);
	IWailaConfig.get().getPlugin().get(optionId);

	optionId = new ResourceLocation("examplemod:string_option");
	registration.addConfig(optionId, "minecraft:apple", ResourceLocation::isValidResourceLocation);
	registration.addConfigListener(optionId, id -> System.out.println("Changed to: " + IWailaConfig.get().getPlugin().getString(id)));
}
```

## Secondary Options

``` java
var parentId = new ResourceLocation("examplemod:parent");
registration.addConfig(new ResourceLocation("examplemod:parent.suboption"), true);
```

## Client Feature

Client feature is option that can work without the need of server data.

To mark a config option as client feature:

``` java
registration.markAsClientFeature(optionId);
```

To check if a config option is a client feature:

```
registration.isClientFeature(optionId); // true
```
