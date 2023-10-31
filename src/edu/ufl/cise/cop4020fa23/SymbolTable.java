package edu.ufl.cise.cop4020fa23;

import edu.ufl.cise.cop4020fa23.ast.NameDef;
import edu.ufl.cise.cop4020fa23.exceptions.TypeCheckException;

import java.util.HashMap;
import java.util.Stack;

public class SymbolTable {
    int current_num;
    int next_num;
    Stack<Integer> scope_stack;
    HashMap<String, Entry> symbol_table;

    class Entry {
        NameDef nameDef;
        int scopeID;
        Entry link = null;

        Entry(NameDef nd, int id, Entry e) {
            nameDef = nd;
            scopeID = id;
            link = e;
        }
    }

    public SymbolTable() {
         current_num = 0;
         next_num = 1;
         scope_stack = new Stack<Integer>();
         symbol_table = new HashMap<String, Entry>();
    }
    void enterScope() {
        current_num = next_num++;
        scope_stack.push(current_num);

    }
    void leaveScope() {
        current_num = scope_stack.pop();
    }
    void insert(NameDef name) throws TypeCheckException {
        if (symbol_table.containsKey(name.getName())) {
            Entry currE = symbol_table.get(name);
            while (currE != null) {
                if (currE.scopeID == current_num) {
                    throw new TypeCheckException(name.getName() + " is already defined in scope");
                }
            }
            symbol_table.get(name.getName()).link = symbol_table.get(name.getName());
            symbol_table.get(name.getName()).nameDef = name;
            symbol_table.get(name.getName()).scopeID = current_num;
        }
        else {

            symbol_table.put(name.getName(), new Entry(name, current_num, null));
        }
    }
    NameDef lookup(String name) throws TypeCheckException {
        if (symbol_table.containsKey(name)) {
            Entry currE = symbol_table.get(name);
            while (currE != null) {
                if (currE.scopeID <= current_num) { //FIXME: Not right
                    return currE.nameDef;
                }
                else {
                    currE = currE.link;
                }
            }
        }
        return null;

    }
}
