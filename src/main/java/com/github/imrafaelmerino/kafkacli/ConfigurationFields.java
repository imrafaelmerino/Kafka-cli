package com.github.imrafaelmerino.kafkacli;

final class ConfigurationFields {

    public static final String PRODUCER_PROPS = "props";
    public static final String CONSUMER_PROPS = "props";
    public static final String COMMON_PROPS = "props";
    public static final String PRODUCER = "producer";
    public static final String KEY_SCHEMA = "key-schema";
    public static final String KEY_GEN = "key-generator";
    public static final String VALUE_GEN = "value-generator";
    public static final String VALUE_SCHEMA = "value-schema";
    public static final String POLL_TIMEOUT_SEC = "pollTimeoutSec";
    public static final String KAFKA = "kafka";
    public static final String PRODUCERS = "producers";
    public static final String CONSUMERS = "consumers";
    public static final String CHANNELS = "channels";
    public static final String TOPICS = "topics";
    public static final String TOPIC = "topic";
    public static final String KSQL = "ksql";
    public static final String KSQL_PROPS = "props";
    public static final String KSQL_HOST = "host";
    public static final String KSQL_PORT = "port";
    public static final String KSQL_QUERIES = "queries";
    public static final String KSQL_PUSH_QUERIES = "push";
    public static final String KSQL_PULL_QUERIES = "pull";
    private ConfigurationFields() {
    }

}
