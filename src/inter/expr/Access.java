/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package inter.expr;

import lexer.Keyword;
import symbol.Type;
import lexer.Tag;

/**
 *
 * @author mark
 */
public class Access extends Expr {
    
    public Id arrayid;
    public Expr index;
    
    public Access(Id a, Expr i, Type p) {
        super(new Keyword("[]", Tag.ARRAY_TYPE), p);
        arrayid = a;        
        index = i;
        if (index.type != Type.INT) {
            error("array index expected to be int");
        }
    }
    
}
