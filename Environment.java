import java.util.HashMap;
import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.List;

class Environment {
    private HashMap<String, String> variableValues = new HashMap<>();
    private List<String> outputNames = new ArrayList<>();

    public void setVariable(String name, String value) {
        variableValues.put(name, value);
    }

    public void setOutput(String varname) {
        outputNames.add(varname);
    }

    public String getVariable(String name) throws Exception {
        String value = variableValues.get(name);
        if (value == null) {
            throw new Exception("Variable not defined" + name);
        }
        return value;
    }

    public String toString() {
        StringBuilder table = new StringBuilder();
        for (String s : outputNames) {
            table.append(variableValues.get(s) + "\t" + s + "\n");
        }

        return table.toString();
    }
}
