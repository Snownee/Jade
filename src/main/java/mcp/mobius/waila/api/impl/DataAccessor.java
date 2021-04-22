package mcp.mobius.waila.api.impl;

import mcp.mobius.waila.api.ICommonAccessor;
import mcp.mobius.waila.api.IDataAccessor;
import mcp.mobius.waila.api.IEntityAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class DataAccessor implements ICommonAccessor, IDataAccessor, IEntityAccessor {

	public static final DataAccessor INSTANCE = new DataAccessor();

	public World world;
	public PlayerEntity player;
	public RayTraceResult hitResult;
	public Vector3d renderingvec = null;
	public Block block = Blocks.AIR;
	public BlockState state = Blocks.AIR.getDefaultState();
	public BlockPos pos = BlockPos.ZERO;
	public ResourceLocation blockRegistryName = Blocks.AIR.getRegistryName();
	public TileEntity tileEntity;
	public Entity entity;
	public CompoundNBT serverData = null;
	public long timeLastUpdate = System.currentTimeMillis();
	public double partialFrame;
	public ItemStack stack = ItemStack.EMPTY;
	public boolean serverConnected;

	public void set(World world, PlayerEntity player, RayTraceResult hit) {
		this.set(world, player, hit, null, 0.0);
	}

	public void set(World world, PlayerEntity player, RayTraceResult hit, Entity viewEntity, double partialTicks) {
		this.world = world;
		this.player = player;
		this.hitResult = hit;

		if (this.hitResult.getType() == RayTraceResult.Type.BLOCK) {
			this.pos = ((BlockRayTraceResult) this.hitResult).getPos();
			this.state = this.world.getBlockState(this.pos);
			this.block = this.state.getBlock();
			TileEntity tileEntity = this.world.getTileEntity(this.pos);
			if (this.tileEntity != tileEntity) {
				this.tileEntity = tileEntity;
				this.serverData = null;
				this.timeLastUpdate = System.currentTimeMillis() - MetaDataProvider.rateLimiter;
			}
			this.entity = null;
			this.blockRegistryName = block.getRegistryName();
			this.stack = block.getPickBlock(state, hitResult, world, pos, player);
		} else if (this.hitResult.getType() == RayTraceResult.Type.ENTITY) {
			Entity entity = ((EntityRayTraceResult) this.hitResult).getEntity();
			if (this.entity != entity) {
				this.entity = entity;
				this.serverData = null;
				this.timeLastUpdate = System.currentTimeMillis() - MetaDataProvider.rateLimiter;
			}
			this.pos = new BlockPos(entity.getPositionVec());
			this.state = Blocks.AIR.getDefaultState();
			this.block = Blocks.AIR;
			this.tileEntity = null;
			this.stack = ItemStack.EMPTY;
		}

		if (viewEntity != null) {
			double px = viewEntity.prevPosX + (viewEntity.getPositionVec().x - viewEntity.prevPosX) * partialTicks;
			double py = viewEntity.prevPosY + (viewEntity.getPositionVec().y - viewEntity.prevPosY) * partialTicks;
			double pz = viewEntity.prevPosZ + (viewEntity.getPositionVec().z - viewEntity.prevPosZ) * partialTicks;
			this.renderingvec = new Vector3d(this.pos.getX() - px, this.pos.getY() - py, this.pos.getZ() - pz);
			this.partialFrame = partialTicks;
		}
	}

	@Override
	public World getWorld() {
		return this.world;
	}

	@Override
	public PlayerEntity getPlayer() {
		return this.player;
	}

	@Override
	public Block getBlock() {
		return this.block;
	}

	@Override
	public BlockState getBlockState() {
		return this.state;
	}

	@Override
	public TileEntity getTileEntity() {
		return this.tileEntity;
	}

	@Override
	public Entity getEntity() {
		return this.entity;
	}

	@Override
	public BlockPos getPosition() {
		return this.pos;
	}

	@Override
	public RayTraceResult getHitResult() {
		return this.hitResult;
	}

	@Override
	public Vector3d getRenderingPosition() {
		return this.renderingvec;
	}

	@Override
	public CompoundNBT getServerData() {
		if ((this.tileEntity != null) && this.isTagCorrectTileEntity(this.serverData))
			return serverData;

		if ((this.entity != null) && this.isTagCorrectEntity(this.serverData))
			return serverData;

		if (this.tileEntity != null)
			return tileEntity.write(new CompoundNBT());

		if (this.entity != null)
			return entity.writeWithoutTypeId(new CompoundNBT());

		return new CompoundNBT();
	}

	public void setServerData(CompoundNBT tag) {
		this.serverData = tag;
	}

	private boolean isTagCorrectTileEntity(CompoundNBT tag) {
		if (tag == null) {
			this.timeLastUpdate = System.currentTimeMillis() - MetaDataProvider.rateLimiter;
			return false;
		}

		int x = tag.getInt("x");
		int y = tag.getInt("y");
		int z = tag.getInt("z");

		BlockPos hitPos = ((BlockRayTraceResult) this.hitResult).getPos();
		if (x == hitPos.getX() && y == hitPos.getY() && z == hitPos.getZ())
			return true;
		else {
			this.timeLastUpdate = System.currentTimeMillis() - MetaDataProvider.rateLimiter;
			return false;
		}
	}

	private boolean isTagCorrectEntity(CompoundNBT tag) {
		if (tag == null || !tag.contains("WailaEntityID")) {
			this.timeLastUpdate = System.currentTimeMillis() - MetaDataProvider.rateLimiter;
			return false;
		}

		int id = tag.getInt("WailaEntityID");

		if (id == this.entity.getEntityId())
			return true;
		else {
			this.timeLastUpdate = System.currentTimeMillis() - MetaDataProvider.rateLimiter;
			return false;
		}
	}

	@Override
	public double getPartialFrame() {
		return this.partialFrame;
	}

	@Override
	public Direction getSide() {
		return hitResult == null ? null : hitResult.getType() == RayTraceResult.Type.ENTITY ? null : ((BlockRayTraceResult) this.hitResult).getFace();
	}

	@Override
	public ItemStack getStack() {
		return this.stack;
	}

	public boolean isTimeElapsed(long time) {
		return System.currentTimeMillis() - this.timeLastUpdate >= time;
	}

	public void resetTimer() {
		this.timeLastUpdate = System.currentTimeMillis();
	}

	@Override
	public ResourceLocation getBlockId() {
		return blockRegistryName;
	}
}
