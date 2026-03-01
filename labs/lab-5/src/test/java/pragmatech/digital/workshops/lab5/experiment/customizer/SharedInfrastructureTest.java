package pragmatech.digital.workshops.lab5.experiment.customizer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface SharedInfrastructureTest {
  // You can add parameters here, like 'boolean useWireMock() default true'
}
