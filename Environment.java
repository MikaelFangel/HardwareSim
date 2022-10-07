import java.util.HashMap;
import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.List;

class Environment {
    private HashMap<String, String> variableValues = new HashMap<>();

    public void setVariable(String name, String value) {
        variableValues.put(name, value);
    }

    public String getVariable(String name) {
        String value = variableValues.get(name);
        if (value == null) {
            System.err.println("Variable not defined: " + name);
            System.exit(1);
        }
        return value;
    }

    public String toString() {
        String table = "";
        for (Entry<String, String> entry : variableValues.entrySet()) {
            table += entry.getKey() + "\t-> " + entry.getValue() + "\n";
        }
        return table;
    }
}
