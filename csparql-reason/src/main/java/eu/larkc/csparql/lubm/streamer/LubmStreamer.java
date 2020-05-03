package eu.larkc.csparql.lubm.streamer;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.FileManager;
import eu.larkc.csparql.cep.api.RdfQuadruple;
import eu.larkc.csparql.cep.api.RdfStream;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalTime;

public class LubmStreamer extends RdfStream implements Runnable {
    private int streamRate;

    public LubmStreamer(String iri, int streamRate) {
        super(iri);
        this.streamRate = streamRate;
    }

    @Override
    public void run() {
        String path = this.getClass().getResource("/dataset").getPath();
        Model model = ModelFactory.createDefaultModel();
        try (InputStream in = FileManager.get().open(path + "/lubm100000C.nt")){
            model.read(in, null, "N3");
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(path);
        StmtIterator iterator = model.listStatements();
        int count = 0;
        LocalTime start = LocalTime.now();
        while (iterator.hasNext() && count < streamRate) {
            Statement statement = iterator.nextStatement();
            String subject = statement.getSubject().toString();
            String predicate = statement.getPredicate().toString();
            String object = statement.getObject().toString();
            RdfQuadruple rdf = new RdfQuadruple(subject, predicate, object, System.currentTimeMillis());
            this.put(rdf);
            count++;
            if (count == streamRate) {
                LocalTime end = LocalTime.now();
                long rest = 1000 - Duration.between(start, end).toMillis();
                System.out.println(rest + " ms");
                try {
                    Thread.sleep(rest > 0 ? rest : 0);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                count = 0;
                start = LocalTime.now();
            }
        }
    }
}
