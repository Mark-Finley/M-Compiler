/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package parser;

import inter.expr.Access;
import inter.expr.Constant;
import inter.expr.Expr;
import inter.expr.Id;
import inter.expr.Rel;
import inter.expr.arith.Arith;
import inter.expr.arith.Unary;
import inter.expr.logic.Not;
import inter.stmt.Break;
import inter.stmt.DoWhile;
import inter.stmt.If;
import inter.stmt.IfElse;
import inter.stmt.Set;
import inter.stmt.SetArrayElem;
import inter.stmt.Stmt;
import inter.stmt.StmtSeq;
import inter.stmt.While;
import java.io.IOException;
import lexer.Keyword;
import lexer.Lexer;
import lexer.Num;
import lexer.Tag;
import lexer.Token;
import symbol.Array;
import symbol.Env;
import symbol.Type;

/**
 *
 * @author mark
 */
public class Parser {

    private final Lexer lexer;
    private Token look;

    public Parser(Lexer lexer) throws IOException {
        this.lexer = lexer;
        move();
    }

    private void move() throws IOException {
        look = lexer.scan();
    }

    private void match(int t) throws IOException {
        if (look.tag == t) {
            move();
        } else {
            error("Syntax Error");
        }
    }

    private void error(String s) {
        throw new Error("near line " + lexer.line + " : " + s);
    }

    public void start() throws IOException {
        program();
    }

    private void program() throws IOException { // PROG → BLOCK
        block();
    }

    private Env top = null; // top symbol table

    private Stmt block() throws IOException { // BLOCK → { DECLS STMTS }
        match('{');
        Env savedEnv = top;
        top = new Env(top);
        decls();
        Stmt s = stmts();
        match('}');
        top = savedEnv;
        return s;
    }

    private int used = 0;

    //==========================================================================
    // what about int a,b, .... ca,d2 ;
    private void decls() throws IOException { // DECLS→  DECLS DECL | TYPE id; | ε 
        while (look.tag == Tag.BASIC_TYPE) {
            Type p = type();
            Token tok = look;
            match(Tag.ID);
            match(';');
            if (top.getLocal(((Keyword) tok).lexeme) == null) {
                Id id = new Id((Keyword) tok, p, used);
                top.push(((Keyword) tok).lexeme, id);
                used = used + p.width;
            } else {
                error("duplicate declaration " + ((Keyword) tok).lexeme);
            }

        }
    }

    private Type type() throws IOException { //TYPE → TYPE [num] | data_type
        Type p = (Type) look; //expect 1ook.tag == Tag.DATA_TYPE
        match(Tag.BASIC_TYPE);
        if (look.tag != '[') {
            return p; // T -> basic
        } else {
            return dims(p);  // return array type
        }
    }

    private Type dims(Type p) throws IOException { //TYPE → TYPE [num] 
        match('[');
        Token tok = look;
        match(Tag.NUM);
        match(']');
        if (look.tag == '[') {
            dims(p);
        }
        return new Array(((Num) tok).value, p);
    }
    //=============================================================================  

    private Stmt stmts() throws IOException { //STMTS →  STMTS STMT | ε
        if (look.tag == '}') {
            return Stmt.Null;
        } else {
            return new StmtSeq(stmt(), stmts());
        }
    }

    private Stmt stmt() throws IOException {
        Expr x;
        Stmt s1;
        Stmt s2;
        Stmt savedstmt; // to save outer loop for break
        switch (look.tag) {
            case ';':
                move();
                return Stmt.Null;
            case Tag.IF: //STMT → if ( BOOL ) STMT
                match(Tag.IF);
                match('(');
                x = bool();
                match(')');
                s1 = stmt();
                if (look.tag != Tag.ELSE) {
                    return new If(x, s1);
                }
                match(Tag.ELSE); //if (BOOL) STMT else STMT
                s2 = stmt();
                return new IfElse(x, s1, s2);

            case Tag.WHILE: //STMT → while ( BOOL ) STMT
                While whilenode = new While();
                savedstmt = Stmt.Enclosing;
                Stmt.Enclosing = whilenode; // now there outer/Stmt.Enclosing is While

                match(Tag.WHILE);
                match('(');
                x = bool();
                match(')');
                s1 = stmt(); // s1 can be break 
                whilenode.init(x, s1);
                Stmt.Enclosing = savedstmt; // reset Stmt.Enclosing
                return whilenode;
            case Tag.DO: //STMT → do STMT while ( BOOL ) ;
                DoWhile donode = new DoWhile();
                savedstmt = Stmt.Enclosing;
                Stmt.Enclosing = donode; // now there outer/Stmt.Enclosing is DoWhile

                match(Tag.DO);
                s1 = stmt(); //s1 can be break 
                match(Tag.WHILE);
                match('(');
                x = bool();
                match(')');
                match(';');
                donode.init(x, s1);
                Stmt.Enclosing = savedstmt; // reset Stmt.Enclosing
                return donode;
            case Tag.BREAK: //STMT → break ;
                match(Tag.BREAK);
                match(';');
                return new Break();
            case '{': //STMT → BLOCK
                return block();
            default:  //STMT → LOC = BOOL ;
                return assign();

        }

    }

    //what about int a = .... or float []r = ...
    private Stmt assign() throws IOException { //STMT → LOC = BOOL ; // LOC →  LOC [ BOOL] | id
        Stmt stmt = null;
        Token t = look;
        match(Tag.ID);
        Id id = top.get(((Keyword) t).lexeme);
        if (id == null) {
            error("variable " + ((Keyword) t).lexeme + " undeclared");
        }

        if (look.tag == '=') { //STMT -> id = expr
            move();
            stmt = new Set(id, bool());
        } else { // STMT -> id[bool()] = expr
            Access a = offset(id);
            match('=');
            stmt = new SetArrayElem(a, bool());
        }
        match(';');
        return stmt;
    }

    private Expr bool() throws IOException { //BOOL → BOOL || JOIN | JOIN
        Expr x = join();
        while (look.tag == Tag.OR) {
            Token tok = look;
            move();
            x = new Rel(tok, x, join());
        }
        return x;
    }

    private Expr join() throws IOException { // JOIN →  JOIN && EQUALITY | EQUALITY
        Expr x = equality();
        while (look.tag == Tag.AND) {
            Token tok = look;
            move();
            x = new Rel(tok, x, equality());
        }
        return x;
    }

    private Expr equality() throws IOException { //EQUALITY  →  EQUALITY == REL | EQUALITY != REL | REL
        Expr x = rel();
        while (look.tag == Tag.EQ || look.tag == Tag.NE) {
            move();
            x = new Rel(look, x, rel());
        }
        return x;
    }

    private Expr rel() throws IOException { //REL → EXPR <  EXPR |  EXPR <= EXPR | EXPR >= EXPR | EXPR > EXPR |  EXPR
        Expr x = expr();
        switch (look.tag) {
            case '<':
            case Tag.LE:
            case Tag.GE:
            case '>':
                Token tok = look;
                move();
                return new Rel(tok, x, expr());
            default:
                return x;
        }

    }

    private Expr expr() throws IOException { //EXPR → EXPR + TERM | EXPR - TERM | TERM
        Expr x = term();
        while (look.tag == '+' || look.tag == '-') {
            Token tok = look;
            move();
            x = new Arith(tok, x, term());
        }
        return x;
    }

    private Expr term() throws IOException { //TERM →  TERM * UNARY | TERM / UNARY | UNARY
        Expr x = unary();
        while (look.tag == '*' || look.tag == '/') {
            Token tok = look;
            move();
            x = new Arith(tok, x, unary());

        }
        return x;

    }

    private Expr unary() throws IOException { // UNARY →  !UNARY | -UNARY | FACTOR
        if (look.tag == '-') {
            move();
            return new Unary(Keyword.MINUS, unary());
        } else if (look.tag == '!') {
            Token tok = look;
            move();
            return new Not(tok, unary());

        } else {
            return factor();
        }

    }

    private Expr factor() throws IOException { //factor -> ( BOOL ) | ID[BOOL] |ID | num | real | true | false
        Expr x = null;
        switch (look.tag) {
            case '(':
                move();
                x = bool();
                match(')');
                return x;
            case Tag.NUM:
                x = new Constant(look, Type.INT); // factor.n= new Num(value)
                move();
                return x;
            case Tag.REAL:
                x = new Constant(look, Type.FLOAT); // factor.n= new Real(value)
                move();
                return x;
            case Tag.TRUE: //factor.n= true, factor.type = bool
                x = Constant.True;
                move();
                return x;
            case Tag.FALSE:
                x = Constant.False;
                move();
                return x;
            case Tag.ID:
                Id id = top.get(((Keyword) look).lexeme);
                if (id == null) {
                    error("variable " + ((Keyword) look).lexeme + " undeclared");
                }
                move();
                if (look.tag != '[') {

                } else {
                    return offset(id);  //array
                }
                return id; //factor.n = id, factor.type= id.type
            default:
                error("Syntax error");

        }
        return null;
    }

    private Access offset(Id a) throws IOException { // I -> [E] | [E] array
        Expr index;
        Type type = a.type; //a is array type , type is array
        match('[');
        index = bool();
        match(']');
        type = ((Array) type).of; // type is now type of array 

        while (look.tag == '[') {// multi-dimensional I -> [ E ] 
            match('[');
            index=bool();
            match(']');
            type = ((Array) type).of;
        }
        return new Access(a, index, type);
    }

}
