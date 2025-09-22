# Plugin Configuration

## Registering and Getting Value

```java
@Override
public void registerClient(IWailaClientRegistration registration) {
    var optionId = ResourceLocation.fromNamespaceAndPath("examplemod", "boolean_option");
	registration.addConfig(optionId, true);
	// IWailaConfig.get().getPlugin().get(optionId);

	optionId = ResourceLocation.fromNamespaceAndPath("examplemod", "string_option");
	registration.addConfig(optionId, "minecraft:apple", ResourceLocation::isValidResourceLocation);
	registration.addConfigListener(optionId, id -> System.out.println("Changed to: " + IWailaConfig.get().getPlugin().getString(id)));
}
```

## Secondary Options

```java
var parentId = ResourceLocation.fromNamespaceAndPath("examplemod", "parent");
registration.addConfig(ResourceLocation.fromNamespaceAndPath("examplemod", "parent.suboption"), true);
```

## Client Feature

Client feature is option that can work without the need of server data.

To mark a config option as client feature:

```java
registration.markAsClientFeature(optionId);
```

To check if a config option is a client feature:

```java
registration.isClientFeature(optionId); // true
```
