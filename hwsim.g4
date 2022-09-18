grammar hwsim;

start   : commands* EOF;

commands            : COMMAND IDENTIFIER* 
                    | '.latch' expr
                    | '.simulate' expr
                    | '.update' expr+
                    | COMMAND
                    ;

expr                : IDENTIFIER '=' CONSTANT
                    | IDENTIFIER '=' condition
                    | IDENTIFIER '->' IDENTIFIER
                    ;

condition           : '!' condition
                    | condition '&&' condition
                    | IDENTIFIER
                    ;

IDENTIFIER          : [a-zA-Z_][a-zA-Z0-9_]* ;
CONSTANT            : [01]+ ;
COMMAND             : '.'[a-zA-Z]+ ;

WHITESPACE          : [ \t\n]+ -> skip ;
COMMENT             : '//' ~[\n]* -> skip ;
MULTILINECOMMENTS   :  '/*'  ( '*'~[/] | ~[*]  )* '*/' -> skip;
