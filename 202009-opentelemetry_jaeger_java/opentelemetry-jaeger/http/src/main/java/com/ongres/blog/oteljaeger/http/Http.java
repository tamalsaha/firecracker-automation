package com.ongres.blog.oteljaeger.http;

import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.context.Scope;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Tracer;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.concurrent.ThreadLocalRandom;


@Path("/traceme")
public class Http {
    private static final Tracer TRACER = OpenTelemetry.getTracerProvider().get(Http.class.getSimpleName());

    private ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello1() throws InterruptedException {
        Span span = TRACER.spanBuilder("hello1").startSpan();
        try(Scope scope = TRACER.withSpan(span)) {
            // Introduce some random delay
            Thread.sleep(threadLocalRandom.nextInt(1000));

            // Call nested function
            return hello2();
        } finally {
            span.end();
        }
    }

    private String hello2() throws InterruptedException {
        Span span = TRACER.spanBuilder("hello2").startSpan();
        try(Scope scope = TRACER.withSpan(span)) {
            // Introduce some random delay
            Thread.sleep(threadLocalRandom.nextInt(1000));

            return "hello";
        } finally {
            span.end();
        }
    }
}