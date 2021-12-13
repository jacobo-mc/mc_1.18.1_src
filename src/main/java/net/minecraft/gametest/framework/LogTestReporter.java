package net.minecraft.gametest.framework;

import net.minecraft.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LogTestReporter implements TestReporter {
   private static final Logger LOGGER = LogManager.getLogger();

   public void onTestFailed(GameTestInfo p_127797_) {
      if (p_127797_.isRequired()) {
         LOGGER.error("{} failed! {}", p_127797_.getTestName(), Util.describeError(p_127797_.getError()));
      } else {
         LOGGER.warn("(optional) {} failed. {}", p_127797_.getTestName(), Util.describeError(p_127797_.getError()));
      }

   }

   public void onTestSuccess(GameTestInfo p_177676_) {
   }
}