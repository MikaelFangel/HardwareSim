import org.antlr.v4.runtime.tree.ParseTreeVisitor;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import org.antlr.v4.runtime.CharStreams;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class AST{};

abstract class Expr extends AST {
    abstract public int eval();
}

class Negation extends Expr {
    Expr c1;
    Negation(Expr c1) {
        this.c1 = c1;
    }

    public int eval() {
        return ~c1.eval();
    }
}

class Conjunction extends Expr {
    Expr c1, c2;
    Conjunction(Expr c1, Expr c2) {
        this.c1 = c1;
        this.c2 = c2;
    }

    public int eval() {
        return c1.eval() & c2.eval();
    }
}

class Disjunction extends Expr {
    Expr c1, c2;
    Disjunction(Expr c1, Expr c2) {
        this.c1 = c1;
        this.c2 = c2;
    }

    public int eval() {
        return c1.eval() | c2.eval();
    }
}


// Leaf of a tree
class Variable extends Expr {
    public String varName;
    Variable(String varName) {
        this.varName = varName;
    }

    public int eval() {
        System.out.println("Variable not implemented, assyming " + varName + " = 0");
        return 0;
    }
}

class Binary extends Expr {
    public int i;
    Binary(int i) {
        this.i = i;
    }

    public int eval() {
        return i;
    }
}

