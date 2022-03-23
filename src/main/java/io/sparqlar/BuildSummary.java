package io.sparqlar;

import org.apache.jena.query.*;
import org.apache.jena.tdb2.TDB2Factory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class BuildSummary {
    public static void main(String[] args) throws IOException {
        Dataset dataset = TDB2Factory.connectDataset("C:\\Users\\frosi\\OneDrive\\Desktop\\DBPedia\\tdb\\");
        dataset.begin(ReadWrite.READ);
        File f = new File("C:\\Users\\frosi\\OneDrive\\Desktop\\DBPedia\\summary_" + 3 + ".ttl");
        createSchema(dataset, f, 3);
        dataset.close();
    }


    public static void createSchema(Dataset dataset, File f, int n) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(f));
        for (int i = 1; i < n; i++) {
            System.out.println("Path size " + i);
            ResultSet execSelect = QueryExecutionFactory.create(constructQuery(i), Syntax.syntaxARQ, dataset).execSelect();
            while (execSelect.hasNext()) {
                int hash = 0;
                QuerySolution next = execSelect.next();
                for (int j = 0; j < i; j++) {
                    int oldHash = hash;
                    hash = hash * 3 + next.getResource("?p" + j).hashCode();
                    System.out.println(oldHash + " " + next.getResource("?p" + j).getURI() + " " + hash);
                    if (j == i - 1) bw.write(oldHash + " " + next.getResource("?p" + j).getURI() + " " + hash + "\n");
                }
            }
        }
        System.out.println("Path size " + n);
        ResultSet execSelect = QueryExecutionFactory.create(constructQuery(n), Syntax.syntaxARQ, dataset).execSelect();
        while (execSelect.hasNext()) {
            int hash = 0;
            int finalHash = 0;
            QuerySolution next = execSelect.next();
            for (int j = 0; j < n - 1; j++) {
                int oldHash = hash;
                hash = hash * 3 + next.getResource("?p" + j).hashCode();
                if (j > 0) finalHash = finalHash * 3 + next.getResource("?p" + j).hashCode();
                System.out.println(oldHash + " " + next.getResource("?p" + j).getURI() + " " + hash);
                //bw.write(oldHash+ " "+next.getResource("?p"+j).getURI()+" "+hash+"\n");
            }
            finalHash = finalHash * 3 + next.getResource("?p" + (n - 1)).hashCode();
            System.out.println(hash + " " + next.getResource("?p" + (n - 1)).getURI() + " " + finalHash);
            bw.write(hash + " " + next.getResource("?p" + (n - 1)).getURI() + " " + finalHash + "\n");

        }
        bw.flush();
        bw.close();
    }

    public static String constructQuery(int n) {
        String ret = "SELECT DISTINCT ";
        String where = "WHERE{";
        for (int i = 0; i < n; i++) {
            ret = ret + "?p" + i + " ";
            where = where + "?s" + i + " ?p" + i + " ?s" + (int) (i + 1) + " . ";
        }
        where = where.substring(0, where.length() - 3);
        ret = ret + where + "}";
        return ret;
    }
}
