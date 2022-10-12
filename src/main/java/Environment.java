import java.util.HashMap;
import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.List;

class Environment {
    private final HashMap<String, List<Boolean>> variableValues = new HashMap<>();
    private final List<String> outputNames = new ArrayList<>();

    public void setVariable(String name, List<Boolean> value) {
        variableValues.put(name, value);
    }

    public void setOutput(String varName) {
        outputNames.add(varName);
    }

    /**
     * Only call this method after all cycles are done
     * @return traces needed to test for e.g. 
     */
    public List<String> getTraces() {
        List<String> keys = new ArrayList<>();
        for (String string : variableValues.keySet()) {
            if (variableValues.get(string).size() > 0) {
                keys.add(string);
            }
        }
        return keys;
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
