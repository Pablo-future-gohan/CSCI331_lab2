/// Daniel "Pablo" Popovich
/// November 1, 2026
/// this takes a file in CNF and sees if the KB is satisfiable
///

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.util.*;

import static java.lang.System.in;

public class lab2 {
    public static void main(String[] args) {
        String file = args[0];
        ArrayList<String> predicates = new ArrayList<String>();
        ArrayList<String> variables = new ArrayList<String>();
        ArrayList<String> constants = new ArrayList<String>();
        ArrayList<String> functions = new ArrayList<String>();
        ArrayList<Clause> clauses = new ArrayList<Clause>(); //list of every clause. allows repeats
        Set<Clause> clauseSet = new HashSet<Clause>(); //each individual clause


        //reads arguments and puts everything into a list
        try(BufferedReader br = new BufferedReader(new FileReader(file))){
            String line;
            while ((line = br.readLine()) != null) {
                String lower = line.toLowerCase();
                if (lower.contains("predicates")) {
                    String[] temp = line.trim().split("\\s+");
                    for (String s : temp) {
                        predicates.add(s);
                    }
                    predicates.remove(0);
                }
                if (lower.contains("variables")) {
                    String[] temp = line.trim().split("\\s+");
                    for (String s : temp) {
                        variables.add(s);
                    }
                    variables.remove(0);
                }

                if (lower.contains("constants")) {
                    String[] temp = line.trim().split("\\s+");
                    for (String s : temp) {
                        constants.add(s);
                    }
                    constants.remove(0);
                }

                if (lower.contains("functions")) {
                    String[] temp = line.trim().split("\\s+");
                    for (String s : temp) {
                        functions.add(s);
                    }
                    functions.remove(0);
                }

                //turns every line into a clause object
                if (lower.contains("clauses")) {
                    while ((line = br.readLine()) != null) {
                        String[] temp = line.trim().split("\\s+");
                        Set<Literal> literals = new HashSet<>();
                        for (String s : temp) {
                            Literal l;
                            if (s.contains("!")) {
                                l = new Literal(s.substring(1), true);
                            } else {
                                l = new Literal(s, false);
                            }
                            literals.add(l);
                        }
                        clauses.add(new Clause(literals));
                    }
                }

                for (Clause clause : clauses) {
                    clauseSet.add(clause);
                }

            }

//            System.out.println(predicates);
//            System.out.println(constants);
//            System.out.println(variables);
//            System.out.println(functions);
//            System.out.println(clauses);



            //this resolves each clause to see if the KB is satisfieble or not.
            // Loops until the empty set is found or nothing new can be created
            while(true) {
                ArrayList<Clause> newClauses = new ArrayList<>();
                //loop that resolves each clause
                for (int i = 0; i < clauses.size() - 1; i++) {
                    for (int j = i+1; j < clauses.size(); j++) {
                        ArrayList<Clause> c = resolve(clauses.get(i), clauses.get(j));
                        for (Clause r : c) {

                            //empty set
                            if (r.isEmpty()) {
                                System.out.println("no");
                                return;

                                //if clause hasnt been found before it gets added
                            } else {
                                for (Clause c1 : c) {
                                    if (!clauseSet.contains(c1)) {
                                        clauseSet.add(c1);
                                        newClauses.add(c1);
                                    }
                                }
                            }
                        }
                    }
                }

                //stops program if no new clauses are made
                if (newClauses.isEmpty()) {
                    System.out.println("yes");
                    return;

                    //addes eevry new clause to the clauses list
                } else{
                    for (Clause c : newClauses) {
                        clauses.add(c);
                    }
                }
            }




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

        //checks if the negation of each literal in c1 is in c2
        //and adds a new clause to clauses each time
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

                //ignores tauatololgy because they arent helpful and it speeds up computer
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
    final String predicateName;//name of the predicat4e



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


    @Override
    public String toString(){
        if(isNegative){
            return "!"+predicateName;
        }
        return predicateName;
    }
}


//makes a clause object
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

    @Override
    public String toString(){
        return literalSet.toString();
    }
}

//this class is for stuff inside predicates like variables and constants
class Term{
    final String name; //term name
    final boolean isVariable; //if term is variable
    final boolean isFunction; //if term is func
    final boolean isConstant; //if term is constant
    final List<Term> functionArgs; //list of terms in a function (eg friends(kim, doug))


    //makes a term object
    Term(String name, boolean isVariable, boolean isFunction, boolean isConstant,  List<Term> functionArgs){
        this.name = name;
        this.isVariable = isVariable;
        this.isFunction = isFunction;
        this.isConstant = isConstant;
        this.functionArgs = new ArrayList<>(functionArgs);
    }

    @Override
    public boolean equals(Object o){
        if(!(o instanceof Term)){
            return false;
        }
        Term other = (Term) o;
        if(other.name.equals(this.name)
                && other.isVariable == this.isVariable
                && other.isFunction == this.isFunction
                && other.isConstant == this.isConstant
                && other.functionArgs.equals(this.functionArgs)){
            {
                return true;
            }
        }

        return false;
    }


    @Override
    public String toString() {
        return "(name: "+ name + ") (isVariable: " + isVariable + ") (isConstant: " + isConstant + ") (isFunction: " + isFunction + ") (functionArgs:" + functionArgs +")";
    }


    @Override
    public int hashCode() {
        return name.hashCode() + Boolean.hashCode(isVariable) + Boolean.hashCode(isFunction) + Boolean.hashCode(isConstant) + functionArgs.hashCode();
    }
}