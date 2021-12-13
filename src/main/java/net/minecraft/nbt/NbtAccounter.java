package net.minecraft.nbt;

public class NbtAccounter {
   public static final NbtAccounter UNLIMITED = new NbtAccounter(0L) {
      public void accountBits(long p_128927_) {
      }
   };
   private final long quota;
   private long usage;

   public NbtAccounter(long p_128922_) {
      this.quota = p_128922_;
   }

   public void accountBits(long p_128923_) {
      this.usage += p_128923_ / 8L;
      if (this.usage > this.quota) {
         throw new RuntimeException("Tried to read NBT tag that was too big; tried to allocate: " + this.usage + "bytes where max allowed: " + this.quota);
      }
   }
}