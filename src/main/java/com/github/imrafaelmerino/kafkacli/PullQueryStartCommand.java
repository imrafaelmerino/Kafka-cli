package com.github.imrafaelmerino.kafkacli;

import jio.IO;
import jio.cli.Command;
import jio.cli.State;
import jsonvalues.JsObj;
import jsonvalues.JsPath;

import java.util.function.Function;

import static com.github.imrafaelmerino.kafkacli.ConfigurationFields.*;

class PullQueryStartCommand extends Command {

    static final String CREATE_PULL_QUERY_COMMAND = "ksql-pull-query";

    static String USAGE = """
            Usage: ksql-pull-query [query-name]

            Description:
            The `ksql-pull-query` command initiates a KSQL pull query using the provided configuration.

            Parameters:
            - query-name (optional): The name of the pull query to start. If not provided, the user will be prompted to select from a list of available pull queries.

            Steps:
            1. Without a query name:
               - The command will list all available pull queries.
               - The user will be prompted to type the name of one of the listed queries.
               - If the input is invalid, the user will have two attempts to provide a correct name.

            2. With a query name:
               - The command will directly attempt to start the specified pull query.

            Output:
            - Success: "Query `<query-name>` started!"
            - Failure: Appropriate error message if the configuration is not found or if the query is already started.

            Example:
            1. Interactive mode (prompt user for query name):
               $ ksql-pull-query
               query1
               query2
               query3
               Type the query name (One of the above):

            2. Direct mode (provide query name):
               $ ksql-pull-query query1

            Note:
            Ensure that the query configurations are correctly set in the configuration file before starting a pull query.
            """;

    private final KsqlDbClient client;


    public PullQueryStartCommand(final KsqlDbClient client) {
        super(CREATE_PULL_QUERY_COMMAND,
              USAGE,
              tokens -> tokens[0].equals(CREATE_PULL_QUERY_COMMAND));
        this.client = client;
    }

    @Override
    public Function<String[], IO<String>> apply(final JsObj conf,
                                                final State state
                                               ) {
        return args -> {
            if (args.length == 1) {

                return Prompts.ASK_FOR_PULL_QUERY.apply(conf)
                                                 .then(producer -> start(conf,
                                                                         producer)
                                                      );
            }

            return start(conf,
                         args[1]);

        };
    }

    private IO<String> start(final JsObj conf,
                             final String queryName
                            ) {
        return IO.task(() -> {


            var query = conf.getStr(JsPath.fromKey(KAFKA)
                                          .key(KSQL)
                                          .key(KSQL_QUERIES)
                                          .key(KSQL_PULL_QUERIES)
                                          .key(queryName));

            return client.startPullQuery(query);
        });
    }
}
