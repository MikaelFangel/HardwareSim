grammar hwsim;

start   : commands* EOF;

commands            : COMMAND IDENTIFIER* 
                    | '.latch' IDENTIFIER '->' IDENTIFIER
                    | '.simulate' expr
                    | COMMAND
                    ;

expr                : IDENTIFIER '=' CONSTANT
                    | IDENTIFIER '=' condition
                    ;

condition           : '!'? IDENTIFIER ('&&' | '||') condition ;

IDENTIFIER          : [a-zA-Z_][a-zA-Z0-9_]* ;
CONSTANT            : [01]+ ;
COMMAND             : '.'[a-zA-Z]+ ;

WHITESPACE          : [ \t\n]+ -> skip ;
COMMENT             : '//' ~[\n]* -> skip ;
MULTILINECOMMENTS   :  '/*'  ( '*'~[/] | ~[*]  )* '*/' -> skip;
