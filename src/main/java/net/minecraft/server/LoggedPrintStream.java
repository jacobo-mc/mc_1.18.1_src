package net.minecraft.server;

import java.io.OutputStream;
import java.io.PrintStream;
import javax.annotation.Nullable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LoggedPrintStream extends PrintStream {
   protected static final Logger LOGGER = LogManager.getLogger();
   protected final String name;

   public LoggedPrintStream(String p_135951_, OutputStream p_135952_) {
      super(p_135952_);
      this.name = p_135951_;
   }

   public void println(@Nullable String p_135957_) {
      this.logLine(p_135957_);
   }

   public void println(Object p_135955_) {
      this.logLine(String.valueOf(p_135955_));
   }

   protected void logLine(@Nullable String p_135953_) {
      LOGGER.info("[{}]: {}", this.name, p_135953_);
   }
}