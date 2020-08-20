package com.ongres.javaprocessexamples;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import com.ongres.process.FluentProcess;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.buildobjects.process.ProcBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.StartedProcess;

public class SimpleExamplesTest {

  @BeforeEach
  public void beforeEach() {
    System.out.println();
  }

  @Test
  public void jdkExample() throws Exception {
    ProcessBuilder sedBuilder = new ProcessBuilder("sed", "s/world/process/");

    Process sed = sedBuilder.start();

    InputStream inputStream = new ByteArrayInputStream(
        "hello\nworld".getBytes(StandardCharsets.UTF_8));
    final CompletableFuture<Void> sedInput = CompletableFuture.runAsync(() -> {
      try {
        try {
          byte[] buffer = new byte[8192];
          while (true) {
            int size = inputStream.read(buffer);
            if (size < 0) {
              break;
            }
            if (size > 0) {
              sed.getOutputStream().write(buffer, 0, size);
            }
          }
        } finally {
          sed.getOutputStream().close();
        }
      } catch (IOException ex) {
        throw new RuntimeException(ex);
      }
    });

    try {
      byte[] buffer = new byte[8192];
      while (true) {
        int size = sed.getInputStream().read(buffer);
        if (size < 0) {
          break;
        }
        if (size > 0) {
          System.out.write(buffer, 0, size);
        }
      }
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
    sed.waitFor();
    if (sed.exitValue() != 0) {
      throw new Exception(
          "Process " + sedBuilder.command() + " has failed with exit code " + sed.exitValue());
    }
    sedInput.join();
  }

  @Test
  public void commonsExecExample() throws Exception {
    CommandLine sed = new CommandLine("sed")
        .addArgument("s/world/process/");

    DefaultExecuteResultHandler sedExecutionHandler = new DefaultExecuteResultHandler();
    Executor sedExecutor = new DefaultExecutor();
    sedExecutor.setStreamHandler(new PumpStreamHandler(System.out, null,
        new ByteArrayInputStream("hello\nworld".getBytes(StandardCharsets.UTF_8))));

    sedExecutor.execute(sed, sedExecutionHandler);

    sedExecutionHandler.waitFor();
    if ((sedExecutionHandler.getExitValue() != 0)
        || sedExecutionHandler.getException() != null) {
      if (sedExecutionHandler.getException() != null) {
        throw new Exception(
            "Process " + sed + " has failed with exit code " + sedExecutionHandler.getExitValue(),
            sedExecutionHandler.getException());
      }
      throw new Exception(
          "Process " + sed + " failed with exit code " + sedExecutionHandler.getExitValue());
    }
  }

  @Test
  public void ztExecExample() throws Exception {
    ProcessExecutor sedExecutor = new ProcessExecutor("sed", "s/world/process/")
        .redirectInput(new ByteArrayInputStream("hello\nworld".getBytes(StandardCharsets.UTF_8)))
        .redirectOutput(System.out);

    StartedProcess sed = sedExecutor
        .start();

    ProcessResult sedResult = sed.getFuture().get();
    if (sedResult.getExitValue() != 0) {
      throw new Exception(
          "Process " + sedExecutor.getCommand()
          + " has failed with exit code " + sedResult.getExitValue());
    }
  }

  @Test
  public void jprocExample() throws Exception {
    new ProcBuilder("sed", "s/world/process/")
        .withInputStream(new ByteArrayInputStream("hello\nworld".getBytes(StandardCharsets.UTF_8)))
        .withOutputStream(System.out)
        .run();
  }

  @Test
  public void fluentProcessExample() {
    FluentProcess.start("sed", "s/world/process/")
        .inputStream(new ByteArrayInputStream("hello\nworld".getBytes(StandardCharsets.UTF_8)))
        .writeToOutputStream(System.out);
  }
}
