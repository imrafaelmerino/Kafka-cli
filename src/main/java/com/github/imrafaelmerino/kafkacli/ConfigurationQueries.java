package com.github.imrafaelmerino.kafkacli;

import jsonvalues.JsObj;
import jsonvalues.JsPath;

import java.util.Set;
import java.util.stream.Collectors;

import static com.github.imrafaelmerino.kafkacli.ConfigurationFields.*;

public class ConfigurationQueries {

    public static JsObj getProducerProps(JsObj conf,
                                         String producerName
                                        ) {
        return conf.getObj(JsPath.fromKey(KAFKA)
                                 .key(PRODUCERS)
                                 .key(producerName)
                                 .key(PRODUCER_PROPS));
    }

    public static boolean isChannelUp(JsObj conf,
                                      String name,
                                      KafkaProducers producers
                                     ) {
        String producer = conf.getStr(JsPath.fromKey(KAFKA)
                                            .key(CHANNELS)
                                            .key(name)
                                            .key(PRODUCER));
        return producer != null && producers.apply(producer) != null;
    }

    public static boolean isPushQueryRunning(JsObj conf,
                                             String name,
                                             KsqlDbClient ksqlDbClient
                                            ) {
        JsObj queries = conf.getObj(JsPath.fromKey(KAFKA)
                                          .key(KSQL_QUERIES)
                                          .key(KSQL_PUSH_QUERIES));
        return queries.containsKey(name) && ksqlDbClient.runningQueries.containsKey(name)
               && ksqlDbClient.runningQueries.get(name).keepRunning;
    }

    public static boolean existChannel(JsObj conf,
                                       String name
                                      ) {
        return conf.getObj(JsPath.fromKey(KAFKA)
                                 .key(CHANNELS)
                          )
                   .containsKey(name);
    }

    public static boolean existPusQuery(JsObj conf,
                                        String name
                                       ) {
        return conf.getObj(JsPath.fromKey(KAFKA)
                                 .key(KSQL).key(KSQL_PUSH_QUERIES)
                          )
                   .containsKey(name);
    }


    public static String getPushQueriesInfo(JsObj conf, KsqlDbClient client) {
        JsObj queries = conf.getObj(JsPath.fromKey(KAFKA).key(KSQL)
                                          .key(KSQL_QUERIES).key(KSQL_PUSH_QUERIES));
        return queries.keySet()
                      .stream()
                      .map(key -> {

                          return String.format("%-20s %-20s %s",
                                               key,
                                               client.runningQueries.containsKey(key)
                                               && client.runningQueries.get(key).keepRunning
                                                       ? "running" : "down",
                                               queries.getStr(key)
                                              );
                      })
                      .collect(Collectors.joining("\n",
                                                  String.format("%-20s %-20s %s\n%s\n",
                                                                "Name",
                                                                "Status",
                                                                "Query",
                                                                "--------------------------------------------------------------------------------"),
                                                  ""));
    }


    public static String getPullQueriesInfo(JsObj conf) {
        JsObj queries = conf.getObj(JsPath.fromKey(KAFKA)
                                          .key(KSQL)
                                          .key(KSQL_QUERIES)
                                          .key(KSQL_PULL_QUERIES));
        return queries.keySet()
                      .stream()
                      .map(key -> {
                          return String.format("%-20s %s",
                                               key,
                                               queries.getStr(key)
                                              );
                      })
                      .collect(Collectors.joining("\n",
                                                  String.format("%-20s %s \n%s\n",
                                                                "Name",
                                                                "Query",
                                                                "--------------------------------------------------------------------------------"),
                                                  ""));
    }

    public static String getChannelsInfo(JsObj conf,
                                         KafkaProducers producers
                                        ) {
        JsObj channels = conf.getObj(JsPath.fromKey(KAFKA)
                                           .key(CHANNELS));
        return channels.keySet()
                       .stream()
                       .map(key -> {
                           String producerName = channels.getObj(key)
                                                         .getStr(PRODUCER);
                           return String.format("%-20s %-20s %-20s %-20s",
                                                key,
                                                producerName,
                                                producers.apply(producerName) != null ? "up" : "down",
                                                channels.getObj(key)
                                                        .getStr(TOPIC)
                                               );
                       })
                       .collect(Collectors.joining("\n",
                                                   String.format("%-20s %-20s %-20s %-20s\n%s\n",
                                                                 "Name",
                                                                 "Producer",
                                                                 "Status",
                                                                 "Topic",
                                                                 "--------------------------------------------------------------------------------"),
                                                   ""));
    }

    static Set<String> getProducers(final JsObj conf) {
        JsObj producers = conf.getObj(JsPath.fromKey(KAFKA)
                                            .key(PRODUCERS));
        return producers.keySet();

    }

    static Set<String> getConsumers(final JsObj conf) {
        JsObj consumers = conf.getObj(JsPath.fromKey(KAFKA)
                                            .key(CONSUMERS));
        return consumers.keySet();
    }


    public static boolean existPullQuery(JsObj conf, String name) {
        return conf.getObj(JsPath.fromKey(KAFKA).key(KSQL).key(KSQL_QUERIES).key(KSQL_PULL_QUERIES)).containsKey(name);
    }
}
