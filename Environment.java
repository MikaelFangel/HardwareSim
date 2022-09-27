import java.util.HashMap;
import java.util.Map.Entry;

class Environment {
    private HashMap<String,Boolean> variableValues = new HashMap<String,Boolean>();

    public void setVariable(String name, Boolean value) {
        variableValues.put(name, value);
    }

    public Boolean getVariable(String name){
        Boolean value = variableValues.get(name); 
        if (value == null) { System.err.println("Variable not defined: "+name); System.exit(1); }
        return value;
    }

    public String toString() {
        String table = "";
        for (Entry<String,Boolean> entry : variableValues.entrySet()) {
            table += entry.getKey() + "\t-> " + entry.getValue() + "\n";
        }
        return table;
    }   
}
