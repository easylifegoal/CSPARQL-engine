package eu.larkc.csparql.data;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.FileManager;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PreProcess {
    public static final String query1 = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
            "PREFIX ub: <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#> " +
            "SELECT ?X " +
            "WHERE " +
            "{?X rdf:type ub:GraduateStudent . " +
            " ?X ub:takesCourse 'http://www.Department0.University0.edu/GraduateCourse0'}";
    
    public static final String query2 = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
            "PREFIX ub: <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#> " +
            "SELECT ?X ?Y ?Z " +
            "WHERE " +
            "{?X rdf:type ub:GraduateStudent . " +
            "?Y rdf:type ub:University . " +
            "?Z rdf:type ub:Department . " +
            "?X ub:memberOf ?Z . " +
            "?Z ub:subOrganizationOf ?Y . " +
            "?X ub:undergraduateDegreeFrom ?Y}";

    public static final String query14 = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
            "PREFIX ub: <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#> " +
            "SELECT ?X " +
            "WHERE {?X rdf:type ub:UndergraduateStudent}";

    public static final String query15 = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
            "PREFIX ub: <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#> " +
            "SELECT ?Y " +
            "WHERE " +
            "{?X ?A ?Y . " +
            "?Y ?B ?Z}";

    public static final String query16 = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
            "PREFIX ub: <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#> " +
            "SELECT ?X ?Y " +
            "WHERE " +
            "{?X ?A ?Y . " +
            "?Y ?B ?X}";

    public static final String query17 = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
            "PREFIX ub: <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#> " +
            "SELECT ?X ?Z " +
            "WHERE " +
            "{?Y ub:advisor ?Z ." +
            "?X ub:publicationAuthor ?Y}";

    public static final String query18 = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
            "PREFIX ub: <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#> " +
            "SELECT ?X ?Z " +
            "WHERE " +
            "{?Y ub:advisor ?Z ." +
            "?X ub:publicationAuthor ?Y ." +
            "?X ub:publicationAuthor ?Z}";
    
    public static void main(String[] args) {
        System.out.println(query18);
        String path = PreProcess.class.getResource("/dataset").getPath();
        Model model = ModelFactory.createDefaultModel();
        try (InputStream in = FileManager.get().open(path + "/lubm100000.nt");){
            model.read(in, null, "N3");
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(path);
        StmtIterator iterator = model.listStatements();
        StmtIterator iterator2 = model.listStatements();
        try (BufferedWriter bufferedWriter = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream("lubm100000C.txt"), StandardCharsets.UTF_8))) {
            Map<String, List<String>> listMap = new HashMap<>();
            System.out.println(model.size());
            while (iterator.hasNext()) {
                Statement statement = iterator.nextStatement();
                String subject = statement.getSubject().toString();
                String predicate = statement.getPredicate().toString();
                String object = statement.getObject().toString();
                if ("http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#memberOf".equals(predicate)) {
                    List<String> list = listMap.computeIfAbsent(object, k -> new ArrayList<>());
                    list.add(subject);
                }
//                String line = subject + "\t" + predicate + "\t" + object;
//                bufferedWriter.write(line);
//                bufferedWriter.newLine();
            }
            List<String> triples = new ArrayList<>();
            listMap.forEach((k, v) -> {
                for (int i = 1; i < v.size(); i++) {
                    triples.add(v.get(i - 1) + "\t" + "http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#schoolMate"
                            + "\t" + v.get(i));
                }
            });
            int count = 1;
            int index = 0;
            int size = triples.size();
            while (iterator2.hasNext()) {
                if (count == 13 && index < size) {
                    bufferedWriter.write(triples.get(index));
                    count = 1;
                    index++;
                } else {
                    Statement statement = iterator2.nextStatement();
                    String subject = statement.getSubject().toString();
                    String predicate = statement.getPredicate().toString();
                    String object = statement.getObject().toString();
                    String line = subject + "\t" + predicate + "\t" + object;
                    bufferedWriter.write(line);
                    count++;
                } 
                bufferedWriter.newLine();
            }
        }  catch (IOException e) {
            e.printStackTrace();
        }
//        Query query = QueryFactory.create(query17);
//        QueryExecution qe = QueryExecutionFactory.create(query, model);
//        ResultSet rs = qe.execSelect();
//        System.out.println();
//        ResultSetFormatter.out(System.out, rs, query);
//        qe.close();
    }
}
