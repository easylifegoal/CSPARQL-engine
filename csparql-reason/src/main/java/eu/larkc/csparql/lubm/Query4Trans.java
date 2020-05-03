package eu.larkc.csparql.lubm;

import eu.larkc.csparql.core.engine.ConsoleFormatter;
import eu.larkc.csparql.core.engine.CsparqlEngineImpl;
import eu.larkc.csparql.core.engine.CsparqlQueryResultProxy;
import eu.larkc.csparql.lubm.streamer.LubmStreamer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Query4Trans {
    private static Logger logger = LoggerFactory.getLogger(Query4Trans.class);
    public static final String rootPath = Query4Trans.class.getResource("/dataset").getPath();
    public static final  String fnMatrixE = rootPath + "/lubm100000C/MatrixE-k30-d0.1-ge0.1-gr0.1-w0.1.best";
    public static final  String fnMatrixR = rootPath + "/lubm100000C/MatrixR-k30-d0.1-ge0.1-gr0.1-w0.1.best";
    public static final  String fnTrainTriples = rootPath + "/lubm100000C/train.txt";
    public static final  String fnEntities = rootPath + "/lubm100000C/entityid.txt";
    public static final  String fnRelations = rootPath + "/lubm100000C/relationid.txt";
    public static final  String fnRules = rootPath + "/lubm100000C/lubm100000_rule";
    public static final  String fnGroundings = rootPath + "/lubm100000C/groundings.txt";
    public static final int factors = 30;
    
    public static void main(String[] args) {
        try {
            String queryBody = "REGISTER STREAM Author AS "
                    + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
                    + "PREFIX ub: <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#> "
                    + "SELECT ?X ?Y "
                    + "FROM STREAM <http://streamreasoning.org/streams/ub> [RANGE 20s STEP 1s] "
                    + "WHERE "
                    + "{?X ub:schoolMate ?Y}";

            String queryBody2 = "REGISTER STREAM Author AS "
                    + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
                    + "PREFIX ub: <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#> "
                    + "SELECT ?X ?Y "
                    + "FROM STREAM <http://streamreasoning.org/streams/ub> [RANGE 5s STEP 1s] "
                    + "WHERE "
                    + "{?X ub:publicationAuthor ?Y . "
                    + "?Y rdf:type ub:FullProfessor}";
            LubmStreamer ub = new LubmStreamer("http://streamreasoning.org/streams/ub", 3200);

            //Create csparql engine instance
            CsparqlEngineImpl engine = new CsparqlEngineImpl();

            //Initialize the engine instance
            //The initialization creates the static engine (SPARQL) and the stream engine (CEP)
            engine.initializeWithTrans(fnMatrixE, fnMatrixR, fnTrainTriples, fnEntities, fnRelations, fnRules, factors);
            //Register new stream in the engine
            engine.registerStream(ub);

            Thread ubThread = new Thread(ub);

            //Register new query in the engine
            CsparqlQueryResultProxy c = engine.registerQuery(queryBody, false);

            //Attach a result consumer to the query result proxy to print the results on the console
//            c.addObserver(new ConsoleFormatter());

            //Start the thread that put the triples in the engine
            ubThread.start();

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

    }
}
