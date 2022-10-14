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
        simulate.eval(env);
        for (Latch latch : latches)
            latch.eval(env);
        output.eval(env);
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
            for (Latch latch : latches)
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
        List<String> prevName = new ArrayList<>();
        for (Variable output : outputs) {
            if (prevName.contains(output.varName))
                System.out.println("Warning: " + output.varName + " is written more than once in output.");
            else {
                prevName.add(output.varName);
                env.setVariable(output.varName, new ArrayList<>());
                env.setOutput(output.varName);

            }
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
        env.setVariable(this.input.varName, new ArrayList<>());
        env.setVariable(this.output.varName, new ArrayList<>());
    }

    // Initialize the output bitstring with 0
    public void initialize(Environment env) {
        env.getVariable(output.varName).add(false);
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
        List<Boolean> inputBinaries = env.getVariable(input.varName);
        Boolean currentCycleInput = inputBinaries.get(Prog.cycle - 1);

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
        if (env.getVariable(this.varname) == null)
            env.setVariable(this.varname, new ArrayList<>());
        // Add boolean value to every variable in update for current cycle
        for (Expr expr : this.exprList) {
            Boolean b = expr.eval(env);
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
        // Error if does declared in input
        if (env.getVariable(varName) == null) {
            System.err.println("Error: " + this.varName + " is not an input.");
            System.exit(1);
        }
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
        // We evaluate before return, so right side are evaluated even if left = 0
        // (always false)
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
        // We evaluate before return, so right side are evaluated even if left = 1
        // (always true)
        Boolean left = this.c1.eval(env);
        Boolean right = this.c2.eval(env);
        return left || right;
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
        if (env.getVariable(this.varName) == null) {
            System.err.println("Error: " + this.varName
                    + " does not exist in the environment yet. You cannot evaluate on a variable that has not been declared yet.");
            System.exit(1);
        }

        return env.getVariable(this.varName).get(Prog.cycle);
    }
}
