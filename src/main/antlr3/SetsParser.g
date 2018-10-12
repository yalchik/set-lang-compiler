parser grammar SetsParser;

options {
  language = Java;
  tokenVocab = SetsLexer;
  output = template;
}

@header {
  package grammar;

  import semantic.SemanticHandler;
  import errors.ErrorsTable;
  import namestable.Variable;
}

@members { 
    private final SemanticHandler semanticHandler = new SemanticHandler();
    
    public ErrorsTable getErrorsTable() {
      return semanticHandler.getErrorsTable();
    }
}

// в исходном файле могут размещаться объявления функций, переменных и определения функций
// переменные, объявленые здесь, считаются глобальными
program
@init {
    // список инструкций сгенерированного кода
    List<StringTemplate> stList = new ArrayList<>();
}
  : (declaration_func  { stList.add($declaration_func.st); }
  | declaration_var    { stList.add($declaration_var.st); }
  | definition_func    { stList.add($definition_func.st); }  
  )+
  {
    semanticHandler.checkMainFunction();
  }
  -> program(statements={stList})
  ;
  
// объявление переменной с возможной инициализацией
// Пример: var x = 5;
declaration_var
  : VARIABLE ID
  {
    semanticHandler.declareVariable($ID.text, $ID.line);
  }  
  (ASSIGN expression)?
  SEMICOLON
  -> declaration_var(id={%variable(id={$ID.text})}, expression={$expression.st})
  ;
  
// объявление функции
// Пример: func union(var s1, var s2);
// Пример настраиваемой функции:
// template<T> func union(T s1, T s2);
declaration_func 
  : template?
  FUNCTION ID
  LEFT_ROUND_BRACKET
  parameters?
  RIGHT_ROUND_BRACKET
  SEMICOLON
  {
    semanticHandler.declareFunction($ID.text, $parameters.argsList, $ID.line, $template.templateList);
  }
  -> declaration_func(template={$template.st}, name={%function(name={$ID.text})}, params={$parameters.st})
  ;
  
// определение функции
// Пример: def union(var s1, var s2) { }
definition_func 
  : template?
  DEFINITION ID
  LEFT_ROUND_BRACKET
  parameters?
  RIGHT_ROUND_BRACKET
  {
    semanticHandler.defineFunction($ID.text, $parameters.argsList, $ID.line, $template.templateList);
  }
  block
  {
    $block.stList.add(%operator_return(expr={"NULL"}));
  }
  -> definition_func(template={$template.st}, name={%function(name={$ID.text})}, params={$parameters.st}, block={$block.st})
  ;
  
// префикс для настраиваемой функции
// Пример: template <T,X>
template returns [List<String> templateList]
@init {
  $templateList = new ArrayList<>();
  List<StringTemplate> stList = new ArrayList<>();
}
  : TEMPLATE
  OP_LESS
  tid=TEMPLATE_ID
  {
    $templateList.add($tid.text);
    stList.add(%template_param(param={$tid.text}));
  }
  (COMMA
  tid=TEMPLATE_ID
  {
    $templateList.add($tid.text);
    stList.add(%template_param(param={$tid.text}));
  }
  )*
  OP_MORE
  -> template_func(params={stList})
  ;
  
// параметры (используются в объявлениях и определениях функций
// Пример: var s1, var s2
// Пример с шаблонными параметрами: T s1, T s2
parameters returns [List<Variable> argsList]
@init {
  List<StringTemplate> stList = new ArrayList<>();
  $argsList = new ArrayList<>();
  String typeStr;
  String idStr;
}
  : (VARIABLE | t1=TEMPLATE_ID) id=ID
  {
    $argsList.add(new Variable($id.text, $id.line));
    typeStr = $t1.text != null ? $t1.text + " ": "Object* ";
    stList.add(new StringTemplate(typeStr));
    stList.add(%variable(id={$id.text}));    
  }
  (COMMA
	  (VARIABLE | t2=TEMPLATE_ID) id=ID
	  {
	    stList.add(new StringTemplate($COMMA.text + " "));
	    $argsList.add(new Variable($id.text, $id.line));
	    typeStr = $t2.text != null ? $t2.text + " " : "Object* ";
	    stList.add(new StringTemplate(typeStr));
	    stList.add(%variable(id={$id.text}));
	  }
  )*
  -> text(text={stList})
  ;
  
// блок функции или оператора
// Пример: { }
block returns [List<StringTemplate> stList]
@init {
  $stList = new ArrayList<>();
}
  :
  {
    semanticHandler.enterLocalNamesTable();
  } 
  LEFT_BRACE
  (statement
	  {
	    $stList.add($statement.st);
	  }
  )*
  RIGHT_BRACE
  {
    semanticHandler.enterParentNamesTable();
  }
  -> block(body={$stList})
  ;
  
// инструкция
// Пример: if (1 == 1) { }
statement 
  : declaration_var           { $st = $declaration_var.st;  }
  | operator_assign           { $st = $operator_assign.st;  }
  | operator_if               { $st = $operator_if.st;      }
  | operator_while            { $st = $operator_while.st;   }
  | operator_for              { $st = $operator_for.st;     }
  | operator_call SEMICOLON   { $st = new StringTemplate($operator_call.st + $SEMICOLON.text); }
  | operator_return           { $st = $operator_return.st;  }
  | operator_read             { $st = $operator_read.st;    }
  | operator_write            { $st = $operator_write.st;   }
  ;
  
// оператор присваивания
// Пример: x = 5;
operator_assign 
  : ID (op=ASSIGN | op=ASSIGN_PLUS | op=ASSIGN_MINUS) expression SEMICOLON
  {
    semanticHandler.checkVariableDeclaration($ID.text, $ID.line);
  }
  -> operator_assign(id={%variable(id={$ID.text})}, expression={$expression.st})
  ;
  
// оператор ветвления
operator_if 
  : IF LEFT_ROUND_BRACKET expression RIGHT_ROUND_BRACKET b1=block (ELSE b2=block)?
  -> operator_if(expr={$expression.st}, block={$b1.st}, elseblock={$b2.st})
  ;
  
// оператор цикла (логически управляемый)
operator_while
  : WHILE LEFT_ROUND_BRACKET expression RIGHT_ROUND_BRACKET block
  -> operator_while(expr={$expression.st}, block={$block.st})
  ;
  
// оператор цикла (основанный на структуре) - аналог foreach в java
// Пример: for (x : set) { }
operator_for
  : FOR LEFT_ROUND_BRACKET ID COLON expression RIGHT_ROUND_BRACKET block
  -> operator_for(id={%variable(id={$ID.text})}, expr={$expression.st}, block={$block.st})
  ;
  
// оператор вызова функции
// Пример: union(x, {1,2})
operator_call
@init {
  List<StringTemplate> argsStList = new ArrayList<>();
}
  : ID
  {
    semanticHandler.checkFunctionDeclaration($ID.text, $ID.line);
  }
  LEFT_ROUND_BRACKET
  (e1=expression      { argsStList.add($e1.st); }
  (COMMA
  e2=expression       { argsStList.add(new StringTemplate(", ")); argsStList.add($e2.st); }
  )*
  )?
  RIGHT_ROUND_BRACKET
  -> operator_call(name={%function(name={$ID.text})}, args={argsStList})
  ;
  
// оператор возвращения значения из функции
operator_return
  : RETURN expression SEMICOLON
  -> operator_return(expr={$expression.st})
  ;
  
// оператор чтения из консоли в переменную
operator_read
  : READ ID SEMICOLON
  {
    semanticHandler.checkVariableDeclaration($ID.text, $ID.line);
  }
  -> operator_read(id={%variable(id={$ID.text})})
  ;
  
// оператор вывода значения выражения на консоль
operator_write 
  : WRITE expression SEMICOLON
  -> operator_write(expr={$expression.st})
  ;
  
// атомарное выражение
// может быть идентификатором, выражением в скобках, константой, вызовом функции
// имеет наивысший приоритет
term
  : ID
    {
      semanticHandler.checkVariableDeclaration($ID.text, $ID.line);
    }
    -> variable(id={$ID.text})
  | LEFT_ROUND_BRACKET expression RIGHT_ROUND_BRACKET
    -> bracket_operator(expr={$expression.st})
  | constant_expression
    -> { $constant_expression.st }
  | operator_call
    -> { $operator_call.st }
  ;

// операнд пересечения множеств или произведение элементов
// приоритет ниже, чем у атомарного выражения
operand_intersection 
  : term { $st = $term.st; }
  ;
  
// операнд объединения и разности множеств
// или сумма и разность элементов
// приоритет ниже, чем у пересечения множеств
operand_union_diff 
  : o1=operand_intersection (op=OP_MULTIPLY o2=operand_intersection)*
  {
    if ($o2.text != null) {
      $st = %binary_operator(o1={$o1.st}, op={$op.text}, o2={$o2.st});
    }
    else {
      $st = %text(text={$o1.st});
    }
  }
  ;
  
// операнд отношений равенства и неравенства
// приоритет ниже, чем у объединения и разности
// самый низкий приоритет
operand_rel 
  : o1=operand_union_diff ((op=OP_PLUS | op=OP_MINUS) o2=operand_union_diff)*
  {
    if ($o2.text != null) {
      $st = %binary_operator(o1={$o1.st}, op={$op.text}, o2={$o2.st});
    }
    else {
      $st = %text(text={$o1.st});
    }
  }
  ;
  
// обобщённое выражение
// Примеры:
// s1
// s1 \ s2
// (s1 \ s2)
// (s1 \ s2) + (s2 \ s1)
expression
  : o1=operand_rel ((op=OP_EQUAL | op=OP_NOT_EQUAL) o2=operand_rel)*
  {
    if ($o2.text != null) {
      $st = %binary_operator(o1={$o1.st}, op={$op.text}, o2={$o2.st});
    }
    else {
      $st = %text(text={$o1.st});
    }
  }
  ;

// константное выражение (множество или элемент)
constant_expression 
  : (c=set
  | c=element)
  {
    $st = $c.st;
  }
  ;
  
// константное множество
// Пример: {1, 2.2}
set
@init {
  List<StringTemplate> stList = new ArrayList<>();
}
  : LEFT_BRACE
  {
    stList.add(%const_set());
  }
  (e=element  { stList.add(%insert_element(element={$e.st})); }
  (COMMA
  e=element   { stList.add(%insert_element(element={$e.st})); }
  )*
  )?
  RIGHT_BRACE
  -> text(text={stList})
  ;
  
// константный элемент
// Пример: 5
element
  : INT   -> const_element_int(elem={$INT.text})
  | FLOAT -> const_element_float(elem={$FLOAT.text})
  ;