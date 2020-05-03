package reasoning;

import basic.util.StringSplitter;
import kale.struct.Matrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.time.LocalTime;
import java.util.*;

public class DataProcess {
    private Map<String, Integer> entity2idx;
    private Map<Integer, String> idx2Entity;
    private Map<String, Integer> relation2idx;
    private Map<Integer, String> idx2Relation;
    private Map<Integer, List<List<Integer>>> rules;
    private Matrix matrixE;
    private Matrix matrixR;
    private Set<String> trainTripleSet;
    private Map<Integer, List<Set<Integer>>> streamMap;

    public static final Logger logger = LoggerFactory.getLogger(DataProcess.class);

    public Map<Integer, List<List<Integer>>> getRules() {
        return rules;
    }

    public Map<Integer, List<Set<Integer>>> getStreamMap() {
        return streamMap;
    }

    public Map<String, Integer> getEntity2idx() {
        return entity2idx;
    }

    public Map<Integer, String> getIdx2Entity() {
        return idx2Entity;
    }

    public Map<Integer, String> getIdx2Relation() {
        return idx2Relation;
    }

    public Map<String, Integer> getRelation2idx() {
        return relation2idx;
    }

    public Matrix getMatrixE() {
        return matrixE;
    }

    public Matrix getMatrixR() {
        return matrixR;
    }

    public Set<String> getTrainTripleSet() {
        return trainTripleSet;
    }

    public void readEntities(String fnEntities) throws IOException {
        entity2idx = new HashMap<>();
        idx2Entity = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(fnEntities))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split("\t");
                int idx = Integer.parseInt(tokens[0]);
                entity2idx.put(tokens[1], idx);
                idx2Entity.put(idx, tokens[1]);
            }
        }
        logger.info("entities read finished!");
    }

    public void readRelations(String fnRelations) throws IOException {
        relation2idx = new HashMap<>();
        idx2Relation = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(fnRelations))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split("\t");
                int idx = Integer.parseInt(tokens[0]);
                relation2idx.put(tokens[1], idx);
                idx2Relation.put(idx, tokens[1]);
            }
        }
        logger.info("relations read finished!");
    }

    public void readTestTriple(String fnTestTriples) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(fnTestTriples))) {
            streamMap = new HashMap<>();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split("\t");
                List<Set<Integer>> temp = streamMap.computeIfAbsent(Integer.valueOf(tokens[1]),
                        k -> new ArrayList<>());
                if (temp.size() == 0) {
                    Set<Integer> index1 = new HashSet<>();
                    temp.add(index1);
                    Set<Integer> index2 = new HashSet<>();
                    temp.add(index2);
                }
                temp.get(0).add(Integer.valueOf(tokens[0]));
                temp.get(1).add(Integer.valueOf(tokens[2]));
            }
        }
    }
    
    public void readTestList(List<int[]> testList) {
        streamMap = new HashMap<>();
        for (int[] tokens: testList) {
            List<Set<Integer>> temp = streamMap.computeIfAbsent(tokens[1],
                    k -> new ArrayList<>());
            if (temp.size() == 0) {
                Set<Integer> index1 = new HashSet<>();
                temp.add(index1);
                Set<Integer> index2 = new HashSet<>();
                temp.add(index2);
            }
            temp.get(0).add(tokens[0]);
            temp.get(1).add(tokens[2]);
        }
    }

    public void readRules(String fnRules) throws IOException {
        rules = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(fnRules))) {
            String line;
            while ((line = reader.readLine()) != null) {
                //rule type 1
                if (line.split("&&").length == 1 && line.endsWith("(x,y)")) {
    //                String[] tokens = StringSplitter.RemoveEmptyEntries(StringSplitter
    //                        .split("=> ", line));
    //                int iFstRelation = relationsMap.get(tokens[0]);
    //                int iSndRelation = relationsMap.get(tokens[1]);
    //                String fstRelation = String.valueOf(iFstRelation);
    //                if (!rules.containsKey(fstRelation)) {
    //                    List<Integer> sndRuleList = new ArrayList<>();
    //                    sndRuleList.add(iSndRelation);
    //                    rules.put(fstRelation, sndRuleList);
    //                } else {
    //                    rules.get(fstRelation).add(iSndRelation);
    //                }
                }
                //rule type 2
                else if (line.split("&&").length == 1 && line.endsWith("(y,x)")) {
    //                String[] tokens = StringSplitter.RemoveEmptyEntries(StringSplitter
    //                        .split("=> ", line));
    //                int iFstRelation = relationsMap.get(tokens[0]);
    //                int iSndRelation = relationsMap.get(tokens[1]);
    //                String fstRelation = String.valueOf(iFstRelation);
    //                if (!rules.containsKey(fstRelation)) {
    //                    List<Integer> sndRuleList = new ArrayList<>();
    //                    sndRuleList.add(iSndRelation);
    //                    rules.put(fstRelation, sndRuleList);
    //                } else {
    //                    rules.get(fstRelation).add(iSndRelation);
    //                }
                }
                // rule type 3
                else {
                    String[] tokens = StringSplitter.RemoveEmptyEntries(StringSplitter
                            .split("=>& ", line));
                    int iFstRelation = relation2idx.get(tokens[0]);
                    int iSndRelation = relation2idx.get(tokens[1]);
                    int iTrdRelation = relation2idx.get(tokens[2]);
                    List<List<Integer>> temp = rules.computeIfAbsent(iTrdRelation, k -> new ArrayList<>());
                    List<Integer> iRules = new ArrayList<>();
                    iRules.add(iFstRelation);
                    iRules.add(iSndRelation);
                    temp.add(iRules);
                }
            }
        }
        logger.info("rules read finished!");
    }

    public void readMatrix(String fnMatrixE, String fnMatrixR, int factors) throws IOException {
        matrixE = new Matrix(entity2idx.size(), factors);
        matrixR = new Matrix(relation2idx.size(), factors);
        matrixE.load(fnMatrixE);
        matrixR.load(fnMatrixR);
        logger.info("Matrix read finished!");
    }

    public void readTrainTripleSet(String fnTrainTriples, String groundings) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fnTrainTriples)));
             BufferedReader reader2 = new BufferedReader(new InputStreamReader(new FileInputStream(groundings)))) {
            trainTripleSet = new HashSet<>();
            String line;
            while ((line = reader.readLine()) != null) {
                trainTripleSet.add(line);
            }
            while ((line = reader2.readLine()) != null) {
                String[] tokens = StringSplitter.RemoveEmptyEntries(StringSplitter
                        .split("\t() ", line));
                if (tokens.length == 6) {
                    trainTripleSet.add(tokens[0] + "\t" + tokens[1] + "\t" + tokens[2]);
                    trainTripleSet.add(tokens[3] + "\t" + tokens[4] + "\t" + tokens[5]);
                }
                if (tokens.length == 9) {
                    trainTripleSet.add(tokens[0] + "\t" + tokens[1] + "\t" + tokens[2]);
                    trainTripleSet.add(tokens[3] + "\t" + tokens[4] + "\t" + tokens[5]);
                    trainTripleSet.add(tokens[6] + "\t" + tokens[7] + "\t" + tokens[8]);
                }
            }
        }
        logger.info("trainTriples read finished!");
    }
    
    public Set<String> getGroundingSet(String groundings) throws IOException {
        Set<String> result = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(groundings)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = StringSplitter.RemoveEmptyEntries(StringSplitter.split("\t() ", line));
                if (tokens.length == 6) {
                    result.add(tokens[3] + "\t" + tokens[4] + "\t" + tokens[5]);
                }
                if (tokens.length == 9) {
                    result.add(tokens[6] + "\t" + tokens[7] + "\t" + tokens[8]);
                }
            }
        }
        return result;
    }
}
