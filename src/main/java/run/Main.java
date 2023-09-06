package run;

import btg.Annotation;
import odg.exceptions.VertexDoesNotExistException;
import parser.Parser;
import parser_domain.Specification;

import java.io.FileNotFoundException;

import btg.BackTrackGraph;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class Main {

    public static void main(String[] args) throws FileNotFoundException, VertexDoesNotExistException {
        try {
            Instant start = Instant.now();

            // Parsing the specification file
            String file_loc = args[0];
            boolean nominal = Integer.parseInt(args[1]) == 1;
            int rands = Integer.parseInt(args[2]);
            int seqs = Integer.parseInt(args[3]);
            int threshold = Integer.parseInt(args[4]);

            Specification spec = Parser.parse(file_loc);

            // Building the graph and generating the sequences
            BackTrackGraph btg = new BackTrackGraph(spec, nominal, rands, seqs, threshold);
            List<List<Annotation>> call_sequences = btg.generateCallSequences();
            btg.printCallSequences(call_sequences);

            Instant end = Instant.now();
            long time = Duration.between(start, end).toMillis();
            long milliseconds = time;
            long minutes = (milliseconds / 1000) / 60;
            long seconds = (milliseconds / 1000) % 60;
            System.out.println();
            System.out.println(milliseconds + " Milliseconds = "
                    + minutes + " minutes and "
                    + seconds + " seconds.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
