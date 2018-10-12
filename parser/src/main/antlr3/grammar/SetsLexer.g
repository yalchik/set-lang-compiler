lexer grammar SetsLexer;

options {
  language = Java;
}

@lexer::header {
  package grammar;
}

VARIABLE : 'var';

LEFT_ROUND_BRACKET : '(';
RIGHT_ROUND_BRACKET : ')';

LEFT_BRACE : '{';
RIGHT_BRACE : '}';

COMMA : ',';
COLON : ':';
SEMICOLON : ';';

ASSIGN : '=';
ASSIGN_PLUS : '+=';
ASSIGN_MINUS : '-=';

FUNCTION : 'function';
DEFINITION : 'def';
RETURN : 'return';
WRITE : 'write:';
READ : 'read:';
TEMPLATE : 'template';

OP_MULTIPLY : '*';
OP_SLASH : '\\';
OP_PLUS : '+';
OP_MINUS : '-';
OP_EQUAL : '==';
OP_NOT_EQUAL : '!=';
OP_LESS : '<';
OP_MORE : '>';
OP_LESS_OR_EQUAL : '<=';
OP_MORE_OR_EQUAL : '>=';

FOR : 'for';
IF : 'if';
ELSE : 'else';
WHILE : 'while';
DO : 'do';

INT   : '0'..'9'+;

FLOAT   : ('0'..'9')+ '.' ('0'..'9')*;

TEMPLATE_ID : ('A'..'Z')+;

ID : ('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'0'..'9'|'_')*;

COMMENT
    :   '//' ~('\n'|'\r')* '\r'? '\n' {$channel=HIDDEN;}
    |   '/*' ( options {greedy=false;} : . )* '*/' {$channel=HIDDEN;}
    ;

WS  :   ( ' '
        | '\t'
        | '\r'
        | '\n'
        ) {$channel=HIDDEN;}
    ; 
