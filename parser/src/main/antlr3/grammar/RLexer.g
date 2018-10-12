lexer grammar RLexer;

options {
  language = Java;
}

@lexer::header {
  package grammar;
}


TYPE_TABLE : 'table';
TYPE_COLUMN : 'column';
TYPE_ROW : 'row';

LEFT_ROUND_BRACKET : '(';
RIGHT_ROUND_BRACKET : ')';

LEFT_BRACE : '{';
RIGHT_BRACE : '}';

LEFT_SQUARE_BRACKET : '[';
RIGHT_SQUARE_BRACKET : ']';

COMMA : ',';
COLON : ':';
SEMICOLON : ';';

ASSIGN : '=';
ASSIGN_PLUS : '+=';
ASSIGN_MINUS : '-=';

FUNCTION : 'function';
DEFINITION : 'def';
RETURN : 'return';
WRITE : 'out:';
READ_COLUMN : 'in:';
READ_ROW : 'in{}:';
//READ : READ_COLUMN | READ_ROW;

FOR : 'for';
IF : 'if';
ELSE : 'else';
WHILE : 'while';
DO : 'do';
SWITCH : 'switch';
DEFAULT : 'default';
CASE : 'case';

STRING : '"' ('a'..'z'|'A'..'Z'|'0'..'9'|'_'|':'|WS)+ '"';

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
