import java.util.HashMap;
import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.List;

class Environment {
    private HashMap<String, List<Boolean>> variableValues = new HashMap<>();
    private List<String> outputNames = new ArrayList<>();

    public void setVariable(String name, List<Boolean> value) {
        variableValues.put(name, value);
    }

    public void setOutput(String varname) {
        outputNames.add(varname);
    }

    /**
     * If no mapping for the key "name" exists, return null
     */
    public List<Boolean> getVariable(String name) {
        return variableValues.get(name);
    }

    public String toString() {
        StringBuilder table = new StringBuilder();
        for (String s : outputNames) {
            for (Boolean b : variableValues.get(s)) {
                table.append(b ? "1" : "0");
            }
            table.append("\t").append(s).append("\n");
        }
        return table.toString();
    }
}
