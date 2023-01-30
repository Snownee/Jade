# Hiding Target

``` java
@Override
public void registerClient(IWailaClientRegistration registration) {
	registration.hideTarget(EntityType.AREA_EFFECT_CLOUD);
	registration.hideTarget(Blocks.BARRIER);
}
```