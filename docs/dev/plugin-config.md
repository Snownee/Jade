# Plugin Configuration

## Registering and Getting Value

```java
@Override
public void registerClient(IWailaClientRegistration registration) {
    var optionId = Identifier.parse("examplemod:boolean_option");
	registration.addConfig(optionId, true);
	// IWailaConfig.get().plugin().get(optionId);

	optionId = Identifier.parse("examplemod:string_option");
	registration.addConfig(optionId, "minecraft:apple", Identifier.tryParse($) != null);
	registration.addConfigListener(optionId, $ -> System.out.println("Changed to: " + IWailaConfig.get().plugin().getString($)));
}
```

## Secondary Options

```java
var parentId = Identifier.parse("examplemod:parent");
registration.addConfig(Identifier.parse("examplemod:parent.suboption"), true);
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
