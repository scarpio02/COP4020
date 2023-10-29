package edu.ufl.cise.cop4020fa23;

import java.util.HashMap;
import java.util.Stack;

public class SymbolTable {
    /*
    int  current_num;//serial number of current scope
    int  next_num;   //next serial number to assign

    void enterScope()
    {   current_num = next_num++;
        scope_stack.push(current_num);
    }

    void closeScope()
    {  current_num = scope_stack.pop();
    }

    void lookup(String name)
    {   look up entry with key “name” in symbol table;
        scan chain—entries whose serial number is in the scope stack are visible.
       Return entry with serial number closest to the top of the scopestack.
        If none, this is an error—the name is not bound in the current scope.
    }
     */

    int current_num;
    int next_num;
    Stack<Integer> scope_stack;

    HashMap<String, Integer> symbolTable;

    public SymbolTable() {
         current_num = 0;
         next_num = 1;
    }
    void enterScope() {
        current_num = next_num++;
        scope_stack.push(current_num);

    }
    void closeScope() {
        current_num = scope_stack.pop();
    }
    void lookup(String name) {
        // FIXME: This is not fully implemented
        symbolTable.get(name);
    }
}
