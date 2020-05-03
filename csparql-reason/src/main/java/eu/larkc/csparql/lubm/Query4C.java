package eu.larkc.csparql.lubm;

import eu.larkc.csparql.common.utils.CsparqlUtils;
import eu.larkc.csparql.common.utils.ReasonerChainingType;
import eu.larkc.csparql.core.engine.ConsoleFormatter;
import eu.larkc.csparql.core.engine.CsparqlEngineImpl;
import eu.larkc.csparql.core.engine.CsparqlQueryResultProxy;
import eu.larkc.csparql.lubm.streamer.LubmStreamer;
import eu.larkc.csparql.lubm.streamer.LubmStreamerC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Query4C {
    private static Logger logger = LoggerFactory.getLogger(Query4C.class);

    public static void main(String[] args) {
        String rootPath = Query4C.class.getResource("/").getPath();
        logger.info("path is {}", rootPath);
        try {
            String queryBody = "REGISTER STREAM Author AS "
                    + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
                    + "PREFIX ub: <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#> "
                    + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
                    + "SELECT ?X ?Y "
                    + "FROM STREAM <http://streamreasoning.org/streams/ub> [RANGE 8s STEP 1s] "
                    + "WHERE "
                    + "{?X rdfs:schoolMate ?Y}";

            String queryBody2 = "REGISTER STREAM Author AS "
                    + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
                    + "PREFIX ub: <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#> "
                    + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
                    + "SELECT ?X ?Z "
                    + "FROM STREAM <http://streamreasoning.org/streams/ub> [RANGE 40s STEP 1s] "
                    + "WHERE "
                    + "{?X rdfs:publicationAuthor ?Y}";
            LubmStreamerC ub = new LubmStreamerC("http://streamreasoning.org/streams/ub", 1600);

            //Create csparql engine instance
            CsparqlEngineImpl engine = new CsparqlEngineImpl();

            //Initialize the engine instance
            //The initialization creates the static engine (SPARQL) and the stream engine (CEP)
            engine.initialize();

            //Register new stream in the engine
            engine.registerStream(ub);

            Thread ubThread = new Thread(ub);

            //Register new query in the engine
            CsparqlQueryResultProxy c = engine.registerQuery(queryBody2, false);

            //Attach a result consumer to the query result proxy to print the results on the console
//            c.addObserver(new ConsoleFormatter());

            //Start the thread that put the triples in the engine
            ubThread.start();
            engine.updateReasoner(c.getSparqlQueryId(),
                    CsparqlUtils.fileToString(rootPath + "examples_files/rdfs.rules"),
                    ReasonerChainingType.FORWARD, CsparqlUtils.serializeRDFFile(rootPath + "examples_files/univ-bench163.nt"));

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

    }

}
