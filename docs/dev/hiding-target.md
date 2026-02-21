# Hiding Target

```java
@Override
public void register(IWailaCommonRegistration registration) {
	registration.blockOperations().hide(Blocks.BARRIER);
	registration.entityTypeOperations().hide(EntityType.AREA_EFFECT_CLOUD);
}
```

# Use the Pick Result as the Target Name, Icon, and Mod Name

```java
@Override
public void register(IWailaCommonRegistration registration) {
	registration.blockOperations().pick(Blocks.PLAYER_HEAD);
}
```