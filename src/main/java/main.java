import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class main {
    public static void main(String[] args) throws IOException {

        // we expect exactly one argument: the name of the input file
        if (args.length != 1) {
            System.err.println("\n");
            System.err.println("hwsim Interpreter\n");
            System.err.println("=================\n\n");
            System.err.println("Please give as input argument a filename\n");
            System.exit(-1);
        }
        String filename = args[0];

        // open the input file
        CharStream input = CharStreams.fromFileName(filename);

        // create a lexer/scanner
        hwsimLexer lex = new hwsimLexer(input);

        // get the stream of tokens from the scanner
        CommonTokenStream tokens = new CommonTokenStream(lex);

        // create a parser
        hwsimParser parser = new hwsimParser(tokens);

        // and parse anything from the grammar for "start"
        ParseTree parseTree = parser.start();
        if (parser.getNumberOfSyntaxErrors() > 0) {
            System.err.println("Error: Syntax does not match with hwsim grammer");
            System.exit(1);
        }

        // Construct an interpreter and run it on the parse tree
        Interpreter interpreter = new Interpreter();
        Prog result = (Prog) interpreter.visit(parseTree);
        Environment env = new Environment();
        result.eval(env);
        result.runSimulator(env);
        System.out.println(env);
    }
}

// We write an interpreter that implements interface
// "hwsimVisitor<T>" that is automatically generated by ANTLR
// This is parameterized over a return type "<T>" which is in our case
// simply a Integer.

class Interpreter extends AbstractParseTreeVisitor<AST> implements hwsimVisitor<AST> {
    public AST visitStart(hwsimParser.StartContext ctx) {
        return visit(ctx.p);
    }

    public AST visitProg(hwsimParser.ProgContext ctx) {
    List<Latch> latches= new ArrayList<>();
        for (hwsimParser.LatchContext la : ctx.l) {
            latches.add((Latch) visit(la));
        }

        return new Prog((Hardware) visit(ctx.h), 
        (Input) visit(ctx.i), 
        (Output) visit(ctx.o), 
        latches, 
        (Update) visit(ctx.u), 
        (Simulate) visit(ctx.s));
    }

   public AST visitHardware(hwsimParser.HardwareContext ctx) {
        return new Hardware(new Variable(ctx.id.getText(), ".hardware"));
    }

    public AST visitInput(hwsimParser.InputContext ctx) {
        List<Variable> ins = new ArrayList<>();
        List<Boolean> b = new ArrayList<>();
        for(var v : ctx.id)
            ins.add(new Variable(v.getText(), ".input"));

        return new Input(ins);
    }

    public AST visitOutput(hwsimParser.OutputContext ctx) {
        List<Variable> outs = new ArrayList<>();
        for(var v : ctx.id)
            outs.add(new Variable(v.getText(), ".output"));
        return new Output(outs);
    }

    public AST visitLatch(hwsimParser.LatchContext ctx) {
        return new Latch(new Variable(ctx.id1.getText(), new ArrayList<>()), new Variable(ctx.id2.getText(), new ArrayList<>()));
    }

    public AST visitUpdate(hwsimParser.UpdateContext ctx) {
        List<UpdateDec> updateDecList = new ArrayList<>();
        for(var updateDec : ctx.u) {
            updateDecList.add((UpdateDec) visit(updateDec));
        }
        return new Update(updateDecList);
    }

    public AST visitSimulate(hwsimParser.SimulateContext ctx) {
        List<SimIn> simIn = new ArrayList<>();
        for (var SimInContext : ctx.s)
            simIn.add((SimIn) visit(SimInContext));
        return new Simulate(simIn);
    }
    
    public AST visitSimIn(hwsimParser.SimInContext ctx) {
        List<Boolean> binaries = new ArrayList<>();
        for (var b : ctx.b) {
            Boolean bool = b.getText().equals("1");
            binaries.add(bool);
        }
        return new SimIn(ctx.id.getText(), binaries);
    }

    public AST visitUpdateDec(hwsimParser.UpdateDecContext ctx) {
        List<Expr> exprList = new ArrayList<>();
        for(var expr : ctx.e)
            exprList.add((Expr) visit(expr));
        return new UpdateDec(ctx.id.getText(), exprList);
    }

    public AST visitNegation(hwsimParser.NegationContext ctx) {
        return new Negation((Expr) visit(ctx.c1));
    }

    public AST visitConjunction(hwsimParser.ConjunctionContext ctx) {
        return new Conjunction((Expr) visit(ctx.c1), (Expr) visit(ctx.c2));
    }

    public AST visitDisjunction(hwsimParser.DisjunctionContext ctx) {
        return new Disjunction((Expr) visit(ctx.c1), (Expr) visit(ctx.c2));
    }

    public AST visitParentheses(hwsimParser.ParenthesesContext ctx) {
        return visit(ctx.c1);
    }

    public AST visitVariable(hwsimParser.VariableContext ctx) {
        return new Variable(ctx.x.getText(), new ArrayList<>());
    }
}
