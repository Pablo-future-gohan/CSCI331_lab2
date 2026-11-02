/// Daniel "Pablo" Popovich
/// November 1, 2026
/// this takes a file in CNF and sees if the KB is satisfiable
///

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;

public class lab2 {
    public static void main(String[] args) {
        String file = args[0];
        ArrayList<String> predicates = new ArrayList<String>();
        ArrayList<String> variables = new ArrayList<String>();
        ArrayList<String> constants = new ArrayList<String>();
        ArrayList<String> functions = new ArrayList<String>();
        ArrayList<String> clauses = new ArrayList<String>();


        //reads arguments and puts everything into a list
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

//this class is to turn every string literal into an object
class Literal{
    final boolean isNegative; //whether or not hte predicate is negated
    final String predicateName; //name of the predicat4e


    //makes a literal object
    Literal(String predicateName, boolean isNegative) {
        this.isNegative = isNegative;
        this.predicateName = predicateName;
    }

    //makes a new literal objext but negated
    Literal negate(){
        return new Literal(predicateName, !isNegative);
    }

    @Override
    public int hashCode(){
        return Objects.hash(isNegative, predicateName);
    }

    //equal one another if they both have the same predicate name and both are negated or not
    @Override
    public boolean equals(Object o){
        if(!(o instanceof Literal)){
            return false;
        } else{
            Literal other = (Literal) o;
            if (isNegative == other.isNegative && predicateName.equals(other.predicateName)){
                return true;
            }
            return false;
        }
    }
}



class Clause{
    final Set<Literal> literalSet; //a set of each literal

    Clause(Set<Literal> literalSet){
        this.literalSet = literalSet;
    }

    boolean isEmpty(){
        return literalSet.isEmpty();
    }

    //checls if the clause is a tautology if it contains any pair of true and not true literals
    boolean isTautology(){
        for(Literal literal : literalSet){
            if(literalSet.contains(literal.negate())){
                return true;
            }
        }

        return falswe;
    }


    @Override
    public boolean equals(Object o){
        if(!(o instanceof Clause)){
            return false;
        }  else{
            Clause other = (Clause) o;
            if(literalSet.equals(other.literalSet)){
                return true;
            }
            return false;
        }
    }

    @Override
    public int hashCode(){
        return Objects.hash(literalSet);
    }
}

