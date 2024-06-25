/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lexer;

import java.io.IOException;
import java.util.Hashtable;
import symbol.Type;

/**
 *
 * @author mark
 */
public class Lexer {

    private Hashtable<String, Keyword> words = new Hashtable<>();
    
    private char peek = ' ';
    
    public static int line = 1;
   

    private void reserve(Keyword kword) {
        words.put(kword.lexeme, kword);
    }
    

    private void reserveKeywords() {

        reserve(Keyword.FALSE);
        reserve(Keyword.TRUE);

        reserve(Keyword.DO);
        reserve(Keyword.WHILE);
        reserve(Keyword.IF);
        reserve(Keyword.ELSE);
        reserve(Keyword.BREAK);
        //for , switch
        
        reserve(Type.BOOLEAN);
        reserve(Type.CHAR);
        reserve(Type.FLOAT);
        reserve(Type.INT);
        
        
        
    }

    public Lexer() {
        reserveKeywords();
    }

    

    private void read() throws IOException {
        peek = (char) System.in.read();
    }

    
    private boolean read(char c) throws IOException {
        read();
        if (peek != c) {
            return false;
        }
        peek = ' ';
        return true;
    }

    

  
    
    public Token scan() throws IOException {
        for (;; read()) {
            if (peek == ' ' || peek == '\t') {
                //continue;
            } else if (peek == '\n') {
                line = line + 1;
            } else {
                break;
            }
        }

        switch (peek) {
            case '&':
                if (read('&')) {
                    return Keyword.AND;
                } else {
                    return new Token('&');
                }
            case '|':
                if (read('|')) {
                    return Keyword.OR;
                } else {
                    return new Token('|');
                }
            case '>':
                if (read('=')) {
                    return Keyword.G_EQUAL;
                } else {
                    return new Token('>');
                }
            case '<':
                if (read('=')) {
                    return Keyword.L_EQUAL;
                } else {
                    return new Token('<');
                }
            case '=':
                if (read('=')) {
                    return Keyword.EQUAL;
                } else {
                    return new Token('=');
                }
            case '!':
                if (read('=')) {
                    return Keyword.N_EQUAL;
                } else {
                    return new Token('!');
                }
        }

        if (Character.isDigit(peek)) {
            int v = 0;
            do {
                v = 10 * v + Character.digit(peek, 10);
                read();
            } while (Character.isDigit(peek));

            if (peek != '.') {
                return new Num(v);
            }
            float x = v, d = 10;
            for (;;) {
                read();
                if (!Character.isDigit(peek)) {
                    break;
                }
                x = x + Character.digit(peek, 10) / d;
                d = d * 10;
            }
            return new Real(x);
        }

        if (Character.isLetter((int) peek)) {
            StringBuilder b = new StringBuilder();
            do {
               b.append(peek);
               read();
            } while (Character.isLetterOrDigit((int) peek));
            String s = b.toString();
            Keyword w = (Keyword) words.get(s);
            if (w != null) {
                return w;
            }
            w = new Keyword(s, Tag.ID);
            words.put(s, w);
            return  new Keyword(s, Tag.ID);
        }

        Token t = new Token(peek);
        peek = ' ';
        return t;
    }

}
