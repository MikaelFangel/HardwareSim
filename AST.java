import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import org.antlr.v4.runtime.CharStreams;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class AST {
};

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
        for (Latch l : latches)
            l.eval(env);
        output.eval(env);
        simulate.eval(env);
    }

    public void initialize(Environment env) {
        // Initialize latches to 0
        for (var l : latches) {
            l.initialize(env);
        }
    }

    public void nextCycle(Environment env) {
        // Execute all update statements
        update.eval(env);
        // Execute all latches
        for (var l : latches)
            l.nextCycle(env);
    }

    public void runSimulator(Environment env) {
        initialize(env);
        // TODO: What if multiple input strings?
        int numOfCycles = 0;
        for (var input : simulate.simIn) {
            int inputSize = input.inputSignal.size();
            if (inputSize > numOfCycles) numOfCycles = inputSize;
        }
        
        while (cycle < numOfCycles) {
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
        env.setVariable(vari.varname, new ArrayList<>());
    }
}

class Input extends AST {
    List<Variable> li;

    Input(List<Variable> li) {
        this.li = li;
    }

    // Initialize input variables in hashmap
    public void eval(Environment env) {
        for (var v : li) {
            env.setVariable(v.varname, new ArrayList<>());
            env.setOutput(v.varname);
        }
    }
}

class Output extends AST {
    List<Variable> outputs;

    Output(List<Variable> outputs) {
        this.outputs = outputs;
    }

    // Initialize output variables in hashmap
    public void eval(Environment env) {
        for (var v : outputs) {
            env.setVariable(v.varname, new ArrayList<>());
            env.setOutput(v.varname);
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
        env.setVariable(input.varname, new ArrayList<>());
        env.setVariable(output.varname, new ArrayList<>());
    }

    // Initialize the output bitstring with 0
    public void initialize(Environment env) {
        List<Boolean> outputBinaries = env.getVariable(output.varname);
        if (outputBinaries == null) env.setVariable(output.varname, new ArrayList<>());

        env.getVariable(output.varname).add(false);
    }

    // Set the output value to the current value of the input

    /**
     * Executes all latches
     */
    public void nextCycle(Environment env) {
        Boolean currentCycleInput;
        // Input to the left of arrow in g4 file
        List<Boolean> inputBinaries = env.getVariable(input.varname);
        currentCycleInput = inputBinaries == null ? false : inputBinaries.get(Prog.cycle); // check cycle!!

        // Output to the right of arrow in g4 file
        if (env.getVariable(output.varname) == null)
            env.setVariable(output.varname, new ArrayList<>());

        // getVariable returns the full bitstring. We use charAt to get the current
        // inputbit, and add to the output bitstring.
        env.getVariable(output.varname).add(currentCycleInput);
    }
}

class Update extends AST {
    List<UpdateDec> u;

    public Update(List<UpdateDec> u) {
        this.u = u;
    }

    // eval all update declarations
    public void eval(Environment env) {
        for (var v : u)
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

    public void eval(Environment env) {
        // If variable doesn't exist yet, add this to environment with an empty list
        if (env.getVariable(vari) == null)
            env.setVariable(vari, new ArrayList<Boolean>());

        // Add boolean value to every variable in update for current cycle
        for (var expr : this.exprList) {
            Boolean b = expr.eval(env);
            env.getVariable(vari).add(b);
        }
    }
}

class Simulate extends AST {
    List<SimIn> simIn;

    public Simulate(List<SimIn> simIn) {
        this.simIn = simIn;
    }

    public void eval(Environment env) {
        for (SimIn in : simIn) {
            in.eval(env);
        }
    }
}

class SimIn extends AST {
    public String variable;
    public List<Boolean> inputSignal;

    public SimIn(String variable, List<Boolean> inputSignal) {
        this.variable = variable;
        this.inputSignal = inputSignal;
    }

    // Store the input bitstring in hashmap
    public void eval(Environment env) {
        env.setVariable(variable, inputSignal);
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
        return c1.eval(env) && c2.eval(env);
    }
}

class Disjunction extends Expr {
    Expr c1, c2;

    Disjunction(Expr c1, Expr c2) {
        this.c1 = c1;
        this.c2 = c2;
    }

    public Boolean eval(Environment env) {
        return c1.eval(env) || c2.eval(env);
    }
}

class Variable extends Expr {
    String varname;
    String string = "";
    List<Boolean> bList;

    public Variable(String varname, List<Boolean> bList) {
        this.varname = varname;
        this.bList = bList;
    }

    public Variable(String varname, String string) {
        this.varname = varname;
        this.string = string;
    }

    // Get the current cycle bit from varname
    public Boolean eval(Environment env) {
        // If the list hasn't been assigned to this variable
        // if (this.string.equals(""))
        //     return null;

        // env.setVariable(varname, this.bList);
        return env.getVariable(this.varname).get(Prog.cycle);
    }
}
