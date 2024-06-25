/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lexer;

/**
 *
 * @author mark
 */
public class Keyword extends Token {

    public final String lexeme;

    public Keyword(String lexeme, int tag) {
        super(tag);
        this.lexeme = lexeme;
    }

    public static final Keyword AND = new Keyword("&&", Tag.AND),
            OR = new Keyword("||", Tag.OR),
            EQUAL = new Keyword("==", Tag.EQ),
            N_EQUAL = new Keyword("!=", Tag.NE),
            L_EQUAL = new Keyword("<=", Tag.LE),
            G_EQUAL = new Keyword(">=", Tag.GE),
            TRUE = new Keyword("true", Tag.TRUE),
            FALSE = new Keyword("false", Tag.FALSE),
            IF = new Keyword("if", Tag.IF),
            ELSE = new Keyword("else", Tag.ELSE),
            BREAK = new Keyword("break", Tag.BREAK),
            DO = new Keyword("do", Tag.DO),
            WHILE = new Keyword("while", Tag.WHILE),
            MINUS = new Keyword("minus", Tag.MINUS);//negative

}
