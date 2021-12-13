package net.minecraft.client.player;

import net.minecraft.client.Options;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class KeyboardInput extends Input {
   private final Options options;
   private static final double MOVING_SLOW_FACTOR = 0.3D;

   public KeyboardInput(Options p_108580_) {
      this.options = p_108580_;
   }

   public void tick(boolean p_108582_) {
      this.up = this.options.keyUp.isDown();
      this.down = this.options.keyDown.isDown();
      this.left = this.options.keyLeft.isDown();
      this.right = this.options.keyRight.isDown();
      this.forwardImpulse = this.up == this.down ? 0.0F : (this.up ? 1.0F : -1.0F);
      this.leftImpulse = this.left == this.right ? 0.0F : (this.left ? 1.0F : -1.0F);
      this.jumping = this.options.keyJump.isDown();
      this.shiftKeyDown = this.options.keyShift.isDown();
      if (p_108582_) {
         this.leftImpulse = (float)((double)this.leftImpulse * 0.3D);
         this.forwardImpulse = (float)((double)this.forwardImpulse * 0.3D);
      }

   }
}