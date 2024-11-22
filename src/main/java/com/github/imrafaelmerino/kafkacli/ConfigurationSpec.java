package com.github.imrafaelmerino.kafkacli;

import jsonvalues.JsNull;
import jsonvalues.spec.JsObjSpec;
import jsonvalues.spec.JsSpec;
import jsonvalues.spec.JsSpecs;


import static com.github.imrafaelmerino.kafkacli.ConfigurationFields.*;

final class ConfigurationSpec {


    /**
     * any kind of map of values, where a value is a string, boolean or integer number In our case we model all the
     * kafka properties with this spec
     */
    static final JsSpec propsSpec =
            JsSpecs.mapOfSpec(JsSpecs.oneSpecOf(JsSpecs.str(),
                                                JsSpecs.bool(),
                                                JsSpecs.integer(),
                                                JsSpecs.longInteger(),
                                                JsSpecs.cons(JsNull.NULL))
                             );
    /**
     * Kafka producer spec
     */
    static final JsSpec producerSpec =
            JsObjSpec.of(PRODUCER_PROPS,
                         propsSpec);
    static final JsSpec consumerSpec =
            JsObjSpec.of(CONSUMER_PROPS,
                         propsSpec,
                         POLL_TIMEOUT_SEC,
                         JsSpecs.integer(n -> n > 1),
                         TOPICS,
                         JsSpecs.arrayOfStr()
                        );
    static final JsSpec channelSpec =
            JsObjSpec.of(PRODUCER,
                         JsSpecs.str(),
                         TOPIC,
                         JsSpecs.str(),
                         KEY_SCHEMA,
                         JsSpecs.str(),
                         VALUE_SCHEMA,
                         JsSpecs.str(),
                         KEY_GEN,
                         JsSpecs.str(),
                         VALUE_GEN,
                         JsSpecs.str()
                        )
                     .withOptKeys(KEY_SCHEMA,
                                  VALUE_SCHEMA,
                                  KEY_GEN,
                                  VALUE_GEN);

    static final JsObjSpec global =
            JsObjSpec.of(KAFKA,
                         JsObjSpec.of(COMMON_PROPS,
                                      propsSpec,
                                      PRODUCERS,
                                      JsSpecs.mapOfSpec(producerSpec),
                                      CONSUMERS,
                                      JsSpecs.mapOfSpec(consumerSpec),
                                      CHANNELS,
                                      JsSpecs.mapOfSpec(channelSpec)
                                     ).withAllOptKeys()
                        ).lenient();
    private ConfigurationSpec() {
    }
}
