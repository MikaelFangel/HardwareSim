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
    // Glocal variable for keeping track of the current cycle
    public static int cycle = 0;

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
        // Initialize latches to 0
        for(var l : latches) {
            l.initialize(env);
        }
    }

    public void nextCycle(Environment env) {
        // Execute all update statements
        update.eval(env);
        // Execute all latches
        for(var l : latches)
            l.nextCycle(env);
    }

    public void runSimulator(Environment env) {
        initialize(env);
        //TODO: What if multiple input strings?
        int bitLength = simulate.simIn.get(0).inputSignal.length();
        while(cycle < bitLength) {
            nextCycle(env);
            cycle++;
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

    // Initialize input variables in hashmap
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

    // Initialize output variables in hashmap
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
        String outputBinaries;
        try {
             outputBinaries = env.getVariable(output.varname);
        } catch(Exception e) {
            outputBinaries = "";
            env.setVariable(output.varname, outputBinaries);
        }
        outputBinaries += '0';
        env.setVariable(output.varname, outputBinaries);
    }

    // Set the output value to the current value of the input 
    public void nextCycle(Environment env) {
        String inputBitString;
        String outputBitString;
        try {
             inputBitString = env.getVariable(input.varname);
        } catch(Exception e) {
            inputBitString = "";
            env.setVariable(output.varname, inputBitString);
        }
        try {
             outputBitString = env.getVariable(output.varname);
        } catch(Exception e) {
            outputBitString = "";
            env.setVariable(output.varname, outputBitString);
        }
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
    String binaries;
        try {
             binaries = env.getVariable(vari);
        } catch(Exception e) {
            binaries = "";
            env.setVariable(vari, binaries);
        }
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
    public String inputSignal;

    public SimIn(String variable, String inputSignal) {
        this.variable = variable;
        this.inputSignal = inputSignal;
    }

    // Store the input bitstring in hashmap
    public void eval(Environment env) {
        env.setVariable(variable, inputSignal);
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
        if(c1.eval(env).equals("0"))
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
        if(c1.eval(env).equals("1") && c2.eval(env).equals("1"))
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
        if(c1.eval(env).equals("0") && c2.eval(env).equals("0"))
            return "0";
        return "1";
    }
}

class Variable extends Expr {
    String varname;

    public Variable(String varname) {
        this.varname = varname;
    }

    // Get the current cycle bit from varname
    public String eval(Environment env) {
        String bitString;
        try {
            bitString = env.getVariable(varname);
        } catch(Exception e) {
            bitString = "";
            env.setVariable(varname, bitString);
        }
        String bit = bitString.charAt(Prog.cycle) + "";
        return bit;
    }
}
