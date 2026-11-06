package gym.integrationbdd;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

@CucumberContextConfiguration
@ContextConfiguration(classes = IntegrationCucumberSpringConfiguration.CucumberContext.class)
public class IntegrationCucumberSpringConfiguration {

    @Configuration
    static class CucumberContext {
    }
}
