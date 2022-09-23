grammar hwsim;

start   : p=prog EOF;

prog                : h=hardware i=input o=output l+=latch+ u=update s=simulate ;

hardware            : '.hardware' IDENTIFIER ;
input               : '.inputs' IDENTIFIER+ ;
output              : '.outputs' IDENTIFIER+ ;
latch               : '.latch' l+=latchDec+  ;
update              : '.update' u+=updateDec+ ;
simulate            : '.simulate' s=simIn ;

simIn               : IDENTIFIER '=' BINARY ;
updateDec           : IDENTIFIER '=' e=expr ;
latchDec            : IDENTIFIER '->' IDENTIFIER ;

expr                : '!' c1=expr             # Negation
                    | c1=expr ('&&') c2=expr  # Conjunction
                    | c1=expr ('||') c2=expr  # Disjunction
                    | '('c1=expr ')'          # Parentheses
                    | x=IDENTIFIER            # Variable
                    ;

IDENTIFIER          : [a-zA-Z_][a-zA-Z0-9_]* ;
BINARY              : [01]+ ;

WHITESPACE          : [ \t\n]+ -> skip ;
COMMENT             : '//' ~[\n]* -> skip ;
MULTILINECOMMENTS   :  '/*'  ( '*'~[/] | ~[*]  )* '*/' -> skip ;
