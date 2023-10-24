package run;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import btg.BackTrackGraph;
import btg.Annotation;

import parser.Parser;
import extended_parser_domain.Specification;

public class Main {

    public static void main(String[] args) {
        try {
            Instant start = Instant.now();

            // Parsing the specification file
            String file_loc = args[0];
            int randoms = Integer.parseInt(args[1]);
            int sequences = Integer.parseInt(args[2]);
            int threshold = Integer.parseInt(args[3]);

            System.out.println("generation setup:");
            System.out.println("  sequences = " + sequences);
            System.out.println("  randoms   = " + randoms);
            System.out.println("  threshold = " + threshold + "\n");

            Specification spec = Parser.parse(file_loc);

            // Building the graph and generating the sequences
            BackTrackGraph btg = new BackTrackGraph(spec, randoms, sequences, threshold);

            // Nominal sequences
            System.out.println("------------------ NOMINAL ------------------");
            List<List<Annotation>> nominal = btg.generateCallSequences(true);
            btg.printCallSequences(nominal, true);

            // Faulty sequences
            List<List<Annotation>> faulty = btg.generateCallSequences(false);
            System.out.println("\n\n------------------ FAULTY  ------------------");
            btg.printCallSequences(faulty, false);

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
