package snownee.jade.api;

/**
 * Callback class interface used to provide Block/BlockEntity tooltip information to Waila.<br>
 * All methods in this interface shouldn't to be called by the implementing mod. An instance of the class is to be
 * registered to Waila via the {@link IWailaClientRegistration} instance provided in the original registration callback method
 * (cf. {@link IWailaClientRegistration} documentation for more information).
 *
 * @author ProfMobius
 */
public interface IBlockComponentProvider extends IComponentProvider<BlockAccessor> {
}
