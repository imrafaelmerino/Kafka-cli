package com.github.imrafaelmerino.kafkacli;

import jio.IO;
import jio.RetryPolicies;
import jio.cli.ConsolePrograms.AskForInputParams;
import jsonvalues.JsObj;

import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import static jio.cli.ConsolePrograms.ASK_FOR_INPUT;

class Prompts {

    static final BiFunction<JsObj, KafkaProducers, AskForInputParams> ASK_FOR_CHANNEL =
            (conf, producers) ->
                    new AskForInputParams("%s\n%s".formatted(ConfigurationQueries.getChannelsInfo(conf,
                                                                                                  producers),
                                                             "\nType the channel name (choose one of the above with an `up` Status):"),
                                          channel -> ConfigurationQueries.existChannel(conf,
                                                                                       channel)
                                                     &&
                                                     ConfigurationQueries.isChannelUp(conf,
                                                                                      channel,
                                                                                      producers),
                                          "Invalid channel name or channel producer is not up.",
                                          RetryPolicies.limitRetries(2));

    static final BiFunction<JsObj, KsqlDbClient, AskForInputParams> ASK_FOR_PUSH_QUERY =
            (conf, ksqlDbClient) ->
                    new AskForInputParams("%s\n%s".formatted(ConfigurationQueries.getPushQueriesInfo(conf,
                                                                                                     ksqlDbClient),
                                                             "\nType the push query name (choose one of the above with a `down` Status):"),
                                          query -> ConfigurationQueries.existPusQuery(conf,
                                                                                      query)
                                                   &&
                                                   ConfigurationQueries.isPushQueryRunning(conf,
                                                                                           query,
                                                                                           ksqlDbClient),
                                          "Invalid push query name or is already running.",
                                          RetryPolicies.limitRetries(2));

    static final Function<JsObj, IO<String>> ASK_FOR_PULL_QUERY =
            conf ->
                    ASK_FOR_INPUT(new AskForInputParams("%s\n%s".formatted(String.join("\n",
                                                                                       ConfigurationQueries.getPullQueriesInfo(conf)),
                                                                           "\nType the query name (One of the above):"),
                                                        query -> ConfigurationQueries.existPullQuery(conf,
                                                                                                     query),
                                                        "Invalid pull query name.",
                                                        RetryPolicies.limitRetries(2))
                                 );

    static final Function<Set<String>, IO<String>> ASK_FOR_PRODUCER =
            allProducers ->
                    ASK_FOR_INPUT(new AskForInputParams("%s\n%s".formatted(String.join("\n",
                                                                                       allProducers),
                                                                           "\nType the producer name (One of the above):"),
                                                        allProducers::contains,
                                                        "Invalid producer name.",
                                                        RetryPolicies.limitRetries(2))
                                 );
    static final Function<Set<String>, AskForInputParams> ASK_FOR_CONSUMER_PARAMS =
            allConsumers -> new AskForInputParams("%s\n%s".formatted(String.join("\n",
                                                                                 allConsumers),
                                                                     "\nType the consumer name (choose one of the above):"),
                                                  allConsumers::contains,
                                                  "Invalid consumer name.",
                                                  RetryPolicies.limitRetries(2));
}
