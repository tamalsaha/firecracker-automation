package com.ongres.blog.oteljaeger.http;


import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.context.Scope;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Tracer;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;


public class TracingUtils {
    public static Tracer getClassTracer(Class<?> clazz) {
        return OpenTelemetry.getTracerProvider().get(clazz.getSimpleName());
    }

    public static <T> T doInTracerSpanScope(Tracer tracer, String spanName, BiFunction<Span,Scope,T> function) {
        Span span = tracer.spanBuilder(spanName).startSpan();
        try(Scope scope = tracer.withSpan(span)) {
            return function.apply(span, scope);
        } finally {
            span.end();
        }
    }

    public static void doInTracerSpanScope(Tracer tracer, String spanName, BiConsumer<Span,Scope> function) {
        Span span = tracer.spanBuilder(spanName).startSpan();
        try(Scope scope = tracer.withSpan(span)) {
            function.accept(span, scope);
        } finally {
            span.end();
        }
    }
}
