import org.antlr.v4.runtime.tree.ParseTreeVisitor;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import org.antlr.v4.runtime.CharStreams;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class AST{};

abstract class Expr extends AST {
    abstract public boolean eval();
}

class Negation extends Expr {
    boolean c1;
    Negation(boolean c1) {
        this.c1 = c1;
    }

    public boolean eval() {
        return !c1;
    }
}

class Conjunction extends Expr {
    boolean c1, c2;
    Conjunction(boolean c1, boolean c2) {
        this.c1 = c1;
        this.c2 = c2;
    }

    public boolean eval() {
        return c1 && c2;
    }
}

class Disjunction extends Expr {
    boolean c1, c2;
    Disjunction(boolean c1, boolean c2) {
        this.c1 = c1;
        this.c2 = c2;
    }

    public boolean eval() {
        return c1 || c2;
    }

}


// Leaf of a tree
class Variable extends Expr {
    public String varName;
    Variable(String varName) {
        this.varName = varName;
    }

    public boolean eval() {
        System.out.println("Variable not implemented, assyming " + varName + " = 0");
        return false;
    }
}
