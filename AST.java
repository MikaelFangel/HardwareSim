import org.antlr.v4.runtime.tree.ParseTreeVisitor;
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
    List<LatchDec> latches;
    Update update;
    Simulate simulate;

    public Prog(Hardware hardware, Input input, Output output, List<LatchDec> latches, Update update, Simulate simulate) {
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
        output.eval(env);
        // latches.eval(env);
        update.eval(env);
        simulate.eval(env);
    }
}

class Hardware extends AST {
    Variable var;

    public Hardware(Variable var) {
        this.var = var;
    }

    public void eval(Environment env) {
        var.eval(env);
    }
}

//-----------------------------------------------------------------------------------
// Input and Output
//-----------------------------------------------------------------------------------
class Input extends AST {
    Identifiers i;

    Input(Identifiers i) {
        this.i = i;
    }

    public void eval(Environment env) {
        i.eval(env);
    }
}

class Output extends AST {
    Identifiers i;

    Output(Identifiers i) {
        this.i = i;
    }

    public void eval(Environment env) {
        i.eval(env);
    }
}

abstract class Identifiers extends AST {
    abstract public void eval(Environment env);
}

class MultiId extends Identifiers {
    Variable id1, id2;

    public MultiId(Variable id1, Variable id2) {
        this.id1 = id1;
        this.id2 = id2;
    }

    public void eval(Environment env) {
        id1.eval(env);
        id2.eval(env);
    }
}

class SingleId extends Identifiers {
    Variable id1;

    public SingleId(Variable id1) {
        this.id1 = id1;
    }

    public void eval(Environment env) {
        id1.eval(env);
    }
}

//-----------------------------------------------------------------------------------
// Simulate
//-----------------------------------------------------------------------------------
class Simulate extends AST {
    SimIns s;

    public Simulate(SimIns s) {
        this.s = s;
    }

    public void eval(Environment env) {
        s.eval(env);
    }
}

abstract class SimIns extends AST {
    abstract public void eval(Environment env);
}

class MultiSim extends SimIns {
    SimIn s1, s2;

    public MultiSim(SimIn s1, SimIn s2) {
        this.s1 = s1;
        this.s2 = s2;
    }

    public void eval(Environment env) {
        s1.eval(env);
        s2.eval(env);
    }
}

class SingleSim extends SimIns {
    SimIn s1;

    public SingleSim(SimIn s1) {
        this.s1 = s1;
    }

    public void eval(Environment env) {
        s1.eval(env);
    }
}

class SimIn extends AST {
    Variable var;
    Binaries binaries;

    public SimIn(Variable var, Binaries binaries) {
        this.var = var;
        this.binaries = binaries;
    }

    public void eval(Environment env) {
        var.eval(env);
        binaries.eval(env);
    }
}

abstract class Binaries extends AST {
    abstract public void eval(Environment env);
}

class MultiBin extends Identifiers {
    Binary b1, b2;

    public MultiBin(Binary b1, Binary b2) {
        this.b1 = b1;
        this.b2 = b2;
    }

    public void eval(Environment env) {
        b1.eval(env);
        b2.eval(env);
    }
}

class SingleBin extends Identifiers {
    Binary b1;

    public SingleBin(Binary b1) {
        this.b1 = b1;
    }

    public void eval(Environment env) {
        b1.eval(env);
    }
}

//-----------------------------------------------------------------------------------
// Latches
//-----------------------------------------------------------------------------------
// abstract class Latches extends AST {
//     abstract public void eval(Environment env);
// }

class Latches extends AST {
    Latch l;
    Latches ls;

    public Latches(Latch l, Latches ls) {
        this.l = l;
        this.ls = ls;
    }

    public void eval(Environment env) {
        l.eval(env);
        ls.eval(env);
    }
}

// class MultiLatch extends Latches {
//     LatchDec l1, l2;

//     MultiLatch(LatchDec l1, LatchDec l2) {
//         this.l1 = l1;
//         this.l2 = l2;
//     }

//     public void eval(Environment env) {
//         l1.eval(env);
//         l2.eval(env);
//     }
// }

// class SingleLatch extends Latches {
//     LatchDec l1;

//     SingleLatch(LatchDec l1) {
//         this.l1 = l1;
//     }

//     public void eval(Environment env) {
//         l1.eval(env);
//     }
// }

class Latch extends AST {
    LatchDec l;

    Latch(LatchDec l) {
        this.l = l;
    }

    public void eval(Environment env) {
        l.eval(env);
    }
}

class LatchDec extends AST {
    Variable input;
    Variable output;

    public LatchDec(Variable input, Variable output) {
        this.input = input;
        this.output = output;
    }
    
    public void eval(Environment env) {
        env.setVariable(input.varName, input.eval(env));
        env.setVariable(output.varName, output.eval(env));
    }

    public void initialize(Environment env) {
        env.setVariable(output.varName, false);
    }

    public void nextCycle(Environment env) {
        env.setVariable(input.varName, output.eval(env));
    }
}


//-----------------------------------------------------------------------------------
// Updates
//-----------------------------------------------------------------------------------
class Update extends AST {
    UpdateDecs u;

    public Update(UpdateDecs u) {
        this.u = u;
    }

    public void eval(Environment env) {
        u.eval(env);
    }
}

abstract class UpdateDecs extends AST {
    abstract public void eval(Environment env);
}

class MultiUpdate extends UpdateDecs {
    UpdateDec u1, u2;

    public MultiUpdate(UpdateDec u1, UpdateDec u2) {
        this.u1 = u1;
        this.u2 = u2;
    }

    public void eval(Environment env) {
        u1.eval(env);
        u2.eval(env);
    }
}

class SingleUpdate extends UpdateDecs {
    UpdateDec u1;

    public SingleUpdate(UpdateDec u1) {
        this.u1 = u1;
    }

    public void eval(Environment env) {
        u1.eval(env);
    }
}

class UpdateDec extends AST {
   Variable var;
   Expr e;

   public UpdateDec(Variable var, Expr e) {
       this.var = var;
       this.e = e;
    }

    // Adds to variable to Environment
   public void eval(Environment env) {
       env.setVariable(var.varName, e.eval(env));
    }
}

//-----------------------------------------------------------------------------------
// Expressions
//-----------------------------------------------------------------------------------
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


// Leaf of a tree
class Variable extends Expr {
    public String varName;

    Variable(String varName) {
        this.varName = varName;
    }

    public Boolean eval(Environment env) {
        return env.getVariable(varName);
    }
}

//-----------------------------------------------------------------------------------
// Binary
//-----------------------------------------------------------------------------------
class Binary extends AST {
    public Boolean binary;

    Binary(Boolean binary) {
        this.binary = binary;
    }

    public Boolean eval(Environment env) {
        return binary;
    }
}

