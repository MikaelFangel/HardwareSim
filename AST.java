import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import org.antlr.v4.runtime.CharStreams;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class AST {};

class Prog extends AST {
    Hardware hardware;
    Input input;
    Output output;
    List<Latch> latches;
    Update update;
    Simulate simulate;

    public Prog(
            Hardware hardware,
            Input input,
            Output output,
            List<Latch> latches,
            Update update,
            Simulate simulate) {

        this.hardware = hardware;
        this.input = input;
        this.output = output;
        this.latches = latches;
        this.update = update;
        this.simulate = simulate;
    }

    public void eval(Environment env) {
        hardware.eval(env);
        input.eval(env);
        for(Latch l : latches)
            l.eval(env);
        output.eval(env);
        simulate.eval(env);
    }

    public void initialize(Environment env) {
        // Read input signal
        SimIn simIn = simulate.simIn.get(0);
        String inputSignal = simIn.binaries.charAt(0) + "";
        env.setVariable(simIn.variable, inputSignal);
        // Initialize latches to 0
        for(var l : latches) {
            l.initialize(env);
        }
    }

    public void nextCycle(Environment env, int i) {
        // Read input signal at cycle i
        SimIn simIn = simulate.simIn.get(0);
        String inputSignal = simIn.binaries.charAt(i) + "";
        env.setVariable(simIn.variable, inputSignal);
        // Execute all update statements
        update.eval(env);
        // Execute all latches
        for(var l : latches)
            l.nextCycle(env, i);
    }

    public void runSimulator(Environment env) {
        initialize(env);
        for (int i = 0; i < simulate.simIn.get(0).binaries.length(); i++) {
            nextCycle(env, i);
        }
    }
}

class Hardware extends AST {
    Variable vari;

    public Hardware(Variable vari) {
        this.vari = vari;
    }

    public void eval(Environment env) {
        env.setVariable(vari.varname, "");
    }
}

class Input extends AST {
    List<Variable> li;

    Input(List<Variable> li) {
        this.li = li;
    }

    public void eval(Environment env) {
        for(var v : li)
            env.setVariable(v.varname, "");
    }
}

class Output extends AST {
    List<Variable> outputs;

    Output(List<Variable> outputs) {
        this.outputs = outputs;
    }

    public void eval(Environment env) {
        for(var v : outputs){
            env.setVariable(v.varname, "");
        }
    }
}

class Latch extends AST {
    Variable input;
    Variable output;

    public Latch(Variable input, Variable output) {
        this.input = input;
        this.output = output;
    }

    public void eval(Environment env) {
        env.setVariable(input.varname, "");
        env.setVariable(output.varname, "");
    }
    
    // Initialize the output bitstring with 0
    public void initialize(Environment env) {
        String outputBinaries = env.getVariable(output.varname);
        outputBinaries += '0';
        env.setVariable(output.varname, outputBinaries);
    }

    // Set the output value to the current value of the input 
    public void nextCycle(Environment env, int cycle) {
        String inputBitString = env.getVariable(input.varname);
        String outputBitString = env.getVariable(output.varname);
        // getVariable returns the full bitstring. We use charAt to get the current inputbit, and add to the output bitstring.
        outputBitString += inputBitString.charAt(inputBitString.length()-1);
        env.setVariable(output.varname, outputBitString);
    }
}

class Update extends AST {
    List<UpdateDec> u;

    public Update(List<UpdateDec> u) {
        this.u = u;
    }
    
    // eval all update declarations
    public void eval(Environment env) {
        for(var v : u)
            v.eval(env);
    }
}

class UpdateDec extends AST {
    String vari;
    List<Expr> exprList;

    public UpdateDec(String vari, List<Expr> exprList) {
        this.vari = vari;
        this.exprList = exprList;
    }


    // eval expr and add the result to the bitstring. Store in hashmap
    public void eval(Environment env) {
        String binaries = env.getVariable(vari);
        for(var expr : exprList) {
            binaries += expr.eval(env);
        }
        env.setVariable(vari, binaries);
    }
}

class Simulate extends AST {
    List<SimIn> simIn;

    public Simulate(List<SimIn> simIn) {
        this.simIn = simIn;
    }

    public void eval(Environment env) {
        for(SimIn in : simIn) {
            in.eval(env);
        }
    }
}

class SimIn extends AST {
    public String variable;
    public String binaries;

    public SimIn(String variable, String binaries) {
        this.variable = variable;
        this.binaries = binaries;
    }

    public void eval(Environment env) {
        // TODO: Discuss what we need to do here. Right now SimIn is just used to hold the input bitstring
        //env.setVariable(vari.varname, binaries);
    }
}

abstract class Expr extends AST {
    abstract public String eval(Environment env);
}

class Negation extends Expr {
    Expr c1;

    Negation(Expr c1) {
        this.c1 = c1;
    }

    public String eval(Environment env) {
        // eval returns the full bitstring, we use charAt to get the last bit
        String b1 = c1.eval(env);
        String r1 = b1.charAt(b1.length()-1) + "";
        if(r1.equals("0"))
            return "1";
        return "0";
    }
}

class Conjunction extends Expr {
    Expr c1, c2;

    Conjunction(Expr c1, Expr c2) {
        this.c1 = c1;
        this.c2 = c2;
    }

    public String eval(Environment env) {
        // eval returns the full bitstring, we use charAt to get the last bit
        String b1 = c1.eval(env);
        String b2 = c2.eval(env);
        String r1 = b1.charAt(b1.length()-1) + "";
        String r2 = b2.charAt(b2.length()-1) + "";
        if(r1.equals("1") && r2.equals("1"))
            return "1";
        return "0";
    }
}

class Disjunction extends Expr {
    Expr c1, c2;

    Disjunction(Expr c1, Expr c2) {
        this.c1 = c1;
        this.c2 = c2;
    }

    public String eval(Environment env) {
        // eval returns the full bitstring, we use charAt to get the last bit
        String b1 = c1.eval(env);
        String b2 = c2.eval(env);
        String r1 = b1.charAt(b1.length()-1) + "";
        String r2 = b2.charAt(b2.length()-1) + "";
        if(r1.equals("0") && r2.equals("0"))
            return "0";
        return "1";
    }
}

class Variable extends Expr {
    String varname;

    public Variable(String varname) {
        this.varname = varname;
    }

    public String eval(Environment env) {
        return env.getVariable(varname);
    }
}
