/// Daniel "Pablo" Popovich
/// November 1, 2026
/// this takes a file in CNF and sees if the KB is satisfiable
///

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static java.lang.System.in;

public class lab2 {
    public static void main(String[] args) {
        String file = args[0];
        ArrayList<String> predicates = new ArrayList<String>();
        ArrayList<String> variables = new ArrayList<String>();
        ArrayList<String> constants = new ArrayList<String>();
        ArrayList<String> functions = new ArrayList<String>();
        ArrayList<Clause> clauses = new ArrayList<Clause>();


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
                        String[] temp = line.trim().split("\\s+");
                        Set<Literal> literals = new HashSet<>();
                        for(String s : temp){
                            Literal l;
                            if(s.contains("!")){
                                l = new Literal(s.substring(1), true);
                            } else{
                                l = new Literal(s, false);
                            }
                            literals.add(l);
                        }
                        clauses.add(new Clause(literals));


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


    /// resoves two clauses
    /// @param c1: clause 1
    /// @param c2: clause 2
    /// @reutnr arrayList of each new clause made
    public static ArrayList<Clause> resolve(Clause c1, Clause c2) {

        ArrayList<Clause> clauses = new ArrayList<>();

        for( Literal l : c1.literalSet){
            Literal nl = l.negate();
            if(c2.literalSet.contains(nl)){
                Set<Literal> literalSet = new HashSet<>();
                for(Literal i :c1.literalSet){
                    if(!i.equals(l)) {
                        literalSet.add(i);
                    }
                }
                for(Literal i :c2.literalSet){
                    if(!i.equals(nl)) {
                        literalSet.add(i);
                    }
                }

                Clause c3 = new Clause(literalSet);
                if(!c3.isTautology()){
                    clauses.add(c3);
                }

            }
        }


        return clauses;
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

        return false;
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

