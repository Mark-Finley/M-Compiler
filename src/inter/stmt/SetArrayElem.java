/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package inter.stmt;

import inter.expr.Access;
import inter.expr.Expr;
import inter.expr.Id;
import symbol.Array;
import symbol.Type;

/**
 *
 * @author mark
 */
public class SetArrayElem extends Stmt {

    public Id array;
    public Expr index;
    public Expr expr;

    public SetArrayElem(Access x, Expr y) {
        array = x.arrayid;
        index = x.index;
        expr = y;
        if (check(x.type, expr.type) == null) {
            error("type error");
        }
    }

    private Type check(Type p1, Type p2) {
        if (p1 instanceof Array || p2 instanceof Array) {
            return null;
        } else if (p1 == p2) {
            return p2;
        } else if (Type.isNumeric(p1) && Type.isNumeric(p2)) {
            return p2;
        } else {
            return null;
        }
    }

}
