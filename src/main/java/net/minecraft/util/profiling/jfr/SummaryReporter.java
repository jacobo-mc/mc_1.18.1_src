package net.minecraft.util.profiling.jfr;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import javax.annotation.Nullable;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.profiling.jfr.parse.JfrStatsParser;
import net.minecraft.util.profiling.jfr.parse.JfrStatsResult;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LifeCycle;
import org.apache.logging.log4j.spi.LoggerContext;
import org.apache.logging.log4j.util.Supplier;

public class SummaryReporter {
   private static final Logger LOGGER = LogManager.getLogger();
   private final Runnable onDeregistration;

   protected SummaryReporter(Runnable p_185398_) {
      this.onDeregistration = p_185398_;
   }

   public void recordingStopped(@Nullable Path p_185401_) {
      if (p_185401_ != null) {
         this.onDeregistration.run();
         infoWithFallback(() -> {
            return "Dumped flight recorder profiling to " + p_185401_;
         });

         JfrStatsResult jfrstatsresult;
         try {
            jfrstatsresult = JfrStatsParser.parse(p_185401_);
         } catch (Throwable throwable1) {
            warnWithFallback(() -> {
               return "Failed to parse JFR recording";
            }, throwable1);
            return;
         }

         try {
            infoWithFallback(jfrstatsresult::asJson);
            Path path = p_185401_.resolveSibling("jfr-report-" + StringUtils.substringBefore(p_185401_.getFileName().toString(), ".jfr") + ".json");
            Files.writeString(path, jfrstatsresult.asJson(), StandardOpenOption.CREATE);
            infoWithFallback(() -> {
               return "Dumped recording summary to " + path;
            });
         } catch (Throwable throwable) {
            warnWithFallback(() -> {
               return "Failed to output JFR report";
            }, throwable);
         }

      }
   }

   private static void infoWithFallback(Supplier<String> p_185403_) {
      if (log4jIsActive()) {
         LOGGER.info(p_185403_);
      } else {
         Bootstrap.realStdoutPrintln(p_185403_.get());
      }

   }

   private static void warnWithFallback(Supplier<String> p_185405_, Throwable p_185406_) {
      if (log4jIsActive()) {
         LOGGER.warn(p_185405_, p_185406_);
      } else {
         Bootstrap.realStdoutPrintln(p_185405_.get());
         p_185406_.printStackTrace(Bootstrap.STDOUT);
      }

   }

   private static boolean log4jIsActive() {
      LoggerContext loggercontext = LogManager.getContext();
      if (loggercontext instanceof LifeCycle) {
         LifeCycle lifecycle = (LifeCycle)loggercontext;
         return !lifecycle.isStopped();
      } else {
         return true;
      }
   }
}