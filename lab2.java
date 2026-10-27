/// Daniel "Pablo" Popovich
/// November 1, 2026
/// this takes a file in CNF and sees if the KB is satisfiable
///

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class lab2 {
    public static void main(String[] args) {
        String file = args[0];
        ArrayList<String> predicates = new ArrayList<String>();
        ArrayList<String> variables = new ArrayList<String>();
        ArrayList<String> constants = new ArrayList<String>();
        ArrayList<String> functions = new ArrayList<String>();
        ArrayList<String> clauses = new ArrayList<String>();


        try(BufferedReader br = new BufferedReader(new FileReader(file))){
            String line;
            while ((line = br.readLine()) != null) {
                String lower = line.toLowerCase();
                if(lower.contains("predicates")){
                    String[] temp = line.trim().split("\\s+");
                    for(String s : temp){
                        predicates.add(s);
                    }
                    predicates.remove(0);
                }
                if(lower.contains("variables")){
                    String[] temp = line.trim().split("\\s+");
                    for(String s : temp){
                        variables.add(s);
                    }
                    variables.remove(0);
                }

                if(lower.contains("constants")){
                    String[] temp = line.trim().split("\\s+");
                    for(String s : temp){
                        constants.add(s);
                    }
                    constants.remove(0);
                }

                if(lower.contains("functions")){
                    String[] temp = line.trim().split("\\s+");
                    for(String s : temp){
                        functions.add(s);
                    }
                    functions.remove(0);
                }

                if(lower.contains("clauses")) {
                    while ((line = br.readLine()) != null) {
                        clauses.add(line);
                    }
                }

            }

            System.out.println(predicates);
            System.out.println(constants);
            System.out.println(variables);
            System.out.println(functions);
            System.out.println(clauses);


        } catch(IOException e) {
            e.printStackTrace();
            System.err.println("Error reading file");
        }




    }
}

