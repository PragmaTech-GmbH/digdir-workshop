package pragmatech.digital.workshops.lab5.experiment.customizer;

import java.util.List;

import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.test.context.ContextConfigurationAttributes;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.ContextCustomizerFactory;

public class SharedInfrastructureCustomizerFactory implements ContextCustomizerFactory {

  @Override
  public ContextCustomizer createContextCustomizer(Class<?> testClass, List<ContextConfigurationAttributes> configAttributes) {
    // Only activate if the @SharedInfrastructureTest annotation is present
    if (AnnotatedElementUtils.hasAnnotation(testClass, SharedInfrastructureTest.class)) {
      return new SharedInfrastructureContextCustomizer();
    }

    // Otherwise, return null (do nothing)
    return null;
  }
}
