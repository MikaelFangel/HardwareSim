grammar hwsim;

start   : commands* EOF;

commands            : '.hardware' IDENTIFIER
                    | '.inputs' IDENTIFIER+
                    | '.outputs' IDENTIFIER+
                    | '.latch' latchDec+ 
                    | '.update' updateDec+
                    | '.simulate' simIn
                    | COMMAND
                    ;

simIn               : IDENTIFIER '=' CONSTANT;
updateDec           : IDENTIFIER '=' condition;
latchDec            : IDENTIFIER '->' IDENTIFIER;

condition           : '!' condition
                    | condition ('&&') condition
                    | condition ('||') condition
                    | '(' condition ')'
                    | IDENTIFIER
                    ;

IDENTIFIER          : [a-zA-Z_][a-zA-Z0-9_]* ;
CONSTANT            : [01]+ ;
COMMAND             : '.'[a-zA-Z]+ ;

WHITESPACE          : [ \t\n]+ -> skip ;
COMMENT             : '//' ~[\n]* -> skip ;
MULTILINECOMMENTS   :  '/*'  ( '*'~[/] | ~[*]  )* '*/' -> skip;
