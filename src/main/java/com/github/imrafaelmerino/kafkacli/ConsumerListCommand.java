package com.github.imrafaelmerino.kafkacli;

import jio.IO;
import jio.cli.Command;
import jio.cli.State;
import jsonvalues.JsObj;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

class ConsumerListCommand extends Command {

    static final String LS_CONSUMERS_COMMAND = "consumer-list";
    private static final String USAGE = """
            Usage: consumer-list

            Description:
            The `consumer-list` command lists all Kafka consumers along with their statuses (up or down).

            Output:
            - The list of consumers with their names and statuses.

            Example:
            $ consumer-list
            Name                 Status     Topics
            consumer1            up         topic2
            consumer2            down       topic1,transactions

            Note:
            Ensure that the consumer configurations are correctly set in the configuration file to accurately reflect their statuses.
            """;
    final KafkaConsumers kafkaConsumers;

    ConsumerListCommand(final KafkaConsumers kafkaConsumers) {
        super(LS_CONSUMERS_COMMAND,
              USAGE,
              tokens -> tokens[0].

                      equals(LS_CONSUMERS_COMMAND));
        this.kafkaConsumers = kafkaConsumers;
    }

    @Override
    public Function<String[], IO<String>> apply(final JsObj conf,
                                                final State state
                                               ) {
        return v -> IO.lazy(() -> {
            Set<String> result = ConfigurationQueries.getConsumers(conf);
          return result.stream()
                       .map(consumer -> {
                         var topics = ConfigurationQueries.getConsumerTopics(conf, consumer);
                         return new Object[]{
                             consumer,
                             kafkaConsumers.apply(consumer) != null ? "up" : "down",
                             String.join(",", topics)
                         };
                       })
                       .map(row -> String.format("%-20s %-10s %-50s", row[0], row[1], row[2]))
                       .collect(Collectors.joining("\n",
                                                   String.format("%-20s %-10s %-50s\n", "Name", "Status", "Topics"),
                                                   ""));

        });
    }

}
