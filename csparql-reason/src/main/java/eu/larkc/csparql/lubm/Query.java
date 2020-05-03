package eu.larkc.csparql.lubm;

import eu.larkc.csparql.core.engine.ConsoleFormatter;
import eu.larkc.csparql.core.engine.CsparqlEngineImpl;
import eu.larkc.csparql.core.engine.CsparqlQueryResultProxy;
import eu.larkc.csparql.lubm.streamer.LubmStreamer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Query {
    private static Logger logger = LoggerFactory.getLogger(Query.class);

    public static void main(String[] args) {
        try {
            String queryBody = "REGISTER STREAM Author AS "
                    + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
                    + "PREFIX lubm: <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#> "
                    + "SELECT ?X ?Z "
                    + "FROM STREAM lubm [RANGE 10s STEP 1s] "
                    + "WHERE "
                    + "{?Y lubm:advisor ? lubm:FullProfessor1}";

            LubmStreamer ub = new LubmStreamer("lubm", 1000);

            //Create csparql engine instance
            CsparqlEngineImpl engine = new CsparqlEngineImpl();

            //Initialize the engine instance
            //The initialization creates the static engine (SPARQL) and the stream engine (CEP)
            engine.initialize();

            //Register new stream in the engine
            engine.registerStream(ub);

            Thread ubThread = new Thread(ub);

            //Register new query in the engine
            CsparqlQueryResultProxy c = engine.registerQuery(queryBody, true);

            //Attach a result consumer to the query result proxy to print the results on the console
            c.addObserver(new ConsoleFormatter());

            //Start the thread that put the triples in the engine
            ubThread.start();

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

    }

}
