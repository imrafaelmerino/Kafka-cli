package com.github.imrafaelmerino.kafkacli;

import io.confluent.ksql.api.client.*;
import jio.IO;
import jio.cli.ConsolePrinter;
import jsonvalues.JsObj;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class KsqlDbClient {

    private static final ExecutorService executorService = Executors.newCachedThreadPool();
    final Map<String, FlagQueryResult> runningQueries = new HashMap<>();
    private final Client client;

    public KsqlDbClient(JsObj ksqlConf) {
        String host = ksqlConf.getStr("host");
        int port = ksqlConf.getInt("port");
        ClientOptions options = ClientOptions.create().setHost(host).setPort(port);
        client = Client.create(options);
    }


    public IO<String> startPushQuery(String label, String query, Duration timeout) {

        Callable<Void> task = () -> {
            StreamedQueryResult result = client.streamQuery(query).get();
            String queryId = result.queryID();
            System.out.println("Push query `%s` response received!".formatted(label));
            FlagQueryResult conf = new FlagQueryResult(label, queryId, true, result);
            runningQueries.put(label, conf);
            while (conf.keepRunning) {
                Row row = result.poll(timeout);
                ConsolePrinter.printlnResult(row.toString());
            }
            System.out.println("Push query `%s` ended!".formatted(label));

            return null;
        };

        return IO.effect(() -> {
                     return executorService.submit(task);
                 })
                 .map(it -> "Push query `%s` request sent!".formatted(label));

    }

    public String startPullQuery(String query) throws ExecutionException, InterruptedException {
        BatchedQueryResult result = client.executeQuery(query);
        return String.join("\n", result.get().stream().map(Object::toString).toList());

    }

    public void stopPusQuery(String label) {
        if (runningQueries.containsKey(label)) {
            runningQueries.get(label).keepRunning = false;
        }
    }


    public void closeClient() {
        client.close();
    }

    static final class FlagQueryResult {
        final String queryId;
        final StreamedQueryResult result;
        final String label;
        boolean keepRunning;

        FlagQueryResult(String label, String queryId, boolean keepRunning, StreamedQueryResult result) {
            this.label = label;
            this.queryId = queryId;
            this.keepRunning = keepRunning;
            this.result = result;
        }


        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (FlagQueryResult) obj;
            return Objects.equals(this.label, that.label);
        }

        @Override
        public int hashCode() {
            return Objects.hash(label);
        }


    }
}
