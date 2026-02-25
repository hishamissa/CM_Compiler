/*
  cminus.flex
  Scanner for the C- language
*/
package scanner;
   
import java_cup.runtime.*;
      
%%
   
/* JFlex options and setup */
   
%class Lexer

%eofval{
  return new Symbol(sym.EOF);
%eofval};

// Track line and column numbers for error reporting
%line
%column
    
// CUP compatibility mode
%cup
   
%{   
    // Create a token with just a type (no value)
    private Symbol symbol(int type) {
        return new Symbol(type, yyline, yycolumn);
    }
    
    // Create a token with a type and value (for IDs and numbers)
    private Symbol symbol(int type, Object value) {
        return new Symbol(type, yyline, yycolumn, value);
    }
%}
   

/* Regular expression patterns */
   
LineTerminator = \r|\n|\r\n
WhiteSpace     = {LineTerminator} | [ \t\f]
   
digit = [0-9]
number = {digit}+
   
// IDs can start with letter or underscore
letter = [a-zA-Z_]
identifier = {letter}({letter}|{digit})*

// C-style comments (non-nested)
Comment = "/*" [^*] ~"*/" | "/*" "*"+ "/"
   
%%
   
/* Lexical rules - what to do when we match each pattern */

// Keywords (check these before general identifiers)
"bool"             { return symbol(sym.BOOL); }
"else"             { return symbol(sym.ELSE); }
"if"               { return symbol(sym.IF); }
"int"              { return symbol(sym.INT); }
"return"           { return symbol(sym.RETURN); }
"void"             { return symbol(sym.VOID); }
"while"            { return symbol(sym.WHILE); }

// Boolean literals
"true"             { return symbol(sym.TRUE); }
"false"            { return symbol(sym.FALSE); }

// Multi-character operators (check before single-char versions)
"<="               { return symbol(sym.LE); }
">="               { return symbol(sym.GE); }
"=="               { return symbol(sym.EQEQ); }
"!="               { return symbol(sym.NE); }
"||"               { return symbol(sym.OR); }
"&&"               { return symbol(sym.AND); }

// Single-character operators
"+"                { return symbol(sym.PLUS); }
"-"                { return symbol(sym.MINUS); }
"*"                { return symbol(sym.TIMES); }
"/"                { return symbol(sym.OVER); }
"<"                { return symbol(sym.LT); }
">"                { return symbol(sym.GT); }
"="                { return symbol(sym.ASSIGN); }
"~"                { return symbol(sym.NOT); }

// Punctuation
";"                { return symbol(sym.SEMI); }
","                { return symbol(sym.COMMA); }
"("                { return symbol(sym.LPAREN); }
")"                { return symbol(sym.RPAREN); }
"["                { return symbol(sym.LBRACKET); }
"]"                { return symbol(sym.RBRACKET); }
"{"                { return symbol(sym.LBRACE); }
"}"                { return symbol(sym.RBRACE); }

// Numbers and identifiers
{number}           { return symbol(sym.NUM, yytext()); }
{identifier}       { return symbol(sym.ID, yytext()); }

// Skip whitespace and comments
{WhiteSpace}+      { /* just skip it */ }   
{Comment}          { /* ignore comments */ }

// Anything else is an error
.                  { return symbol(sym.ERROR); }
