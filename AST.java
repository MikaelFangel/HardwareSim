import org.antlr.v4.runtime.tree.ParseTreeVisitor;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import org.antlr.v4.runtime.CharStreams;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class AST{};

abstract class Update extends AST {
    abstract public void eval();
}


abstract class Condition {

}

class Negation extends Condition {

}

class Conjunction extends Condition {

}

class Disjunction extends Condition {

}

class Parentheses extends Condition {

}

class Variable extends Condition {
    public String varName;
    
    Variable(String varName) {
        this.varName = varName;
    }

}
