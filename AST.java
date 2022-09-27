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

class Negation extends Prog {
    Prog c1;
    Negation(Prog c1) {
        this.c1 = c1;
    }

    public Boolean eval(Environment env) {
        return ~c1.eval(env);
    }
}

class Conjunction extends Prog {
    Prog c1, c2;
    Conjunction(Prog c1, Prog c2) {
        this.c1 = c1;
        this.c2 = c2;
    }

    public Boolean eval(Environment env) {
        return c1.eval(env) & c2.eval(env);
    }
}

class Disjunction extends Prog {
    Prog c1, c2;
    Disjunction(Prog c1, Prog c2) {
        this.c1 = c1;
        this.c2 = c2;
    }

    public Boolean eval(Environment env) {
        return c1.eval(env) | c2.eval(env);
    }
}


// Leaf of a tree
class Variable extends Prog {
    public String varName;
    Variable(String varName) {
        this.varName = varName;
    }

    public Boolean eval(Environment env) {
        System.out.println("Variable not implemented, assyming " + varName + " = 0");
        return 0;
    }
}

class Binary extends Prog {
    public Boolean i;
    Binary(Boolean i) {
        this.i = i;
    }

    public Boolean eval(Environment env) {
        return i;
    }
}

