import org.antlr.v4.runtime.tree.ParseTreeVisitor;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import org.antlr.v4.runtime.CharStreams;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class AST{};

abstract class Condition extends AST {

}

class And extends Condition {

}

class Or extends Condition {

}

class Not extends Condition {

}

class Constant extends Condition {

}

class Identifier extends Condition {

}
