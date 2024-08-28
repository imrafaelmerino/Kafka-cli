package com.github.imrafaelmerino.kafkacli;

import jsonvalues.JsObj;
import jsonvalues.JsObjPair;
import jsonvalues.JsPath;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Parser;

import java.util.HashMap;
import java.util.Map;

class AvroSchemas {

    Parser avroParser = new Parser();
    Map<String, Schema> keySchemasPerChannel = new HashMap<>();

    Map<String, Schema> valueSchemasPerChannel = new HashMap<>();


    public AvroSchemas(final JsObj conf) {

        for (JsObjPair pair : conf.getObj(JsPath.fromKey(ConfigurationFields.KAFKA).key(ConfigurationFields.CHANNELS))) {
            String channel = pair.key();
            JsObj channelConf = pair.value()
                                    .toJsObj();
            String keySchema = channelConf.getStr(ConfigurationFields.KEY_SCHEMA);
            String valueSchema = channelConf.getStr(ConfigurationFields.VALUE_SCHEMA);
            if (valueSchema != null) {
                valueSchemasPerChannel.put(channel,
                                           avroParser.parse(valueSchema));
            }
            if (keySchema != null) {
                keySchemasPerChannel.put(channel,
                                         avroParser.parse(keySchema));
            }
        }

    }


}
