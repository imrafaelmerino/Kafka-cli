package com.github.imrafaelmerino.kafkacli;

import static com.github.imrafaelmerino.kafkacli.ConfigurationFields.CHANNELS;
import static com.github.imrafaelmerino.kafkacli.ConfigurationFields.CONSUMERS;
import static com.github.imrafaelmerino.kafkacli.ConfigurationFields.KAFKA;
import static com.github.imrafaelmerino.kafkacli.ConfigurationFields.PRODUCER;
import static com.github.imrafaelmerino.kafkacli.ConfigurationFields.PRODUCERS;
import static com.github.imrafaelmerino.kafkacli.ConfigurationFields.PRODUCER_PROPS;
import static com.github.imrafaelmerino.kafkacli.ConfigurationFields.TOPIC;
import static com.github.imrafaelmerino.kafkacli.ConfigurationFields.TOPICS;

import java.util.Set;
import java.util.stream.Collectors;
import jsonvalues.JsObj;
import jsonvalues.JsPath;
import jsonvalues.JsStr;

public class ConfigurationQueries {

  public static JsObj getProducerProps(
      JsObj conf,
      String producerName
                                      ) {
    return conf.getObj(JsPath.fromKey(KAFKA)
                             .key(PRODUCERS)
                             .key(producerName)
                             .key(PRODUCER_PROPS));
  }

  public static boolean isChannelUp(
      JsObj conf,
      String name,
      KafkaProducers producers
                                   ) {
    String producer = conf.getStr(JsPath.fromKey(KAFKA)
                                        .key(CHANNELS)
                                        .key(name)
                                        .key(PRODUCER));
    return producer != null && producers.apply(producer) != null;
  }


  public static boolean existChannel(
      JsObj conf,
      String name
                                    ) {
    return conf.getObj(JsPath.fromKey(KAFKA)
                             .key(CHANNELS)
                      )
               .containsKey(name);
  }


  public static String getChannelsInfo(
      JsObj conf,
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

  static JsObj getConsumerConf(
      JsObj conf,
      String consumerName
                              ) {
    JsObj consumers = conf.getObj(JsPath.fromKey(KAFKA)
                                        .key(CONSUMERS));
    return consumers.getObj(consumerName);
  }

  static Set<String> getConsumerTopics(
      JsObj conf,
      String consumerName
                                      ) {
    return getConsumerConf(conf,
                           consumerName).getArray(TOPICS)
                                        .streamOfValues()
                                        .map(it -> ((JsStr) it).value)
                                        .collect(Collectors.toSet());
  }

}
