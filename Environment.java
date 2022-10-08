import java.util.HashMap;
import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.List;

class Environment {
    private HashMap<String, String> variableValues = new HashMap<>();

    public void setVariable(String name, String value) {
        variableValues.put(name, value);
    }

    public String getVariable(String name) throws Exception {
        String value = variableValues.get(name);
        if (value == null) {
            throw new Exception("Variable not defined" + name);
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
