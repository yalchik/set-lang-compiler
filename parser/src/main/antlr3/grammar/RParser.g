parser grammar RParser;

options {
  language = Java;
  tokenVocab = RLexer;
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

// в исходном файле могут размещаться объявления функций, определения функций и инструкции
// переменные, инициализируемые здесь, считаются глобальными
program
scope {
  List<StringTemplate> varList;
}
@init {
    // список инструкций сгенерированного кода в глобальной области видимости
    List<StringTemplate> stList = new ArrayList<>();
    // список инструкций сгенерированного кода для функций
    List<StringTemplate> funcList = new ArrayList<>();
    
    $program::varList = new ArrayList<>();
}
  : (declaration_func
  | definition_func   { funcList.add($definition_func.st); }
  | statement         { stList.add($statement.st); }
  )+
  -> program(statements={stList}, functions={funcList}, variables={$program::varList})
  ;
  
 
// объявление функции
// Пример: func join(t1, t2);
declaration_func 
  : FUNCTION ID
  LEFT_ROUND_BRACKET
  parameters?
  RIGHT_ROUND_BRACKET
  SEMICOLON
  { semanticHandler.declareFunction($ID.text, $parameters.argsList, $ID.line); }
  ;
  
// определение функции
// Пример: def join(t1, t2) { }
definition_func returns [List<StringTemplate> stList]
scope {
  List<StringTemplate> varList;
}
@init {
  $stList = new ArrayList<>();
  $definition_func::varList = new ArrayList<>();
}
  : DEFINITION ID
  LEFT_ROUND_BRACKET
  parameters?
  RIGHT_ROUND_BRACKET
  {
    semanticHandler.defineFunction($ID.text, $parameters.argsList, $ID.line);
    semanticHandler.enterLocalNamesTable();
  }
  LEFT_BRACE
  (statement { $stList.add($statement.st); } )*
  RIGHT_BRACE
  {
    semanticHandler.checkFunctionReturn();
    semanticHandler.enterParentNamesTable();    
  }
  -> definition_func(name={%function(name={$ID.text})}, params={$parameters.st}, body={$stList}, variables={$definition_func::varList})
  ;
  
  
// параметры (используются в объявлениях и определениях функций)
// Пример: t1, t2
parameters returns [List<Variable> argsList]
@init {
  List<StringTemplate> stList = new ArrayList<>();
  $argsList = new ArrayList<>();
}
  : id=ID
  {
    $argsList.add(new Variable($id.text, $id.line));
    stList.add(%entity_type());
    stList.add(%variable(id={$id.text}));
  }
  (COMMA id=ID
    {     
      $argsList.add(new Variable($id.text, $id.line));
      stList.add(new StringTemplate(", "));
      stList.add(%entity_type());
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
  : LEFT_BRACE
  (statement { $stList.add($statement.st); } )*
  RIGHT_BRACE
  -> block(body={$stList})
  ;
  
// инструкция
// Пример: if (1 == 1) { }
statement 
  : operator_assign           { $st = $operator_assign.st;  }
  | operator_if               { $st = $operator_if.st;      }
  | operator_while            { $st = $operator_while.st;   }
  | operator_for              { $st = $operator_for.st;     }
  | operator_call SEMICOLON   { $st = new StringTemplate($operator_call.st + $SEMICOLON.text); }
  | operator_return           { $st = $operator_return.st;  }
  | operator_read             { $st = $operator_read.st;    }
  | operator_write            { $st = $operator_write.st;   }
  | operator_switch           { $st = $operator_switch.st;  }
  ;
  
// оператор присваивания
// Пример:
// col = "5";
// t += col;
operator_assign 
  : ID
  ( op=ASSIGN
  | op=ASSIGN_PLUS
  | op=ASSIGN_MINUS)
  expression
  SEMICOLON
  {
    StringTemplate var = %variable(id={$ID.text});
    
    // если переменная ещё не объявлялась,
    // то генерируем объявляющий код
    // либо в глобальном пространстве имён,
    // либо в пространстве имён функции
    if (!semanticHandler.checkVariableDeclaration($ID.text, $ID.line)) {
      if (semanticHandler.isGlobalCurrentNamesTable()) {
	      $program::varList.add(%declaration_var(id={var}));
	    }
	    else {
	        $definition_func::varList.add(%declaration_var(id={var}));
	    }
    }
    
    // решаем, который из шаблонов применять для генерации оператора присваивания
    if ($op.text.equals("=")) {
      $st = %operator_assign(id={var}, expression={$expression.st});
    }
    else if ($op.text.equals("+=")) {
      $st = %operator_plus(id={var}, expression={$expression.st});
    }
    else if ($op.text.equals("-=")) {      
      $st = %operator_minus(id={var}, expression={$expression.st});
    }
  }
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
// Пример: for (row : table) { }
operator_for
  : FOR LEFT_ROUND_BRACKET ID
		{
		  semanticHandler.checkVariableDeclaration($ID.text, $ID.line);
		}
		COLON expression RIGHT_ROUND_BRACKET block
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
  (e1=expression      { argsStList.add(%arg(st={$e1.st})); }
  (COMMA
  e2=expression       { argsStList.add(new StringTemplate(", ")); argsStList.add(%arg(st={$e2.st})); }
  )*
  )?
  RIGHT_ROUND_BRACKET
  {
    semanticHandler.checkCallFunction($ID.text, argsStList, $ID.line);
  }
  -> operator_call(name={%function(name={$ID.text})}, args={argsStList})
  ;
  
// оператор возвращения значения из функции
operator_return
  : RETURN expression? SEMICOLON
  {
    semanticHandler.setCurrentFunctionReturn();
  }
  -> operator_return(expr={$expression.st})
  ;
  
// оператор чтения из консоли в переменную
operator_read
@init {
  int type = 0;
}
  : (READ_COLUMN  { type = 1; }
  | READ_ROW      { type = 2; } )
  ID
  SEMICOLON
  {
    StringTemplate var = %variable(id={$ID.text});
    
    // если переменная ещё не объявлялась,
    // то генерируем объявляющий код
    // либо в глобальном пространстве имён,
    // либо в пространстве имён функции
    if (!semanticHandler.checkVariableDeclaration($ID.text, $ID.line)) {
      if (semanticHandler.isGlobalCurrentNamesTable()) {
        $program::varList.add(%declaration_var(id={var}));
      }
      else {
          $definition_func::varList.add(%declaration_var(id={var}));
      }
    }
        
    if (type == 1) {
      $st = %operator_read(id={var},init={"new Column()"});
    }
    else if (type == 2) {
      $st = %operator_read(id={var},init={"new Row()"});
    }
  }
  ;
  
// оператор вывода значения выражения на консоль
operator_write 
  : WRITE expression SEMICOLON
  -> operator_write(expr={$expression.st})
  ;
  
// Пример:
// switch (t[i][j]) {
// case "1": out: "1";
// }
operator_switch
  : SWITCH
  LEFT_ROUND_BRACKET
  expression
  RIGHT_ROUND_BRACKET
  case_block
  -> operator_switch(expr={$expression.st}, block={$case_block.st})
  ;
  
// Пример
// {
// case "1": out: "1";
// default:
// }
case_block returns [List<StringTemplate> stList]
@init {
  $stList = new ArrayList<>();
}
  : LEFT_BRACE
  (operator_case    { $stList.add($operator_case.st); } )+
  operator_default?
  RIGHT_BRACE
  -> case_block(cases={$stList}, default_case={$operator_default.st})
  ;
  
// Пример:
// case "1": out: "1";
operator_case returns [List<StringTemplate> stList]
@init {
  $stList = new ArrayList<>();
}
  : CASE
  constant_expression
  COLON
  (statement { $stList.add($statement.st); } )*
  -> operator_case(expr={$constant_expression.text}, statements={$stList})
  ;
  
// Пример: default: out: "Hello";
operator_default returns [List<StringTemplate> stList]
@init {
  $stList = new ArrayList<>();
}
  : DEFAULT
  COLON
  (statement { $stList.add($statement.st); } )*
  -> operator_default(statements={$stList})
  ;
  
// Оператор приведения типов
// col = "id";
// t = (Table) col;
// получили таблицу из одной колонки
operator_cast
@init {
  int type = 0;
}
  : LEFT_ROUND_BRACKET
  (TYPE_TABLE     { type = 1; }
  | TYPE_COLUMN   { type = 2; }
  | TYPE_ROW      { type = 3; } )     
  RIGHT_ROUND_BRACKET
  expression
  {
    switch (type) {
    case 1: $st = %operator_cast_to_table(expr={$expression.st});   break;
    case 2: $st = %operator_cast_to_column(expr={$expression.st});  break;
    case 3: $st = %operator_cast_to_row(expr={$expression.st});     break;
    }
  }
  ;
  
// оператор получения содержимого ячейки таблицы
// Пример: t[i][j]
operator_cell
  : ID
  LEFT_SQUARE_BRACKET
  c=expression
  RIGHT_SQUARE_BRACKET
  LEFT_SQUARE_BRACKET
  r=expression
  RIGHT_SQUARE_BRACKET
  -> operator_cell(id={%variable(id={$ID.text})}, column={$c.st}, row={$r.st})
  ;
  
// атомарное выражение
// может быть идентификатором, выражением в скобках, константой,
// вызовом функции, получением ячейки
// имеет наивысший приоритет
term
  : ID
    {
      semanticHandler.checkVariableInitialization($ID.text, $ID.line);
    }
    -> variable(id={$ID.text})
  | LEFT_ROUND_BRACKET expression RIGHT_ROUND_BRACKET
    -> bracket_operator(expr={$expression.st})
  | constant_expression
    -> { $constant_expression.st }
  | operator_call
    -> { $operator_call.st }
  | operator_cell
    -> { $operator_cell.st }
  | operator_cast
    -> { $operator_cast.st }
  ;

// выражение (в данном случае всегда атомарное)
expression 
  : term { $st = $term.st; }
  ;
  
// константное выражение (таблица, столбец или строка)
constant_expression
  : (c=table
  | c=column
  | c=row)
  {
    $st = $c.st;
  }
  ;
  
// константная таблица
// Пример: ["col1", "col2"]
table
@init {
  List<StringTemplate> stList = new ArrayList<>();
}
  : LEFT_SQUARE_BRACKET
  {
    stList.add(%const_table());
  }
  (e=column  { stList.add(%insert_column(element={$e.st})); }
  (COMMA
  e=column   { stList.add(%insert_column(element={$e.st})); }
  )*
  )?
  RIGHT_SQUARE_BRACKET
  -> text(text={stList})
  ;
  
// константный столбец
// "qwe"
column
  : STRING
  -> const_column(elem={$STRING.text})
  ;
  
// константная строка
// {"qwe", "123"}
row
@init {
  List<StringTemplate> stList = new ArrayList<>();
}
  : LEFT_BRACE
  {
    stList.add(%const_row());
  }
  (s=STRING  { stList.add(%insert_cell(element={$s.text})); }
    (COMMA
      s=STRING   { stList.add(%insert_cell(element={$s.text})); }
    )*
  )?
  RIGHT_BRACE
  -> text(text={stList})
  ;