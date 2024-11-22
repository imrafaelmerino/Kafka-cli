package com.github.imrafaelmerino.kafkacli;

import java.time.Instant;

record KafkaResponse(long timestamp,
                     long offset,
                     int partition) {


  String getResponseReceivedMessage(String topic) {
    return String.format(
        """
            Publish response received:
              Topic: %s
              Offset: %d
              Partition: %d
              Timestamp: %s""",
        topic,
        offset,
        partition,
        Instant.ofEpochMilli(timestamp)
                        );

  }


}
