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

    static int variableCounter = 0; //this is used for when I rename variables for unification

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
                            String name;
                            String inner;
                            boolean isNegated = false;

                            //negstive literal
                            if (s.startsWith("!")) {
                                isNegated = true;

                                //function
                                if(s.contains("(")){
                                    name = s.substring(1, s.indexOf("("));
                                    inner = s.substring(s.indexOf("(")+1, s.lastIndexOf(")"));
                                    List<String> funcArgs = splitter(inner);
                                    List<Term> t = new ArrayList<>();
                                    for (String funcArg : funcArgs) {
                                        t.add(toTerm(funcArg, variables, constants, functions));
                                    }
                                    l = new Literal(name, true, t);
                                }
                                else{
                                    l = new Literal(s.substring(1), true);
                                }
                            }

                            //non negative
                            else{

                                //functoin
                                if(s.contains("(")){
                                    name = s.substring(0, s.indexOf("("));
                                    inner = s.substring(s.indexOf("(")+1, s.lastIndexOf(")"));
                                    List<String> funcArgs = splitter(inner);
                                    List<Term> t = new ArrayList<>();
                                    for (String funcArg : funcArgs) {
                                        t.add(toTerm(funcArg, variables, constants, functions));
                                    }
                                    l = new Literal(name, false, t);
                                } else{
                                    l = new Literal(s, false);
                                }
                            }


                            literals.add(l);
                        }
                        //clauses.add(new Clause(literals));
                        Clause normalClause = normalize(new Clause(literals));
                        if(!clauseSet.contains(normalClause)){
                            clauses.add(normalClause);
                            clauseSet.add(normalClause);
                        }
                    }
                }

                for (Clause clause : clauses) {
                    clauseSet.add(clause);
                }

            }


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
                                Clause n=normalize(r);
                                if(!clauseSet.contains(n)){
                                    newClauses.add(n);
                                    clauseSet.add(n);
                                }
                                /* old code
                                for (Clause c1 : c) {
                                    if (!clauseSet.contains(c1)) {
                                        clauseSet.add(c1);
                                        newClauses.add(c1);
                                    }
                                }

                                 */
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
        Clause a = standardize(c1);
        Clause b = standardize(c2);

        //unifies literals from both
        for(Literal l1: a.literalSet){
            for(Literal l2: b.literalSet){
                Map<Term, Term> litMap= literalUnifier(l1, l2);
                if(litMap == null){
                    continue;
                }
                Set<Literal> remaining = new HashSet<>();
                for(Literal l: a.literalSet){
                    if(!l.equals(l1)){
                        remaining.add(l);
                    }
                }
                for (Literal l: b.literalSet){
                    if(!l.equals(l2)){
                        remaining.add(l);
                    }
                }

                Clause c = clauseSub(new Clause(remaining), litMap);
                if(!c.isTautology()){
                    clauses.add(c);
                }

            }
        }




        /* this is code that was used for just prop and constants
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

         */

        return clauses;
    }





    ///this binds two terms that can be binedd like a constant with a variable
    /// @param t1: term 1
    /// @param t2: term 2
    /// @return: a map where the two terms are binded together
    public static Map<Term, Term> binder(Term t1, Term t2, Map<Term, Term> substitutionMap){
        Map<Term, Term> binder = new HashMap<>(substitutionMap);

        //follows a chain of bindings for both terms
        while(t1.isVariable && substitutionMap.containsKey(t1)){
            t1=substitutionMap.get(t1);
        }
        while(t2.isVariable && substitutionMap.containsKey(t2)){
            t2=substitutionMap.get(t2);
        }

        //both are the same
        if(t1.equals(t2)){
            return binder;
        }

        //both are variables
        if(t1.isVariable && t2.isVariable){
            if(binder.containsKey(t1) && Objects.equals(binder.get(t1), t2)){
                return null;
            }
            binder.put(t1, t2);
        }

        //variable against something
        else if((t1.isVariable && !t2.isVariable) || (t2.isVariable && !t1.isVariable)){
            if(binder.containsKey(t1) && Objects.equals(binder.get(t1), t2)){
                return null;
            }

            if(t1.isVariable) {
                binder.put(t1, t2);

                //used so that if you bind a variable to a variable then rebind it you dont get an infinite loop
                if(t2.isFunction){
                    for(Term arg: t2.functionArgs){
                        while(arg.isVariable && substitutionMap.containsKey(arg)){
                            arg=substitutionMap.get(arg);
                        }
                        if(arg.equals(t1)){
                            return null;
                        }
                    }
                }

            } else{
                binder.put(t2, t1);

                //used so that if you bind a variable to a variable then rebind it you dont get an infinite loop
                if(t1.isFunction){
                    for(Term arg: t1.functionArgs){
                        while(arg.isVariable && substitutionMap.containsKey(arg)){
                            arg=substitutionMap.get(arg);
                        }
                        if(arg.equals(t2)){
                            return null;
                        }
                    }
                }

            }
        }

        //can't bind a constant to a constant
        else if(t1.isConstant && t2.isConstant){
            return null;
        }

        //cant bind a constant to a function
        else if((t1.isConstant && t2.isFunction) || (t1.isFunction && t2.isConstant)){
            return null;
        }

        //two functions
        else if(t1.isFunction && t2.isFunction){
            if((!t1.name.equals(t2.name)) || (t1.functionArgs.size() != t2.functionArgs.size())){
                return null;
            } else {
                for(int i = 0; i < t1.functionArgs.size(); i++){
                    binder = binder(t1.functionArgs.get(i), t2.functionArgs.get(i), binder);
                    if(binder == null){
                        return null;
                    }
                }
            }
        }


        return binder;
    }



    /// unifies two lietrals together
    /// @param l1: literal 1
    /// @param l2: literal 2
    /// @reutnr: a map with the literals paired up
    public static Map<Term, Term> literalUnifier(Literal l1, Literal l2){
        Map<Term, Term> literalMap = new HashMap<>();

        //different names
        if(!l1.predicateName.equals(l2.predicateName)){
           return null;
        }

        //different signs
        else if((l1.isNegative==l2.isNegative)){
            return null;
        }

        //different number of terms
        else if(l1.terms.size() != l2.terms.size()){
            return null;
        }

        //unifies each term in both literals
        else{
            for(int i = 0; i < l1.terms.size(); i++){
                literalMap = binder(l1.terms.get(i), l2.terms.get(i), literalMap);
                if(literalMap == null){
                    return null;
                }
            }
        }

        return literalMap;
    }



    /// this function takes a clause and splits along it's commas so that
    /// something like loves(SKF0(x2),x2) has each part as it's own thing
    /// //or just splits up a literal like dog(x) into each component
    /// @param s: the string from the input file that you want to split into terms and make into a literal
    /// @return a list of strings of each component of the literal
    public static List<String> splitter(String s){
        List<String> result = new ArrayList<>();
        int depth = 0; //used to see how deep a symbol is. Eg loves(SKF0(x2),x2)) the first x2 is depth of 2
        String currentPiece="";

        for(int i = 0; i < s.length(); i++){
            if(s.charAt(i) == '('){
                depth++;
                currentPiece += s.charAt(i);
            } else if(s.charAt(i) == ')'){
                depth--;
                currentPiece += s.charAt(i);
            } else if(s.charAt(i) == ',' && depth==0){
                result.add(currentPiece);
                currentPiece="";
            } else{
                currentPiece += s.charAt(i);
            }
        }

        result.add(currentPiece);
        return result;
    }


    /// this function takes a string that was made with the splitter method and turns it into a term object
    /// @param s: the string you want to make into a term
    /// @param vars: list of every variable item
    /// @param consts: list of every constant
    /// @param funcs: list of every function
    /// @return term
    public static Term toTerm(String s, ArrayList<String> vars, ArrayList<String> consts, ArrayList<String> funcs){

        //function term
        if(s.contains("(")){
            String funcName = s.substring(0, s.indexOf("("));
            String funcInner = s.substring(s.indexOf("(") + 1, s.lastIndexOf(")"));
            List<String> splitFunction = splitter(funcInner); //splits the inside of the function based on its terms
            List<Term> terms = new ArrayList<>();

            for(String str: splitFunction){
                terms.add(toTerm(str, vars, consts, funcs));
            }
            return new Term(funcName, false, true, false, terms);
        }

        //varoable
        else if(vars.contains(s)){
            return new Term(s, true, false, false, new ArrayList<>());
        }

        //constant
        else{
            return new Term(s, false, false, true, new ArrayList<>());
        }

    }


    /// substitues terms so that something like x is subbed with Kim
    /// @param t: the term
    /// @param substitutionMap: the map pairing for t
    /// @return: the term t is paired to
    public static Term termSub(Term t, Map<Term, Term> substitutionMap){
        if(t.isConstant){
            return t;
        }
        if(t.isVariable){
            if(substitutionMap.containsKey(t)){
                return termSub(substitutionMap.get(t), substitutionMap);
            }
        }

        if(t.isFunction){
            List<Term> newArgs = new ArrayList<>();
            for(Term arg : t.functionArgs){
                newArgs.add(termSub(arg, substitutionMap));
            }
            return new Term(t.name, false, true, false, newArgs);
        }
        return t;
    }

    /// substitues literal items
    /// @param l: the literal being subebed
    /// @param substitutionMap: the map with the pairing
    /// @return: the literal that l is paried with
    public static Literal literalSub(Literal l, Map<Term, Term> substitutionMap){
        List<Term> newTerms = new ArrayList<>();
        for(Term t: l.terms){
            newTerms.add(termSub(t, substitutionMap));
        }

        return new Literal(l.predicateName, l.isNegative, newTerms);
    }

    /// substitues clause objects
    /// @param c: the clause
    /// @param substitutionMap: the subsitutuon map
    /// @return: new clause with it being substitued
    public static Clause clauseSub(Clause c, Map<Term, Term> substitutionMap){
        Set<Literal> newLiterals = new HashSet<>();
        for(Literal l: c.literalSet){
            List<Term> newTerms = new ArrayList<>();
            for(Term t: l.terms){
                newTerms.add(termSub(t, substitutionMap));
            }
            newLiterals.add(new Literal(l.predicateName, l.isNegative, newTerms));
        }
        return new Clause(newLiterals);
    }


    /// this method takes a term and finds every variable in it. Used later so that variables are renamed for unifciations
    public static void varCollector(Term t, Set<Term> found){
        if(t.isVariable){
            found.add(t);
        } else if(t.isFunction){
            for(Term arg: t.functionArgs){
                varCollector(arg, found);
            }
        }
    }

    /// standardizes variables so that an x1 in one clause is different
    /// to an x1 in another clause
    public static Clause standardize(Clause c){

        //adds each variable to the vars list
        Set<Term> vars = new LinkedHashSet<>();
        for(Literal l: c.literalSet){
            for(Term t: l.terms){
                varCollector(t, vars);
            }
        }

        //renames each variable
        Map<Term, Term> rename = new LinkedHashMap<>();
        for(Term t: vars){
            variableCounter++;
            Term newVar = new Term("v"+variableCounter, true, false, false, new ArrayList<>());
            rename.put(t, newVar);
        }

        return clauseSub(c, rename);

    }


    /// this takes a variable which might be different in two clauses (eg x1-->v2 and x1-->v3) and makes them the same
    /// @param c: clause to normalize
    /// @return: normalized clause
    public static Clause normalize(Clause c){

        List<Literal> literals = new ArrayList<>();
        for(Literal l: c.literalSet){
            literals.add(l);
        }

        //a map of every literal and it's string version
        Map<Literal, String> litString = new HashMap<>();

        //this turns everything into a string, ignoring variable names to make it so that you can order
        //them and noralize easily
        for(Literal l: literals){
            String text="";
            if(l.isNegative){
                text+="-";
            }
            text+=l.predicateName;

            //variables replaced with "@" so that theyre ignored
            for(Term t: l.terms){
                if(t.isVariable){
                    text+="@";
                }
                else if(t.isConstant){
                    text+=t.name;
                } else{
                    text=text+t.name+"(";
                    for(Term funcArg: t.functionArgs){
                        if(funcArg.isVariable){
                            text+="@";
                        } else{
                            text+=funcArg.name;
                        }
                        text+=",";
                    }
                    text+=")";
                }
                text+=",";
            }
            text+=")";
            litString.put(l, text);
        }


        literals.sort(new Comparator<Literal>() {
            @Override
            public int compare(Literal o1, Literal o2) {
                int comp=litString.get(o1).compareTo(litString.get(o2));
                if(comp!=0){
                    return comp;
                }
                return o1.toString().compareTo(o2.toString());
            }
        });


        //this collects each variable in the sorted order
        Set<Term> vars = new LinkedHashSet<>();
        for(Literal l: literals){
            for(Term t: l.terms){
                varCollector(t, vars);
            }
        }

        //turns everything into the normalized form
        Map<Term, Term> rename = new LinkedHashMap<>();
        int normalizedCounter=0;
            for(Term t: vars){
                rename.put(t, new Term("$"+normalizedCounter, true, false, false, new ArrayList<>()));
                normalizedCounter++;
            }



        return clauseSub(c, rename);
    }

} //END OF LAB2 CLASS






//this class is to turn every string literal into an object
class Literal{
    final boolean isNegative; //whether or not hte predicate is negated
    final String predicateName;//name of the predicat4e
    final List<Term> terms; //list of terms inside the literal


    //makes a literal object WITH terms
    Literal(String predicateName, boolean isNegative, List<Term> terms) {
        this.isNegative = isNegative;
        this.predicateName = predicateName;
        this.terms = new ArrayList<>(terms);
    }


    //makes a literal object WITHOUT terms (used for prop and constant test cases
    Literal(String predicateName, boolean isNegative) {
        this.isNegative = isNegative;
        this.predicateName = predicateName;
        this.terms=new ArrayList<>();
    }


    //makes a new literal objext but negated
    Literal negate(){
        return new Literal(predicateName, !isNegative, terms);
    }

    @Override
    public int hashCode(){
        return Objects.hash(isNegative, predicateName, terms);
    }

    //equal one another if they both have the same predicate name and both are negated or not
    @Override
    public boolean equals(Object o){
        if(!(o instanceof Literal)){
            return false;
        } else{
            Literal other = (Literal) o;
            if (isNegative == other.isNegative && predicateName.equals(other.predicateName) && terms.equals(other.terms)){
                return true;
            }
            return false;
        }
    }


    @Override
    public String toString(){
        if(isNegative){
            return "!"+predicateName +" "+terms;
        }
        return predicateName + " "+terms;
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




