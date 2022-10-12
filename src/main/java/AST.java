import java.util.ArrayList;
import java.util.List;

public abstract class AST {
}

class Prog extends AST {
    Hardware hardware;
    Input input;
    Output output;
    List<Latch> latches;
    Update update;
    Simulate simulate;
    // Global variable for keeping track of the current cycle
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
        for (Latch latch : latches)
            latch.eval(env);
        output.eval(env);
        simulate.eval(env);
    }

    /**
     * This method is called by main
     * (Whether input size is less than 1 is checked by the hwsim grammar)
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
            for (var latch : latches)
                latch.nextCycle(env);

            // Execute all update statements
            update.eval(env);

            cycle++;
        }

        // Check for cyclic updates
        for (var stringOfBinaries : env.getTraces()) {
            Boolean prevBool = null;
            boolean isCyclic = true;
            for (Boolean binary : env.getVariable(stringOfBinaries)) {
                // First case
                if (prevBool == null) {
                    prevBool = binary;
                    continue;
                }
                // If not cyclic
                if (prevBool == binary) {
                    isCyclic = false;
                    break;
                }
                prevBool = binary;
            }
            if (isCyclic) {
                System.out.println("Warning: " + stringOfBinaries + " is cyclic.\n");
                System.out.println();
            }
        }
    }
}

class Hardware extends AST {
    Variable variable;

    public Hardware(Variable variable) {
        this.variable = variable;
    }

    public void eval(Environment env) {
        env.setVariable(variable.varName, new ArrayList<>());
    }
}

class Input extends AST {
    List<Variable> variableList;

    Input(List<Variable> variableList) {
        this.variableList = variableList;
    }

    // Initialize input variables in hashmap
    public void eval(Environment env) {
        for (Variable variable : variableList) {
            env.setVariable(variable.varName, new ArrayList<>());
            env.setOutput(variable.varName);
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
        for (Variable output : outputs) {
            env.setVariable(output.varName, new ArrayList<>());
            env.setOutput(output.varName);
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
        env.setVariable(input.varName, new ArrayList<>());
        env.setVariable(output.varName, new ArrayList<>());
    }

    // Initialize the output bitstring with 0
    public void initialize(Environment env) {
        List<Boolean> outputBinaries = env.getVariable(output.varName);
        if (outputBinaries == null)
            env.setVariable(output.varName, new ArrayList<>());

        env.getVariable(output.varName).add(false);
    }

    // Set the output value to the current value of the input

    /**
     * Executes all latches
     */
    public void nextCycle(Environment env) {
        // In first cycle, latches should initialize to 0
        if (Prog.cycle == 0) {
            initialize(env);
            return;
        }

        // Input to the left of arrow in g4 file
        List<Boolean> inputBinaries = env.getVariable(input.varName);
        Boolean currentCycleInput = inputBinaries != null && inputBinaries.get(Prog.cycle - 1);

        // Output to the right of arrow in g4 file
        if (env.getVariable(output.varName) == null)
            env.setVariable(output.varName, new ArrayList<>());

        // Stores the current value of an incoming signal and outputs
        // it on an out-coming value at each cycle
        env.getVariable(output.varName).add(currentCycleInput);
    }
}

class Update extends AST {
    List<UpdateDec> updateDecList;

    public Update(List<UpdateDec> updateDecList) {
        this.updateDecList = updateDecList;
    }

    // eval all update declarations
    public void eval(Environment env) {
        for (var updateDec : updateDecList)
            updateDec.eval(env);
    }
}

class UpdateDec extends AST {
    String varName;
    List<Expr> exprList;

    public UpdateDec(String varName, List<Expr> exprList) {
        this.varName = varName;
        this.exprList = exprList;
    }

    public void eval(Environment env) {
        // Add boolean value to every variable in update for current cycle
        for (Expr expr : this.exprList) {
            Boolean b = expr.eval(env);

            if (env.getVariable(varName) == null)
                env.setVariable(varName, new ArrayList<>());

            env.getVariable(this.varName).add(b);
        }
    }
}

class Simulate extends AST {
    List<SimIn> simIn;

    public Simulate(List<SimIn> simIn) {
        this.simIn = simIn;
    }

    public void eval(Environment env) {
        for (SimIn in : simIn)
            in.eval(env);
    }
}

class SimIn extends AST {
    public String varName;
    public List<Boolean> inputSignal;

    public SimIn(String varName, List<Boolean> inputSignal) {
        this.varName = varName;
        this.inputSignal = inputSignal;
    }

    // Store the input bit-string in hashmap
    public void eval(Environment env) {
        env.setVariable(varName, inputSignal);
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
    String varName;
    String string = "";
    List<Boolean> bList;

    public Variable(String varName, List<Boolean> bList) {
        this.varName = varName;
        this.bList = bList;
    }

    public Variable(String varName, String string) {
        this.varName = varName;
        this.string = string;
    }

    /**
     * Get the current cycle bit from varName
     */
    public Boolean eval(Environment env) {
        return env.getVariable(this.varName).get(Prog.cycle);
    }
}
