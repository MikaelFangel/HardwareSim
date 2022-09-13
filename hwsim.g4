grammar hwsim;

start   : command* EOF;

command : IDENTIFIER '=' expr ';'
	| 'while' '(' condition ')' block
	;

block : '{' command* '}'
	| command
	;

condition : IDENTIFIER ('>'|'<'|'=='|'!=') expr ;

expr : 	  expr ('*'|'/') expr
	| expr ('+'|'-') expr
	| '(' expr ')'
	| CONST
	| IDENTIFIER
	;

IDENTIFIER : [a-zA-Z_] [a-zA-Z0-9_]*;

CONST : [0-9]+ ('.' [0-9]+)? ;

HVIDRUM : [ \t\n]+ -> skip ;
KOMMENTAR : '//' ~[\n]* -> skip ;
MULTILINECOMMENTS :  '/*'  ( '*'~[/] | ~[*]  )* '*/' -> skip;
