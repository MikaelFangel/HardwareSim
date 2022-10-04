grammar hwsim;

start   : p=prog EOF;

prog                : h=hardware i=input o=output l+=latch+ u=update s=simulate ;

//latches             :  ls+=latch+           //# MultiLatch
                    //| l=latch                       # SingleLatch
                    //;

hardware            : '.hardware' id=IDENTIFIER ;
input               : '.inputs' id=identifiers ;
output              : '.outputs' id=identifiers ;
latch               : '.latch' id1=IDENTIFIER '->' id2=IDENTIFIER ;
update              : '.update' u=updateDecs ;
simulate            : '.simulate' s=simIns ;

identifiers         : id1=IDENTIFIER id2=identifiers # MultiId
                    | id=IDENTIFIER                  # SingleId
                    ;

updateDecs          : u=updateDec us=updateDecs     # MultiUpdate
                    | u=updateDec                   # SingleUpdate
                    ;

simIns              : s=simIn ss=simIns             # MultiSim
                    | s=simIn                       # SingleSim
                    ;

simIn               : id=IDENTIFIER '=' b=binaries ;
updateDec           : id=IDENTIFIER '=' e=expr ;

binaries            : b1=BINARY b2=binaries         # MultiBin
                    | b=BINARY                      # SingleBin
                    ;

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
