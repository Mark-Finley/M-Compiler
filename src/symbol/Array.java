/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package symbol;

import lexer.Tag;



/**
 *
 * @author mark
 */
public class Array extends Type {

    public final Type of;
    public final int size;

    public Array(int size, Type of) {
        super("[]", Tag.ARRAY_TYPE, size * of.width);
        this.of = of;
        this.size = size;

    }

}
