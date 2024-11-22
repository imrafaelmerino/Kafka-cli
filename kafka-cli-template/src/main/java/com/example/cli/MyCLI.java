package com.example.cli;

import com.github.imrafaelmerino.kafkacli.KafkaCLI;
import fun.gen.Gen;
import fun.gen.StrGen;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import jsonvalues.JsObj;
import jsonvalues.gen.JsDoubleGen;
import jsonvalues.gen.JsIntGen;
import jsonvalues.gen.JsObjGen;
import jsonvalues.gen.JsStrGen;

public class MyCLI {

  public static void main(String[] args) {

    Map<String, Gen<?>> generators = new HashMap<>();
    generators.put("topic1KeyGen",
                   JsObjGen.of("_id",
                               JsStrGen.alphabetic()));
    generators.put("topic1ValueGen",
                   JsObjGen.of("a",
                               JsIntGen.arbitrary(0,
                                                  1000),
                               "b",
                               JsStrGen.alphabetic(),
                               "c",
                               JsStrGen.alphabetic()
                              )
                  );
    generators.put("topic2ValueGen",
                   JsObjGen.of("id",
                               JsStrGen.alphabetic(),
                               "amount",
                               JsDoubleGen.arbitrary(100.0d,
                                                     1000d)
                              ));
    generators.put("topic3ValueGen",
                   StrGen.alphabetic(10,
                                     100));

    new KafkaCLI(generators).start(args);


  }
}
