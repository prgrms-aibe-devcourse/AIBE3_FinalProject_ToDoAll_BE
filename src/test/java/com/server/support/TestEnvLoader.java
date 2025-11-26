package com.server.support;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

@PropertySource("classpath:application-test.yml")
@TestExecutionListeners(value = DependencyInjectionTestExecutionListener.class, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
public abstract class TestEnvLoader {
    static {
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMalformed()
                .ignoreIfMissing()
                .load();
        dotenv.entries().forEach(e -> System.setProperty(e.getKey(), e.getValue()));
    }
}
