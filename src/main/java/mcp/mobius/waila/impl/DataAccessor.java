package mcp.mobius.waila.impl;

import mcp.mobius.waila.api.IDataAccessor;
import mcp.mobius.waila.api.IEntityAccessor;
import mcp.mobius.waila.api.TooltipPosition;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class DataAccessor implements IDataAccessor, IEntityAccessor {

    public static final DataAccessor INSTANCE = new DataAccessor();

    public World world;
    public PlayerEntity player;
    public RayTraceResult hitResult;
    public BlockState state = Blocks.AIR.getDefaultState();
    public BlockPos pos = BlockPos.ZERO;
    public TileEntity tileEntity;
    public Entity entity;
    public CompoundNBT serverData = null;
    public long timeLastUpdate = System.currentTimeMillis();
    public boolean serverConnected;
    public TooltipPosition tooltipPosition;

    public void set(World world, PlayerEntity player, RayTraceResult hit) {
        this.world = world;
        this.player = player;
        this.hitResult = hit;

        if (this.hitResult.getType() == RayTraceResult.Type.BLOCK) {
            this.pos = ((BlockRayTraceResult) this.hitResult).getPos();
            this.state = this.world.getBlockState(this.pos);
            TileEntity tileEntity = this.world.getTileEntity(this.pos);
            if (this.tileEntity != tileEntity) {
                this.tileEntity = tileEntity;
                this.serverData = null;
                this.timeLastUpdate = System.currentTimeMillis() - MetaDataProvider.rateLimiter;
            }
            this.entity = null;
        } else if (this.hitResult.getType() == RayTraceResult.Type.ENTITY) {
            Entity entity = ((EntityRayTraceResult) this.hitResult).getEntity();
            if (this.entity != entity) {
                this.entity = entity;
                this.serverData = null;
                this.timeLastUpdate = System.currentTimeMillis() - MetaDataProvider.rateLimiter;
            }
            this.pos = new BlockPos(entity.getPositionVec());
            this.state = Blocks.AIR.getDefaultState();
            this.tileEntity = null;
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
        return this.state.getBlock();
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
    public Direction getSide() {
        return hitResult == null ? null : hitResult.getType() == RayTraceResult.Type.ENTITY ? null : ((BlockRayTraceResult) this.hitResult).getFace();
    }

    public boolean isTimeElapsed(long time) {
        return System.currentTimeMillis() - this.timeLastUpdate >= time;
    }

    public void resetTimer() {
        this.timeLastUpdate = System.currentTimeMillis();
    }

    @Override
    public TooltipPosition getTooltipPosition() {
        return tooltipPosition;
    }

    @Override
    public boolean isServerConnected() {
        return serverConnected;
    }

}
