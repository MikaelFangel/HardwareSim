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
        simulate.eval(env);
        for (Latch l : latches)
            l.eval(env);
        output.eval(env);
    }

    /**
     * This method is called by main
     * (Whether inputsize is less than 1 is checked by the hwsim grammar)
     */
    public void runSimulator(Environment env) {
        // Calculates number of cycles
        int numOfCycles = 0;
        for (var input : simulate.simIn) {
            int inputSize = input.inputSignal.size();

            if (numOfCycles == 0) {
                numOfCycles = inputSize;
                continue;
            }

            if (inputSize != numOfCycles) {
                System.err.print("Error: Length of all inputs should be the same\n");
                System.exit(1);
            }
        }

        // Run next cycle until all cycles have been run
        while (cycle < numOfCycles) {
            // Start next cycle by executing all latches
            for (var l : latches)
                l.nextCycle(env);

            // Execute all update statements
            update.eval(env);

            cycle++;
        }

        // Check for cyclic updates
        for (var string : env.getTraces()) {
            Boolean prevBool = null;
            Boolean isCyclic = true;
            for (Boolean b : env.getVariable(string)) {
                // First case
                if (prevBool == null) {
                    prevBool = b;
                    continue;
                }
                // If not cyclic
                if (prevBool == b) {
                    isCyclic = false;
                    break;
                }
                prevBool = b;
            }
            if (isCyclic) {
                System.out.println("Warning: " + string + " is cyclic.\n");
                System.out.println();
            }
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
    Variable input; // Left side of arrow
    Variable output; // right side of arrow

    public Latch(Variable input, Variable output) {
        this.input = input;
        this.output = output;
    }

    public void eval(Environment env) {
        env.setVariable(this.input.varname, new ArrayList<>());
        env.setVariable(this.output.varname, new ArrayList<>());
    }

    // Initialize the output bitstring with 0
    public void initialize(Environment env) {
        env.getVariable(output.varname).add(false);
    }

    /**
     * Executes all latches. Set the output value to the current value of the input
     */
    public void nextCycle(Environment env) {
        // In first cycle, latches should initialize to 0
        if (Prog.cycle == 0) {
            initialize(env);
            return;
        }

        // Input to the left of arrow in g4 file
        List<Boolean> inputBinaries = env.getVariable(input.varname);
        Boolean currentCycleInput = inputBinaries.get(Prog.cycle - 1);

        // Stores the current value of an incomming signal and outputs
        // it on an outcomming value at each cycle
        env.getVariable(output.varname).add(currentCycleInput);
    }
}

class Update extends AST {
    List<UpdateDec> updates;

    public Update(List<UpdateDec> updates) {
        this.updates = updates;
    }

    // eval all update declarations
    public void eval(Environment env) {
        for (var v : updates)
            v.eval(env);
    }
}

class UpdateDec extends AST {
    String varname;
    List<Expr> exprList;

    public UpdateDec(String varname, List<Expr> exprList) {
        this.varname = varname;
        this.exprList = exprList;
    }

    public void eval(Environment env) {
        if (env.getVariable(this.varname) == null)
            env.setVariable(this.varname, new ArrayList<>());
        // Add boolean value to every variable in update for current cycle
        for (var expr : this.exprList) {
            Boolean b = expr.eval(env);
            env.getVariable(this.varname).add(b);
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
    public String varname;
    public List<Boolean> inputSignal;

    public SimIn(String varname, List<Boolean> inputSignal) {
        this.varname = varname;
        this.inputSignal = inputSignal;
    }

    // Store the input bitstring in hashmap
    public void eval(Environment env) {
        // Error if does declared in input
        if (env.getVariable(varname) == null) {
            System.err.println("Error: " + this.varname + " is not an input.");
            System.exit(1);
        }
        env.setVariable(varname, inputSignal);
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
        return !this.c1.eval(env);
    }
}

class Conjunction extends Expr {
    Expr c1, c2;

    Conjunction(Expr c1, Expr c2) {
        this.c1 = c1;
        this.c2 = c2;
    }

    public Boolean eval(Environment env) {
        // We evaluate before return, so right side are evaluated even if left = 0 (always false)
        Boolean left = this.c1.eval(env);
        Boolean right = this.c2.eval(env);
        return left && right;
    }
}

class Disjunction extends Expr {
    Expr c1, c2;

    Disjunction(Expr c1, Expr c2) {
        this.c1 = c1;
        this.c2 = c2;
    }

    public Boolean eval(Environment env) {
        // We evaluate before return, so right side are evaluated even if left = 1 (always true)
        Boolean left = this.c1.eval(env);
        Boolean right = this.c2.eval(env);
        return left || right;
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

    /**
     * Get the current cycle bit from varname
     */
    public Boolean eval(Environment env) {
        if (env.getVariable(this.varname) == null) {
            System.err.println("Error: " + this.varname + " does not exist in the environment yet. You cannot evaluate on a variable that has not been declared yet.");
            System.exit(1);
        }

        return env.getVariable(this.varname).get(Prog.cycle);
    }
}
