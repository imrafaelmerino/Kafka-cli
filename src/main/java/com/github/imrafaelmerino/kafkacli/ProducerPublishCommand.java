package com.github.imrafaelmerino.kafkacli;

import fun.gen.Gen;
import jio.IO;
import jio.cli.Command;
import jio.cli.ConsolePrinter;
import jio.cli.ConsolePrograms;
import jio.cli.State;
import jsonvalues.JsObj;
import jsonvalues.JsPath;
import jsonvalues.spec.JsonToAvro;
import org.apache.avro.Schema;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.random.RandomGenerator;

import static com.github.imrafaelmerino.kafkacli.ConfigurationFields.CHANNELS;
import static com.github.imrafaelmerino.kafkacli.ConfigurationFields.KAFKA;

class ProducerPublishCommand extends Command {

    private final static RandomGenerator keySeed = new Random();
    private final static RandomGenerator valueSeed = new Random();
    private static final String COMMAND_NAME = "producer-publish";
    private static final String USAGE = """
            Usage: publish [channel-name]

            Description:
            The `producer-publish` command sends a generated key-value pair or value to a specified Kafka topic using the appropriate Kafka producer.

            Parameters:
            - channel-name (optional): The name of the channel to publish to. If not provided, the user will be prompted to select from a list of available channels with an `up` status.

            Steps:
            1. Without a channel name:
               - The command will list all available channels with an `up` status.
               - The user will be prompted to type the name of one of the listed channels.
               - If the input is invalid, the user will have three attempts to provide a correct name.

            2. With a channel name:
               - The command will directly attempt to publish to the specified channel.

            Output:
            - Success: A message indicating that the record was successfully sent, along with the offset and partition details.
            - Failure: Appropriate error message if the producer is not started or if the channel name is invalid.

            Example:
            1. Interactive mode (prompt user for channel name):
               $ producer-publish
                 Name                 Producer             Status               Topic
                 --------------------------------------------------------------------------------
                 messages             producer2            up                   messageSent
                 flights              producer1            down                 flightUpdated
                 Type the channel name (choose one of the above with an `up` Status):

            2. Direct mode (provide channel name):
               $ producer-publish channel1

            Note:
            Ensure that the channel configurations and the corresponding Kafka producers are correctly set and started before attempting to publish.
            """;
    private final KafkaProducers producers;
    private final AvroSchemas avroSchemas;
    private final Map<String, Gen<?>> generators;


    public ProducerPublishCommand(final Map<String, Gen<?>> generators,
                                  final KafkaProducers producers,
                                  final AvroSchemas avroSchemas
                                 ) {
        super(COMMAND_NAME,
              USAGE,
              args -> args[0].equals(COMMAND_NAME));
        this.generators = generators;
        this.producers = producers;
        this.avroSchemas = avroSchemas;
    }

    @Override
    public Function<String[], IO<String>> apply(final JsObj conf,
                                                final State state
                                               ) {
        return args -> {

            if (args.length == 1) {

                return ConsolePrograms.ASK_FOR_INPUT(Prompts.ASK_FOR_CHANNEL.apply(conf, producers))
                                      .then(channel -> sendGenerated(conf,
                                                                     channel)
                                           );

            } else {
                String channelName = args[1];

                return sendGenerated(conf,
                                     channelName);

            }
        };

    }

    private IO<String> sendGenerated(final JsObj conf,
                                     final String channelName
                                    ) {
        var channels = conf.getObj(JsPath.fromKey(KAFKA)
                                         .key(CHANNELS));
        var channel = channels.getObj(channelName);
        var producerName = channel.getStr(ConfigurationFields.PRODUCER);
        var topic = channel.getStr(ConfigurationFields.TOPIC);

        KafkaProducer<Object, Object> producer =
                producers.apply(producerName);
        if (producer == null) {
            return IO.fail(
                    new IllegalArgumentException("Producer `%s` not started. Use the command `start-producer %s`".formatted(producerName,
                                                                                                                            producerName)));
        }

        var keyGenName = channel.getStr(ConfigurationFields.KEY_GEN);
        var valueGenName = channel.getStr(ConfigurationFields.VALUE_GEN);

        Object value = generators.get(valueGenName)
                                 .apply(valueSeed)
                                 .get();
        if (keyGenName == null) {
            return sendValue(value,
                             channelName,
                             producer,
                             topic);
        }
        Object key = generators.get(keyGenName)
                               .apply(keySeed)
                               .get();
        return sendKeyAndValue(key,
                               value,
                               channelName,
                               producer,
                               topic);
    }

    IO<String> sendKeyAndValue(final Object key,
                               final Object value,
                               final String channelName,
                               final KafkaProducer<Object, Object> producer,
                               final String topic
                              ) {
        Schema keySchema = avroSchemas.keySchemasPerChannel.get(channelName);
        Schema valueSchema = avroSchemas.valueSchemasPerChannel.get(channelName);
        if (valueSchema != null && keySchema != null) {
            return sendRecordTask(producer,
                                  new ProducerRecord<>(topic,
                                                       JsonToAvro.convert(((JsObj) key),
                                                                          keySchema),
                                                       JsonToAvro.convert(((JsObj) value),
                                                                          valueSchema)
                                  ));
        }
        if (valueSchema != null) {
            return sendRecordTask(producer,
                                  new ProducerRecord<>(topic,
                                                       key,
                                                       JsonToAvro.convert(((JsObj) value),
                                                                          valueSchema)
                                  ));
        }
        if (keySchema != null) {
            return sendRecordTask(producer,
                                  new ProducerRecord<>(topic,
                                                       JsonToAvro.convert((JsObj) key,
                                                                          keySchema),
                                                       value
                                  ));
        }
            return sendRecordTask(producer,
                                  new ProducerRecord<>(topic,
                                                       key,
                                                       value
                              )
                             );

    }

    private IO<String> sendRecordTask(final KafkaProducer<Object, Object> producer,
                                      final ProducerRecord<Object, Object> record
                                     ) {
        return
                IO.lazy(() -> ConsolePrinter.printlnResult(Fun.getMessageSent(record)))
                  .then(v -> 
                                IO.effect(() -> producer.send(record))
                                 .map(it -> new KafkaResponse(it.timestamp(),
                                                               it.offset(),
                                                               it.partition())
                                          .getResponseReceivedMessage(record.topic()))
                       );
    }

    IO<String> sendValue(final Object value,
                         final String channelName,
                         final KafkaProducer<Object, Object> producer,
                         final String topic
                        ) {

        Schema valueSchema = avroSchemas.valueSchemasPerChannel.get(channelName);

        if (valueSchema != null) {
            return sendRecordTask(producer,
                                  new ProducerRecord<>(topic,
                                                       JsonToAvro.convert(((JsObj) value),
                                                                          valueSchema)));

        }
            return sendRecordTask(producer,
                                  new ProducerRecord<>(topic,
                                                       value));

    }
}
