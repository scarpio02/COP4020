package edu.ufl.cise.cop4020fa23;

import edu.ufl.cise.cop4020fa23.ast.NameDef;
import edu.ufl.cise.cop4020fa23.exceptions.TypeCheckException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

public class SymbolTable {
    int current_num;
    int next_num;
    Stack<Integer> scope_stack;
    HashMap<String, ArrayList<Entry>> symbol_table;

    class Entry {
        NameDef nameDef;
        int scopeID;

        Entry(NameDef nd, int id) {
            nameDef = nd;
            scopeID = id;
        }
    }

    public SymbolTable() {
         current_num = 0;
         next_num = 1;
         scope_stack = new Stack<Integer>();
         symbol_table = new HashMap<String, ArrayList<Entry>>();
    }
    void enterScope() {
        current_num = next_num++;
        scope_stack.push(current_num);

    }
    void leaveScope() {
        current_num = scope_stack.pop();
    }

    //FIXME: Infinite looping in symbol table. MUST FIX!!!!!!!!!!
    void insert(NameDef name) throws TypeCheckException {
        if (symbol_table.containsKey(name.getName())) {
            for (int i = 0; i < symbol_table.get(name.getName()).size(); i++)
            {
                if (symbol_table.get(name.getName()).get(i).scopeID == current_num)
                {
                    throw new TypeCheckException(name.getName() + " is already defined in scope");
                }
            }
            symbol_table.get(name.getName()).add(new Entry(name, current_num));
        }
        else {
            symbol_table.put(name.getName(), new ArrayList<Entry>());
            symbol_table.get(name.getName()).add(new Entry(name, current_num));
        }
    }
    NameDef lookup(String name) throws TypeCheckException {
        if (symbol_table.containsKey(name)) {
            for (int i = symbol_table.get(name).size() - 1; i >= 0; i--)
            {
                if (scope_stack.search(symbol_table.get(name).get(i).scopeID) != -1)
                {
                    return symbol_table.get(name).get(i).nameDef;
                }
            }
        }
        return null;

    }
}
