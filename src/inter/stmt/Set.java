/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package inter.stmt;

import inter.expr.Expr;
import inter.expr.Id;
import symbol.Type;

/**
 *
 * @author mark
 */
//Loc
public class Set extends Stmt {
    
    Id id;
    Expr expr;
    
    public Set(Id id, Expr expr) {
        this.id = id;
        this.expr = expr;
        if (check(id.type, expr.type) == null) {
            error("type error ");
        }
    }
    
    public Type check(Type p1, Type p2) {
        if (Type.isNumeric(p1) && Type.isNumeric(p2)) {
            return p2;
        } else if (p1 == Type.BOOLEAN && p2 == Type.BOOLEAN) {
            return p2;
        } else {
            return null;
        }
    }
    
}
