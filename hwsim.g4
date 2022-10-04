grammar hwsim;

start   : p=prog EOF;

prog                : h=hardware i=input o=output l=latches u=update s=simulate ;

latches             : l=latch ls=latches            # MultiLatch
                    | l=latch                       # SingleLatch
                    ;

hardware            : '.hardware' id=IDENTIFIER ;
input               : '.inputs' id+=IDENTIFIER+ ;
output              : '.outputs' id+=IDENTIFIER+ ;
latch               : '.latch' l=latchDec ;
update              : '.update' u=updateDecs ;
simulate            : '.simulate' s=simIns ;

updateDecs          : u=updateDec us=updateDecs     # MultiUpdate
                    | u=updateDec                   # SingleUpdate
                    ;

simIns              : s=simIn ss=simIns             # MultiSim
                    | s=simIn                       # SingleSim
                    ;

simIn               : id=IDENTIFIER '=' b+=BINARY+ ;
updateDec           : id=IDENTIFIER '=' e=expr ;
latchDec            : id1=IDENTIFIER '->' id2=IDENTIFIER ;

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
