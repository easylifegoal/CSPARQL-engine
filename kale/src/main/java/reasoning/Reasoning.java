package reasoning;

import kale.struct.Matrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalTime;
import java.util.*;

public class Reasoning {
    private String fnMatrixE;
    private String fnMatrixR;
    private String fnTrainTriples;
    private String fnEntities;
    private String fnRelations;
    private String fnRules;
    private int factors;
    private DataProcess dataProcess;
    private static final Logger logger = LoggerFactory.getLogger(Reasoning.class);

    public Reasoning(String fnMatrixE, String fnMatrixR, String fnTrainTriples, String fnEntities, String fnRelations,
                     String fnRules, int factors) {
        this.fnMatrixE = fnMatrixE;
        this.fnMatrixR = fnMatrixR;
        this.fnTrainTriples = fnTrainTriples;
        this.fnEntities = fnEntities;
        this.fnRelations = fnRelations;
        this.fnRules = fnRules;
        this.factors = factors;
        DataProcess t = new DataProcess();
        dataProcess = new DataProcess();
    }

    public void readData() throws IOException {
        dataProcess.readEntities(fnEntities);
        dataProcess.readRelations(fnRelations);
        dataProcess.readRules(fnRules);
        dataProcess.readMatrix(fnMatrixE, fnMatrixR, factors);
//        dataProcess.readTrainTripleSet(fnTrainTriples, fnGroundings);
    }

    public Map<String, Integer> getEntity2Idx() {
        return dataProcess.getEntity2idx();
    }

    public Map<Integer, String> getIdx2Entity() {
        return dataProcess.getIdx2Entity();
    }

    public Map<String, Integer> getRelation2Idx() {
        return dataProcess.getRelation2idx();
    }

    public Map<Integer, String> getIdx2Relation() {
        return dataProcess.getIdx2Relation();
    }

    public void calc(String fnTestTriples, String fnGroundings) throws IOException {
        dataProcess.readTestTriple(fnTestTriples);
        Set<String> groundings = dataProcess.getGroundingSet(fnGroundings);
        PriorityQueue<Double> max = new PriorityQueue<>(100, Collections.reverseOrder());
        PriorityQueue<Double> min = new PriorityQueue<>(100);
        Map<Integer, List<List<Integer>>> rules = dataProcess.getRules();
        Map<Integer, List<Set<Integer>>> streamMap = dataProcess.getStreamMap();
        Matrix matrixE = dataProcess.getMatrixE();
        Matrix matrixR = dataProcess.getMatrixR();
        double gate = -3;
        StringBuilder line = new StringBuilder();
        while (gate < -1) {
            logger.info("gate is {}", gate);
            List<String> trueList = new ArrayList<>();
            List<String> falseList = new ArrayList<>();
            double finalGate = gate;
            rules.forEach((k, v) -> {
                for (List<Integer> rel : v) {
                    Set<Integer> fstRel = streamMap.get(rel.get(0)).get(0);
                    Set<Integer> sndRel = streamMap.get(rel.get(1)).get(1);
                    for (int subjectId : fstRel) {
                        for (int objectId : sndRel) {
                            double value = 0;
                            line.delete(0, line.length());
                            line.append(subjectId).append("\t").append(k).append("\t").append(objectId);
                            for (int p = 0; p < factors; p++) {
                                try {
                                    value -= Math.abs(matrixE.get(subjectId, p)
                                            + matrixR.get(k, p) - matrixE.get(objectId, p));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            min.add(value);
                            max.add(value);
                            if (value > finalGate) trueList.add(line.toString());
                            else falseList.add(line.toString());
                        }
                    }
                }
            });
            int tp = 0;
            int tn = 0;
            int fp = 0;
            int fn = 0;
            for (String triple : trueList) {
                if (groundings.contains(triple)) tp++;
                else fp++;
            }
            for (String triple : falseList) {
                if (groundings.contains(triple)) fn++;
                else tn++;
            }
            logger.info("tp is {}, fp is {}, tn is {}, fn is {}, trueList size is {}, falseList size is {}",
                    tp, fp, tn, fn, trueList.size(), falseList.size());
            double p = (tp + tn) * 1.0 / (tp + fp + tn + fn);
            double r = tp * 1.0 / (tp + fn);
            logger.info("p is {}, r is {}", String.format("%.3f", p), String.format("%.3f", r));
            gate += 0.1;
        }
    }

    public List<int[]> calc4Stream(List<int[]> testList) {
        dataProcess.readTestList(testList);
        List<String> reasoningList = new ArrayList<>();
        List<int[]> result = new ArrayList<>();
        Map<Integer, List<List<Integer>>> rules = dataProcess.getRules();
        Map<Integer, List<Set<Integer>>> streamMap = dataProcess.getStreamMap();
        Matrix matrixE = dataProcess.getMatrixE();
        Matrix matrixR = dataProcess.getMatrixR();
        Set<String> trainTripleSet = dataProcess.getTrainTripleSet();
        double gate = -0.2;
        LocalTime reasoningStart = LocalTime.now();
        rules.forEach((k, v) -> {
            for (List<Integer> rel : v) {
                Set<Integer> fstRel = streamMap.get(rel.get(0)).get(0);
                Set<Integer> sndRel = streamMap.get(rel.get(1)).get(1);
                for (int subjectId : fstRel) {
                    for (int objectId : sndRel) {
                        double value = 0;
                        String line = subjectId + "\t" + k + "\t" + objectId;
                        for (int p = 0; p < factors; p++) {
                            try {
                                value -= Math.abs(matrixE.get(subjectId, p)
                                        + matrixR.get(k, p) - matrixE.get(objectId, p));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        if (value > gate) {
                            result.add(new int[]{subjectId, k, objectId});
                            reasoningList.add(line);
                        }
                    }
                }
            }
        });
        LocalTime reasoningEnd = LocalTime.now();
        logger.info(Duration.between(reasoningStart, reasoningEnd).toMillis() + "ms");
        //identify
        double correct = 0;
        for (String s : reasoningList) {
            if (trainTripleSet.contains(s)) {
                correct++;
            }
        }
        System.out.println(String.format("%.3f", correct / reasoningList.size()));
        return result;
    }

    public List<int[]> calc4StreamByMap(Map<Integer, List<Set<Integer>>> streamMap) {
        List<int[]> result = new ArrayList<>();
        Map<Integer, List<List<Integer>>> rules = dataProcess.getRules();
        Matrix matrixE = dataProcess.getMatrixE();
        Matrix matrixR = dataProcess.getMatrixR();
        double gate = -1.5;
        LocalTime reasoningStart = LocalTime.now();
        rules.forEach((k, v) -> {
            for (List<Integer> rel : v) {
                Set<Integer> fstRel = streamMap.get(rel.get(0)).get(0);
                Set<Integer> sndRel = streamMap.get(rel.get(1)).get(1);
                for (int subjectId : fstRel) {
                    for (int objectId : sndRel) {
                        double value = 0;
                        String line = subjectId + "\t" + k + "\t" + objectId;
                        for (int p = 0; p < factors; p++) {
                            try {
                                value -= Math.abs(matrixE.get(subjectId, p)
                                        + matrixR.get(k, p) - matrixE.get(objectId, p));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        if (value > gate) {
                            result.add(new int[]{subjectId, k, objectId});
                        }
                    }
                }
            }
        });
        LocalTime reasoningEnd = LocalTime.now();
        logger.info("calc time is {}", Duration.between(reasoningStart, reasoningEnd).toMillis() + "ms");
//        //identify
//        double correct = 0;
//        for (String s : reasoningList) {
//            if (trainTripleSet.contains(s)) {
//                correct++;
//            }
//        }
//        logger.info(String.format("%.3f", correct / reasoningList.size()));
        return result;
    }

    public static void main(String[] args) throws Exception {
        String rootPath = Reasoning.class.getResource("/dataset").getPath();
        int iFactors = 50;
        String fnMatrixE = rootPath + "/lubm100000/MatrixE.real.best";
        String fnMatrixR = rootPath + "/lubm100000/MatrixR.real.best";
        String fnTrainTriples = rootPath + "/lubm100000/train.txt";
        String fnEntities = rootPath + "/lubm100000/entityid.txt";
        String fnRelations = rootPath + "/lubm100000/relationid.txt";
        String fnRules = rootPath + "/lubm100000/lubm100000_rule";
        String fnGroundings = rootPath + "/lubm100000/groundings.txt";
        String fnTestTriples = rootPath + "/lubm100000/test.txt";

        Reasoning reasoning = new Reasoning(fnMatrixE, fnMatrixR, fnTrainTriples,
                fnEntities, fnRelations, fnRules, iFactors);
        reasoning.readData();
        LocalTime start = LocalTime.now();
        reasoning.calc(fnTestTriples, fnGroundings);
        LocalTime end = LocalTime.now();
        logger.info("{} ms!", Duration.between(start, end).toMillis());
    }

}
