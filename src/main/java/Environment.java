import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

class Environment {
    private final HashMap<String, Variable> variableValues = new HashMap<>();
    private final List<String> outputNames = new ArrayList<>();

    public void setVariable(String name, Variable variable) {
        variableValues.put(name, variable);
    }

    public void setOutput(String varName) {
        outputNames.add(varName);
    }

    /**
     * Only call this method after all cycles are done
     *
     * @return traces needed to test for e.g.
     */
    public List<String> getTraces() {
        List<String> keys = new ArrayList<>();
        for (String string : variableValues.keySet()) {
            if (variableValues.get(string).valueList != null &&
                    variableValues.get(string).valueList.size() > 0) {
                keys.add(string);
            }
        }
        return keys;
    }

    /**
     * If no mapping for the key exists, return null
     */
    public Variable getVariable(String key) {
        return variableValues.get(key);
    }

    public String toString() {
        StringBuilder table = new StringBuilder();
        for (String s : outputNames) {
            Variable entry = variableValues.get(s);

            if (entry.valueList != null) {
                for (Value b : entry.valueList) {
                    table.append(b);
                }
            } else {
                table.append(entry.value);
            }

            table.append("\t").append(s).append("\n");
        }
        return table.toString();
    }
}
