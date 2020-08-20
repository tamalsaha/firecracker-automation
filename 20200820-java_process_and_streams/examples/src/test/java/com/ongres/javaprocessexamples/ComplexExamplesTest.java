package com.ongres.javaprocessexamples;

import java.io.BufferedReader;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

public class ComplexExamplesTest {

  @BeforeEach
  public void beforeEach() {
    System.out.println();
  }

  @Test
  public void jdkExample() throws Exception {
    ProcessBuilder shBuilder = new ProcessBuilder("sh", "-c",
        Stream.of("cat", "exit 79").collect(Collectors.joining("\n")));
    ProcessBuilder sedBuilder = new ProcessBuilder("sed", "s/world/process/");
    Stream<String> inputStream = Stream.of(
        "hello",
        "world"
        );

    Process sh = shBuilder.start();
    Process sed = sedBuilder.start();

    CompletableFuture<Void> shInput = CompletableFuture.runAsync(() -> {
      try {
        try {
          Iterator<byte[]> iterator = inputStream
              .map(line -> (line + "\n").getBytes(StandardCharsets.UTF_8))
              .iterator();
          while (iterator.hasNext()) {
            sh.getOutputStream().write(iterator.next());
          }
        } finally {
          sh.getOutputStream().close();
        }
      } catch (IOException ex) {
        throw new RuntimeException(ex);
      }
    });

    CompletableFuture<Void> sedInput = CompletableFuture.runAsync(() -> {
      try {
        try {
          byte[] buffer = new byte[8192];
          while (true) {
            int size = sh.getInputStream().read(buffer);
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

    try (BufferedReader sedReader = new BufferedReader(
        new InputStreamReader(sed.getInputStream(), StandardCharsets.UTF_8))) {
      sedReader.lines()
          .peek(System.out::println)
          .collect(Collectors.toList());
      sh.waitFor();
      sed.waitFor();
      if (sh.exitValue() != 79) {
        throw new Exception(
            "Process " + shBuilder.command() + " has failed with exit code " + sh.exitValue());
      }
      if (sed.exitValue() != 0) {
        throw new Exception(
            "Process " + sedBuilder.command() + " has failed with exit code " + sed.exitValue());
      }
      shInput.join();
      sedInput.join();
    }
  }

  @Test
  public void commonsExecExample() throws Exception {
    final CommandLine sh = new CommandLine("sh")
        .addArgument("-c")
        .addArgument(Stream.of(
            "cat",
            "exit 79"
            )
            .collect(Collectors.joining("\n")));
    final CommandLine sed = new CommandLine("sed")
        .addArgument("s/world/process/");
    Stream<String> inputStream = Stream.of(
        "hello",
        "world"
        );
    InputStream shInputStream = new InputStream() {
      final Iterator<Byte> iterator = inputStream
          .map(line -> line.getBytes(StandardCharsets.UTF_8))
          .flatMap(bytes -> {
            List<Byte> byteList = new ArrayList<>(bytes.length);
            for (byte value : bytes) {
              byteList.add(value);
            }
            return byteList.stream();
          })
          .iterator();
      @Override
      public int read() throws IOException {
        if (!iterator.hasNext()) {
          return -1;
        }
        return iterator.next().byteValue();
      }
    };

    DefaultExecuteResultHandler shExecutionHandler = new DefaultExecuteResultHandler();
    Executor shExecutor = new DefaultExecutor();
    PipedOutputStream shOutputStream = new PipedOutputStream();
    PipedInputStream sedInputStream = new PipedInputStream(shOutputStream);
    shExecutor.setStreamHandler(new PumpStreamHandler(shOutputStream, null, shInputStream));

    DefaultExecuteResultHandler sedExecutionHandler = new DefaultExecuteResultHandler();
    Executor sedExecutor = new DefaultExecutor();
    PipedOutputStream sedOutputStream = new PipedOutputStream();
    PipedInputStream sedInputStreamForOutput = new PipedInputStream(sedOutputStream);
    sedExecutor.setStreamHandler(new PumpStreamHandler(sedOutputStream, null, sedInputStream));

    shExecutor.execute(sh, shExecutionHandler);
    sedExecutor.execute(sed, sedExecutionHandler);

    try (BufferedReader sedReader = new BufferedReader(
        new InputStreamReader(sedInputStreamForOutput, StandardCharsets.UTF_8))) {
      sedReader.lines()
          .peek(System.out::println)
          .collect(Collectors.toList());
      shExecutionHandler.waitFor();
      if ((shExecutionHandler.getExitValue() != 79)
          || shExecutionHandler.getException() != null) {
        if (shExecutionHandler.getException() != null) {
          throw new Exception(
              "Process " + sh + " has failed with exit code " + shExecutionHandler.getExitValue(),
              shExecutionHandler.getException());
        }
        throw new Exception(
            "Process " + sh + " has failed with exit code " + shExecutionHandler.getExitValue());
      }
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
  }

  @Test
  public void ztExecExample() throws Exception {
    Stream<String> inputStream = Stream.of(
        "hello",
        "world"
        );
    InputStream shInputStream = new InputStream() {
      final Iterator<Byte> iterator = inputStream
          .map(line -> (line + "\n").getBytes(StandardCharsets.UTF_8))
          .flatMap(bytes -> {
            List<Byte> byteList = new ArrayList<>(bytes.length);
            for (byte value : bytes) {
              byteList.add(value);
            }
            return byteList.stream();
          })
          .iterator();
      @Override
      public int read() throws IOException {
        if (!iterator.hasNext()) {
          return -1;
        }
        return iterator.next().byteValue();
      }
    };
    PipedOutputStream shOutputStream = new PipedOutputStream();
    PipedInputStream sedInputStream = new PipedInputStream(shOutputStream);

    ProcessExecutor shExecutor = new ProcessExecutor("sh", "-c",
        Stream.of("cat", "exit 79")
        .collect(Collectors.joining("\n")))
        .exitValue(79)
        .redirectInput(shInputStream)
        .redirectOutput(shOutputStream);
    PipedOutputStream sedOutputStream = new PipedOutputStream();
    PipedInputStream sedInputStreamForOutput = new PipedInputStream(sedOutputStream);
    InputStream endProtectedInputStream = new FilterInputStream(sedInputStreamForOutput) {

      @Override
      public int read() throws IOException {
        return checkEndDead(() -> super.read());
      }

      @Override
      public int read(byte[] b) throws IOException {
        return checkEndDead(() -> super.read(b));
      }

      @Override
      public int read(byte[] b, int off, int len) throws IOException {
        return checkEndDead(() -> super.read(b, off, len));
      }

      private int checkEndDead(Callable<Integer> readCall) throws IOException {
        try {
          return readCall.call();
        } catch (IOException ex) {
          if (ex.getMessage().equals("Write end dead")) {
            return -1;
          }
          throw ex;
        } catch (RuntimeException ex) {
          throw ex;
        } catch (Exception ex) {
          throw new RuntimeException(ex);
        }
      }
    };
    ProcessExecutor sedExecutor = new ProcessExecutor("sed", "s/world/process/")
        .redirectInput(sedInputStream)
        .redirectOutput(sedOutputStream);

    StartedProcess sh = shExecutor
        .start();
    StartedProcess sed = sedExecutor
        .start();

    try (BufferedReader sedReader = new BufferedReader(
        new InputStreamReader(endProtectedInputStream, StandardCharsets.UTF_8))) {
      sedReader.lines()
          .peek(System.out::println)
          .collect(Collectors.toList());
      ProcessResult shResult = sh.getFuture().get();
      ProcessResult sedResult = sed.getFuture().get();
      if (shResult.getExitValue() != 79) {
        throw new Exception(
            "Process " + shExecutor.getCommand()
            + " has failed with exit code " + shResult.getExitValue());
      }
      if (sedResult.getExitValue() != 0) {
        throw new Exception(
            "Process " + sedExecutor.getCommand()
            + " has failed with exit code " + sedResult.getExitValue());
      }
    }
  }

  @Test
  public void jprocExample() throws Exception {
    Stream<String> inputStream = Stream.of(
        "hello",
        "world"
        );
    InputStream shInputStream = new InputStream() {
      final Iterator<Byte> iterator = inputStream
          .map(line -> (line + "\n").getBytes(StandardCharsets.UTF_8))
          .flatMap(bytes -> {
            List<Byte> byteList = new ArrayList<>(bytes.length);
            for (byte value : bytes) {
              byteList.add(value);
            }
            return byteList.stream();
          })
          .iterator();
      @Override
      public int read() throws IOException {
        if (!iterator.hasNext()) {
          return -1;
        }
        return iterator.next().byteValue();
      }
    };
    CompletableFuture<List<String>> futureOutput = new CompletableFuture<>();
    new ProcBuilder("sh", "-c",
        Stream.of("cat", "exit 79")
        .collect(Collectors.joining("\n")))
        .withExpectedExitStatuses(79)
        .withInputStream(shInputStream)
        .withOutputConsumer(sedInputStream -> {
          new ProcBuilder("sed", "s/world/process/")
              .withInputStream(sedInputStream)
              .withOutputConsumer(sedInputStreamForOutput -> {
                try (BufferedReader sedReader = new BufferedReader(
                    new InputStreamReader(sedInputStreamForOutput, StandardCharsets.UTF_8))) {
                  futureOutput.complete(sedReader.lines()
                      .peek(System.out::println)
                      .collect(Collectors.toList()));
                } catch (Exception ex) {
                  futureOutput.completeExceptionally(ex);
                }
              })
              .run();
        })
        .run();
    futureOutput.join();
  }

  @Test
  public void fluentProcessExample() {
    FluentProcess.builder("sh").arg("-c")
        .multilineArg(
          "cat",
          "exit 79")
        .allowedExitCodes(Arrays.asList(79))
        .start()
        .inputStream(Stream.of("hello", "world"))
        .pipe("sed", "s/world/process/")
        .stream()
        .peek(System.out::println)
        .collect(Collectors.toList());
  }
}
