package eu.larkc.csparql.data;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class ConvertDataForm {
    public static void main(String[] args) {
        String path = PreProcess.class.getResource("/dataset").getPath();
        System.out.println(path);
        try (BufferedWriter bufferedWriter = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream("lubm100000C.nt"), StandardCharsets.UTF_8));
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
                new FileInputStream(path + "/lubm100000C.txt")))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] tokes = line.split("\t");
                for (int i = 0; i < 3; i++) {
                    if (tokes[i].startsWith("http")) {
                        tokes[i] = "<" + tokes[i] + ">";
                    } else {
                        tokes[i] = '"' + tokes[i] + '"';
                    } 
                }
                bufferedWriter.write( tokes[0] + "\t" + tokes[1] + "\t" + tokes[2] + "\t.");
                bufferedWriter.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
