grammar hwsim;

start   : p=prog EOF;

prog                : h=hardware i=input o=output l+=latch+ u=update s=simulate ;

hardware            : '.hardware' id=IDENTIFIER ;
input               : '.inputs' id+=IDENTIFIER+ ;
output              : '.outputs' id+=IDENTIFIER+ ;
latch               : '.latch' id1=IDENTIFIER '->' id2=IDENTIFIER ;
update              : '.update' u+=updateDec+ ;
simulate            : '.simulate' s+=simIn+ ;

simIn               : id=IDENTIFIER '=' b+=BINARY+ ;
updateDec           : id=IDENTIFIER '=' e+=expr+ ;

expr                : '!' c1=expr                   # Negation
                    | c1=expr ('&&') c2=expr        # Conjunction
                    | c1=expr ('||') c2=expr        # Disjunction
                    | '(' c1=expr ')'               # Parentheses
                    | x=IDENTIFIER                  # Variable
                    ;

IDENTIFIER          : [a-zA-Z_][a-zA-Z0-9_]* ;
BINARY              : [01] ;

WHITESPACE          : [ \t\n\r]+ -> skip ;
COMMENT             : '//' ~[\n]* -> skip ;
MULTILINECOMMENTS   :  '/*'  ( '*'~[/] | ~[*]  )* '*/' -> skip ;
