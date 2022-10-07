import java.util.HashMap;
import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.List;

class Environment {
    private HashMap<String, List<Boolean>> variableValues = new HashMap<>();

    public void setVariable(String name, List<Boolean> value) {
        variableValues.put(name, value);
    }

    public List<Boolean> getVariable(String name) {
        List<Boolean> value = variableValues.get(name);
        if (value == null) {
            System.err.println("Variable not defined: " + name);
            System.exit(1);
        }
        return value;
    }

    public String toString() {
        String table = "";
        for (Entry<String, List<Boolean>> entry : variableValues.entrySet()) {
            table += entry.getKey() + "\t-> " + entry.getValue() + "\n";
        }
        return table;
    }
}
