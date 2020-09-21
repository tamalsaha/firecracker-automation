package com.ongrs.blog.oteljaeger.main;

import io.opentelemetry.exporters.jaeger.JaegerGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.Samplers;
import io.opentelemetry.sdk.trace.config.TraceConfig;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;


public class TracerProvider {
    private static final String TRACER_SERVICE_NAME = "opentelemetry-jaeger";

    public static void initialize(String jaegerEndpoint, double samplingPercentage) {
        JaegerGrpcSpanExporter jaegerExporter =
                JaegerGrpcSpanExporter.newBuilder()
                        .setEndpoint(jaegerEndpoint)
                        .setServiceName(TRACER_SERVICE_NAME)
                        .build();

        var tracerProvider = OpenTelemetrySdk.getTracerProvider();

        tracerProvider.addSpanProcessor(
                BatchSpanProcessor.newBuilder(jaegerExporter).build()
        );

        TraceConfig traceConfig = TraceConfig.getDefault().toBuilder().setSampler(
                Samplers.probability(samplingPercentage)
        ).build();

        tracerProvider.updateActiveTraceConfig(traceConfig);
    }
}
