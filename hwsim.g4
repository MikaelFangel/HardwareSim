grammar hwsim;

start   : hardware input output latch+ update simulate EOF;

hardware            : '.hardware' IDENTIFIER ;
input               : '.inputs' IDENTIFIER+ ;
output              : '.outputs' IDENTIFIER+ ;
latch               : '.latch' latchDec+  ;
update              : '.update' updateDec+ ;
simulate            : '.simulate' simIn ;

simIn               : IDENTIFIER '=' BINARY ;
updateDec           : IDENTIFIER '=' condition ;
latchDec            : IDENTIFIER '->' IDENTIFIER ;

condition           : '!' condition                     # Negation
                    | c1=condition ('&&') c2=condition  # Conjunction
                    | c1=condition ('||') c2=condition  # Disjunction
                    | '('c1=condition ')'               # Parentheses
                    | x=IDENTIFIER                      # Variable
                    ;

IDENTIFIER          : [a-zA-Z_][a-zA-Z0-9_]* ;
BINARY: [01]+ ;

WHITESPACE          : [ \t\n]+ -> skip ;
COMMENT             : '//' ~[\n]* -> skip ;
MULTILINECOMMENTS   :  '/*'  ( '*'~[/] | ~[*]  )* '*/' -> skip ;
