grammar hwsim;

start   : p=prog EOF;

prog                : h=hardware i=input o=output l=latches u=update s=simulate ;

latches             : l=latch ls=latches            # MultiLatch
                    | l=latch                       # SingleLatch
                    ;

hardware            : '.hardware' IDENTIFIER ;
input               : '.inputs' id=identifiers ;
output              : '.outputs' id=identifiers ;
latch               : '.latch' l=latchDecs ;
update              : '.update' u=updateDecs ;
simulate            : '.simulate' s=simIn ;

identifiers         : id=IDENTIFIER ids=identifiers # MultiId
                    | id=IDENTIFIER                 # SingleId
                    ;

latchDecs           : l=latchDec ls=latchDecs       # MultiLatchDec
                    | l=latchDec                    # SingleLatchDec
                    ;

updateDecs          : u=updateDec us=updateDecs     # MultiUpdate
                    | u=updateDec                   # SingleUpdate
                    ;

simIn               : IDENTIFIER '=' BINARY ;
updateDec           : IDENTIFIER '=' e=expr ;
latchDec            : IDENTIFIER '->' IDENTIFIER ;

expr                : '!' c1=expr                   # Negation
                    | c1=expr ('&&') c2=expr        # Conjunction
                    | c1=expr ('||') c2=expr        # Disjunction
                    | '('c1=expr ')'                # Parentheses
                    | x=IDENTIFIER                  # Variable
                    ;

IDENTIFIER          : [a-zA-Z_][a-zA-Z0-9_]* ;
BINARY              : [01]+ ;

WHITESPACE          : [ \t\n]+ -> skip ;
COMMENT             : '//' ~[\n]* -> skip ;
MULTILINECOMMENTS   :  '/*'  ( '*'~[/] | ~[*]  )* '*/' -> skip ;
