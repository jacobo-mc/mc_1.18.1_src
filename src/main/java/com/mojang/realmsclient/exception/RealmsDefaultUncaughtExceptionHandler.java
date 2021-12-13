package com.mojang.realmsclient.exception;

import java.lang.Thread.UncaughtExceptionHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsDefaultUncaughtExceptionHandler implements UncaughtExceptionHandler {
   private final Logger logger;

   public RealmsDefaultUncaughtExceptionHandler(Logger p_87766_) {
      this.logger = p_87766_;
   }

   public void uncaughtException(Thread p_87768_, Throwable p_87769_) {
      this.logger.error("Caught previously unhandled exception :");
      this.logger.error(p_87769_);
   }
}