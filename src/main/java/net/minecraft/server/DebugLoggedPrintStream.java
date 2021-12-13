package net.minecraft.server;

import java.io.OutputStream;

public class DebugLoggedPrintStream extends LoggedPrintStream {
   public DebugLoggedPrintStream(String p_135934_, OutputStream p_135935_) {
      super(p_135934_, p_135935_);
   }

   protected void logLine(String p_135937_) {
      StackTraceElement[] astacktraceelement = Thread.currentThread().getStackTrace();
      StackTraceElement stacktraceelement = astacktraceelement[Math.min(3, astacktraceelement.length)];
      LOGGER.info("[{}]@.({}:{}): {}", this.name, stacktraceelement.getFileName(), stacktraceelement.getLineNumber(), p_135937_);
   }
}