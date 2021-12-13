package net.minecraft.world.level.material;

import java.util.Optional;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.IdMapper;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class Fluid {
   public static final IdMapper<FluidState> FLUID_STATE_REGISTRY = new IdMapper<>();
   protected final StateDefinition<Fluid, FluidState> stateDefinition;
   private FluidState defaultFluidState;

   protected Fluid() {
      StateDefinition.Builder<Fluid, FluidState> builder = new StateDefinition.Builder<>(this);
      this.createFluidStateDefinition(builder);
      this.stateDefinition = builder.create(Fluid::defaultFluidState, FluidState::new);
      this.registerDefaultState(this.stateDefinition.any());
   }

   protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> p_76121_) {
   }

   public StateDefinition<Fluid, FluidState> getStateDefinition() {
      return this.stateDefinition;
   }

   protected final void registerDefaultState(FluidState p_76143_) {
      this.defaultFluidState = p_76143_;
   }

   public final FluidState defaultFluidState() {
      return this.defaultFluidState;
   }

   public abstract Item getBucket();

   protected void animateTick(Level p_76116_, BlockPos p_76117_, FluidState p_76118_, Random p_76119_) {
   }

   protected void tick(Level p_76113_, BlockPos p_76114_, FluidState p_76115_) {
   }

   protected void randomTick(Level p_76132_, BlockPos p_76133_, FluidState p_76134_, Random p_76135_) {
   }

   @Nullable
   protected ParticleOptions getDripParticle() {
      return null;
   }

   protected abstract boolean canBeReplacedWith(FluidState p_76127_, BlockGetter p_76128_, BlockPos p_76129_, Fluid p_76130_, Direction p_76131_);

   protected abstract Vec3 getFlow(BlockGetter p_76110_, BlockPos p_76111_, FluidState p_76112_);

   public abstract int getTickDelay(LevelReader p_76120_);

   protected boolean isRandomlyTicking() {
      return false;
   }

   protected boolean isEmpty() {
      return false;
   }

   protected abstract float getExplosionResistance();

   public abstract float getHeight(FluidState p_76124_, BlockGetter p_76125_, BlockPos p_76126_);

   public abstract float getOwnHeight(FluidState p_76123_);

   protected abstract BlockState createLegacyBlock(FluidState p_76136_);

   public abstract boolean isSource(FluidState p_76140_);

   public abstract int getAmount(FluidState p_76141_);

   public boolean isSame(Fluid p_76122_) {
      return p_76122_ == this;
   }

   public boolean is(Tag<Fluid> p_76109_) {
      return p_76109_.contains(this);
   }

   public abstract VoxelShape getShape(FluidState p_76137_, BlockGetter p_76138_, BlockPos p_76139_);

   public Optional<SoundEvent> getPickupSound() {
      return Optional.empty();
   }
}