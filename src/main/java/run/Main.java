package run;

import odg.OperationDependencyGraph;
import odg.exceptions.VertexDoesNotExistException;
import parser.Parser;
import parser_domain.Specification;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import btg.BackTrackGraph;
import btg.BackTrackGraph;

import java.time.Duration;
import java.time.Instant;

public class Main {

    public static void main(String[] args) throws FileNotFoundException, VertexDoesNotExistException {
        String file_loc = "src/main/resources/tournaments-magmact-extended.json";
        Specification spec = Parser.parse(file_loc);
   
        Instant start = Instant.now();
        BackTrackGraph btg = new BackTrackGraph(spec.getOperations(),false,10,10,100);
        Instant end = Instant.now();
        long time = Duration.between(start, end).toMillis();
        long milliseconds = time;
        long minutes = (milliseconds / 1000) / 60;
        long seconds = (milliseconds / 1000) % 60;
        System.out.println();
        System.out.println(milliseconds + " Milliseconds = "
                           + minutes + " minutes and "
                           + seconds + " seconds.");
    
      
    }


}
