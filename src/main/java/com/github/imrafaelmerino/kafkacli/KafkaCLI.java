package com.github.imrafaelmerino.kafkacli;

import fun.gen.Gen;
import jio.ExceptionFun;
import jio.IO;
import jio.cli.Command;
import jio.cli.Console;
import jio.cli.GenerateCommand;
import jsonvalues.JsObj;
import jsonvalues.JsObjPair;
import jsonvalues.JsPath;
import jsonvalues.spec.JsObjSpecParser;
import org.apache.avro.Schema.Parser;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.github.imrafaelmerino.kafkacli.ConfigurationFields.*;

public class KafkaCLI {


    final Map<String, Gen<?>> generators;

    public KafkaCLI(final Map<String, Gen<?>> generators) {
        this.generators = generators;
    }

    private static void validateSerializer(String keySerializerField,
                                           JsObj producerProps,
                                           String producerName
                                          ) {
        String keySerializer = producerProps.getStr(keySerializerField);
        String validSerializer = "jsonvalues.spec.serializers.confluent.ConfluentSerializer";
        if (!validSerializer.equals(keySerializer)) {
            JsPath path =
                    JsPath.fromKey(KAFKA)
                          .key(PRODUCERS)
                          .key(producerName)
                          .key(PRODUCER_PROPS)
                          .key(keySerializerField);
            throw new IllegalArgumentException("The property %s must be set to %s".formatted(path,
                                                                                             validSerializer));
        }
    }

    private static JsObj parseConf(final String[] args) throws IOException {
        JsObjSpecParser parser = JsObjSpecParser.of(ConfigurationSpec.global);
        if (args.length == 0) {
            throw new IllegalArgumentException("Pass in the configuration file");
        }
        var path = Path.of(args[0]);
        if (!path.toFile()
                 .exists()) {
            throw new IllegalArgumentException("File %s not found".formatted(path));
        }
        return parser.parse(Files.readAllBytes(path));
    }


    public IO<String> executeCommand(JsObj conf, String command) {
        return createConsole(conf).executeCommand(conf, command);
    }


    public void start(String[] args) {
        JsObj conf;
        try {
            conf = parseConf(args);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }


        Console cli = createConsole(conf);

        cli.eval(conf);

    }

    private Console createConsole(JsObj conf) {
        validate(conf);

        KsqlDbClient ksqlDbClient = new KsqlDbClient(conf.getObj(JsPath.fromKey(KAFKA).key(KSQL).key(KSQL_PROPS)));

        List<Command> myCommands = new ArrayList<>();

        KafkaProducers producers = new KafkaProducers();
        KafkaConsumers consumers = new KafkaConsumers();

        AvroSchemas avroSchemas = new AvroSchemas(conf);

        myCommands.add(new ProducerPublishCommand(generators,
                                                  producers,
                                                  avroSchemas));
        myCommands.add(new PublishFileCommand(generators,
                                              producers,
                                              avroSchemas));
        myCommands.add(new ProducerStartCommand(producers));
        myCommands.add(new ConsumerAsyncCommitCommand(consumers));
        myCommands.add(new ProducerStopCommand(producers));
        myCommands.add(new ConsumerStopCommand(consumers));
        myCommands.add(new ConsumerStartCommand(consumers));
        myCommands.add(new ConsumerListCommand(consumers));
        myCommands.add(new ProducerListCommand(producers));
        myCommands.add(new ChannelListCommand(producers));
        myCommands.add(new PullQueryStartCommand(ksqlDbClient));

        for (var genName : generators.keySet()) {
            myCommands.add(new GenerateCommand(genName,
                                               "Data Generator",
                                               generators.get(genName)
                                                         .map(Object::toString)));
        }
        Console cli = new Console(myCommands);
        return cli;
    }

    private void validate(final JsObj conf) {
        JsObj channels = conf.getObj(JsPath.fromKey(KAFKA)
                                           .key(CHANNELS));
        for (JsObjPair pair : channels) {
            String channelName = pair.key();
            JsObj channelConf = pair.value()
                                    .toJsObj();
            String producerName = channelConf.getStr(ConfigurationFields.PRODUCER);
            if (!ConfigurationQueries.getProducers(conf)
                                     .contains(producerName)) {
                throw new IllegalArgumentException("The producer `%s` associated to the channel `%s` has not been defined in %s".formatted(producerName,
                                                                                                                                           channelName,
                                                                                                                                           "/kafka/producers"));

            }
            String keyGen = channelConf.getStr(KEY_GEN);
            if (keyGen != null && !generators.containsKey(keyGen)) {
                throw new IllegalArgumentException(("The generator `%s` associated to the key of the channel `%s` has not been "
                                                    + "created.").formatted(keyGen,
                                                                            channelName));
            }

            String valueGen = channelConf.getStr(VALUE_GEN);
            if (valueGen != null && !generators.containsKey(valueGen)) {
                throw new IllegalArgumentException(("The generator `%s` associated to the value of the channel `%s` has not "
                                                    + "been created.").formatted(valueGen,
                                                                                 channelName));
            }

            String keySchema = channelConf.getStr(KEY_SCHEMA);
            if (keySchema != null) {
                try {
                    new Parser().parse(keySchema);
                } catch (Exception e) {
                    throw new IllegalArgumentException("The AVRO schema associated to the key of the channel `%s` is not valid: %s".formatted(channelName,
                                                                                                                                              ExceptionFun.findUltimateCause(e)
                                                                                                                                                          .toString()));
                }
            }
            String valueSchema = channelConf.getStr(VALUE_SCHEMA);
            if (valueSchema != null) {
                try {
                    new Parser().parse(valueSchema);
                } catch (Exception e) {
                    throw new IllegalArgumentException("The AVRO schema associated to the value of the channel `%s` is not valid: %s"
                                                               .formatted(channelName,
                                                                          ExceptionFun.findUltimateCause(e)
                                                                                      .toString()));
                }
            }

            //validate key and value serializer of producer
            JsObj producerProps = ConfigurationQueries.getProducerProps(conf,
                                                                        producerName);

            if (keySchema != null) {
                validateSerializer("key.serializer",
                                   producerProps,
                                   producerName);
            }

            if (valueSchema != null) {
                validateSerializer("value.serializer",
                                   producerProps,
                                   producerName);
            }

        }
    }

}
