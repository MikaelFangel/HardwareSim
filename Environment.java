import java.util.HashMap;
import java.util.Map.Entry;

class Environment {
    private HashMap<String,Integer> variableValues = new HashMap<String,Integer>();

    public void setVariable(String name, Integer value) {
        variableValues.put(name, value);
    }

    public Integer getVariable(String name){
        Integer value = variableValues.get(name); 
        if (value == null) { System.err.println("Variable not defined: "+name); System.exit(1); }
        return value;
    }

    public String toString() {
        String table = "";
        for (Entry<String,Integer> entry : variableValues.entrySet()) {
            table += entry.getKey() + "\t-> " + entry.getValue() + "\n";
        }
        return table;
    }   
}
