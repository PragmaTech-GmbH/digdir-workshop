package pragmatech.digital.workshops.lab1.experiment;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimingExtension
  implements BeforeTestExecutionCallback,
  AfterTestExecutionCallback {

  private static final Logger logger = LoggerFactory.getLogger(TimingExtension.class);

  @Override
  public void beforeTestExecution(ExtensionContext context) {
    getStore(context).put("start", System.currentTimeMillis());
  }

  @Override
  public void afterTestExecution(ExtensionContext context) {
    long start = getStore(context).remove("start", Long.class);
    long duration = System.currentTimeMillis() - start;
    logger.info("Test {} took {} ms", context.getDisplayName(), duration);
  }

  private ExtensionContext.Store getStore(ExtensionContext context) {
    return context.getStore(ExtensionContext.Namespace.create(getClass(), context.getRequiredTestMethod()));
  }
}
