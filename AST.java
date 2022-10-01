import org.antlr.v4.runtime.tree.ParseTreeVisitor;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import org.antlr.v4.runtime.CharStreams;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class AST{};

abstract class Prog extends AST {
    abstract public Boolean eval(Environment env);
}

/*class Output extends Prog {
        
}*/

class Latch extends Prog {
    Variable input;
    Variable output;

    public Latch(Variable input, Variable output) {
        this.input = input;
        this.output = output;
    }
    
    public Boolean eval(Environment env) {
        env.setVariable(input.varName, input.eval(env));
        env.setVariable(output.varName, output.eval(env));
    }
    
    public void initialize(Environment env) {
        env.setVariable(output.varName, false);
    }

    public void nextCycle(Environment env) {
        env.setVariable(input.varName, output.eval(env));
    }
}

class Update extends Prog {
   Variable v;
   Expr e;

   public Update(Variable v, Expr e) {
       this.v = v;
       this.e = e;
    }
   public Boolean eval(Environment env) {
       env.setVariable(v.varName, e.eval(env));
    }
}

abstract class Expr extends AST {
    abstract public Boolean eval(Environment env);
}

class Negation extends Expr {
    Expr c1;
    Negation(Expr c1) {
        this.c1 = c1;
    }

    public Boolean eval(Environment env) {
        return !c1.eval(env);
    }
}

class Conjunction extends Expr {
    Expr c1, c2;
    Conjunction(Expr c1, Expr c2) {
        this.c1 = c1;
        this.c2 = c2;
    }

    public Boolean eval(Environment env) {
        return c1.eval(env) & c2.eval(env);
    }
}

class Disjunction extends Expr {
    Expr c1, c2;
    Disjunction(Expr c1, Expr c2) {
        this.c1 = c1;
        this.c2 = c2;
    }

    public Boolean eval(Environment env) {
        return c1.eval(env) | c2.eval(env);
    }
}


// Leaf of a tree
class Variable extends Expr {
    public String varName;
    Variable(String varName) {
        this.varName = varName;
    }

    public Boolean eval(Environment env) {
        //System.out.println("Variable not implemented, assyming " + varName + " = 0");
        return env.getVariable(varName);
    }
}

class Binary extends Expr {
    public Boolean i;
    Binary(Boolean i) {
        this.i = i;
    }

    public Boolean eval(Environment env) {
        return i;
    }
}

