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
        output.eval(env);
        for(var v : latches)
            v.eval(env);
        update.eval(env);
        simulate.eval(env);
    }
}

class Hardware extends AST {
    Variable vari;

    public Hardware(Variable vari) {
        this.vari = vari;
    }

    public void eval(Environment env) {
        //vari.eval(env);
        env.setVariable(vari.varname, new ArrayList<Boolean>());
    }
}

class Input extends AST {
    List<Variable> li;

    Input(List<Variable> li) {
        this.li = li;
    }

    public void eval(Environment env) {
        for(var v : li)
            //v.eval(env);
            env.setVariable(v.varname, new ArrayList<Boolean>());
    }
}

class Output extends AST {
    List<Variable> outputs;

    Output(List<Variable> outputs) {
        this.outputs = outputs;
    }

    public void eval(Environment env) {
        for(var v : outputs)
            //v.eval(env);
            env.setVariable(v.varname, new ArrayList<Boolean>());
    }
}

// Maybe not needed
// class Latches extends AST {
//     Latch l;
//     Latches ls;
// 
//     public Latches(Latch l, Latches ls) {
//         this.l = l;
//         this.ls = ls;
//     }
// 
//     public void eval(Environment env) {
//         l.eval(env);
//         ls.eval(env);
//     }
// }

class Latch extends AST {
    Variable input;
    Variable output;

    public Latch(Variable input, Variable output) {
        this.input = input;
        this.output = output;
    }

    public void eval(Environment env) {
        env.setVariable(input.varname, new ArrayList<Boolean>());
        env.setVariable(output.varname, new ArrayList<Boolean>());
    }

    public void initialize(Environment env) {
        env.setVariable(output.varname, new ArrayList<Boolean>());
    }

    public void nextCycle(Environment env) {
        env.setVariable(input.varname, output.eval(env));
    }
}

class Update extends AST {
    List<UpdateDec> u;

    public Update(List<UpdateDec> u) {
        this.u = u;
    }
    
    public void eval(Environment env) {
        for(var v : u)
            v.eval(env);
    }
}

class UpdateDec extends AST {
    Variable vari;
    List<Expr> e;

    public UpdateDec(Variable vari, List<Expr> e) {
        this.vari = vari;
        this.e = e;
    }

    // Adds to variable to Environment
    public void eval(Environment env) {
        for(var v : e) {
            vari.binaries.add(v.eval(env));
            env.setVariable(vari.varname, vari.binaries);
        }
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
    Variable vari;

    public SimIn(Variable vari) {
        this.vari = vari;
    }

    public void eval(Environment env) {
        //vari.eval(env);
        env.setVariable(vari.varname, vari.binaries);
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

class Variable extends AST {
    public String varname;
    public String string;
    public List<Boolean> binaries;

    Variable(String varname, List<Boolean> binaries) {
        this.varname = varname;
        this.binaries = binaries;
    }

    Variable(String varname, String string) {
        this.varname = varname;
        this.string = string; 
    }

    public List<Boolean> eval(Environment env) {
        return env.getVariable(varname);
    }
}
