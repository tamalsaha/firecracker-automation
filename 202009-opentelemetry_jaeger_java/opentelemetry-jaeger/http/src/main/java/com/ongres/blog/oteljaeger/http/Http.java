package com.ongres.blog.oteljaeger.http;

import io.opentelemetry.trace.Tracer;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.concurrent.ThreadLocalRandom;

import static com.ongres.blog.oteljaeger.http.TracingUtils.doInTracerSpanScope;


@Path("/traceme")
public class Http {
    private static final Tracer TRACER = TracingUtils.getClassTracer(Http.class);

    private ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello1() {
        return doInTracerSpanScope(TRACER, "hello1", (span, scope) -> {
            // Introduce some random delay
            try {
                Thread.sleep(threadLocalRandom.nextInt(1000));
            } catch (InterruptedException e) {}

            // Call nested function
            return hello2();
        });
    }

    private String hello2() {
        return doInTracerSpanScope(TRACER, "hello2", (span, scope) -> {
            // Introduce some random delay
            try {
                Thread.sleep(threadLocalRandom.nextInt(1000));
            } catch (InterruptedException e) {}

            return "hello";
        });
    }
}