package com.ongrs.blog.oteljaeger.main;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import org.eclipse.microprofile.config.inject.ConfigProperty;


@QuarkusMain
public class Main {
    public static void main(String[] args) {
        Quarkus.run(MainApp.class, args);
    }

    public static class MainApp implements QuarkusApplication {
        @ConfigProperty(name = "config.jaegerEndpoint")
        String jaegerEndpoint;

        @ConfigProperty(name = "config.samplingPercentage")
        double samplingPercentage;

        @Override
        public int run(String... args) throws Exception {
            TracerProvider.initialize(jaegerEndpoint, samplingPercentage);

            Quarkus.waitForExit();

            return 0;
        }
    }
}
