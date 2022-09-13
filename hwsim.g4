grammar hwsim;

start   : command* EOF;

WHITESPACE          : [ \t\n]+ -> skip ;
COMMENT             : '//' ~[\n]* -> skip ;
MULTILINECOMMENTS   :  '/*'  ( '*'~[/] | ~[*]  )* '*/' -> skip;
