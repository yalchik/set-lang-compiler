import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

abstract class Entity {

    public void print() {
	System.out.println(this.toString());
    }

    public abstract Entity copy();
    
    public void read() {
	throw new RuntimeException("Cannot read this type");
    }

    public Table toTable() {
	throw new RuntimeException("Illegal cast to table");
    }

    public Row toRow() {
	throw new RuntimeException("Illegal cast to row");
    }

    public Column toColumn() {
	throw new RuntimeException("Illegal cast to column");
    }

    public void add(Entity entity) {
	throw new RuntimeException("Illegal add operation.");
    }

    public void remove(Entity entity) {
	throw new RuntimeException("Illegal remove operation.");
    }

    public List<Row> getRows() {
	throw new RuntimeException("Can iterate only for tables");
    }

    public Cell getCell(Entity row, Entity col) {
	throw new RuntimeException("Can get cell by column only for tables");
    }

    public Cell getCell(int index) {
	throw new RuntimeException("Can get cell  by index only for rows");
    }

}

class Column extends Entity {

    private String name;

    public Column() {
	this.name = "";
    }

    public Column(String name) {
	this.name = new String(name);
    }
    
    public Column(Column column) {
	this.name = new String(column.getName());
    }

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    @Override
    public Entity copy() {
	return new Column(this);
    }
    
    @Override
    public Table toTable() {
	return new Table().addColumn(this);
    }

    @Override
    public Row toRow() {
	return new Row().addCell(this.getName());
    }

    @Override
    public Column toColumn() {
	return this;
    }

    @Override
    public String toString() {
	return name;
    }

    @Override
    public void read() {
	try (BufferedReader console = new BufferedReader(new InputStreamReader(
		System.in))) {
	    this.name = console.readLine();
	} catch (IOException e) {
	}
    }

}

class Row extends Entity {

    private List<Cell> cells = new ArrayList<>();

    public Row addCell(String cell) {
	cells.add(new Cell(cell));
	return this;
    }
    
    public Row() {

    }
    
    public Row(Row row) {
	for (Cell cell : row.getCells()) {
	    this.cells.add(new Cell(cell));
	}
    }
    
    public List<Cell> getCells() {
	return cells;
    }

    @Override
    public Entity copy() {
	return new Row(this);
    }
    
    @Override
    public Cell getCell(int index) {
	return cells.get(index);
    }

    @Override
    public Table toTable() {
	return new Table().addRow(this);
    }

    @Override
    public Row toRow() {
	return this;
    }

    @Override
    public String toString() {
	return cells.toString();
    }

    @Override
    public void read() {
	try (BufferedReader console = new BufferedReader(new InputStreamReader(
		System.in))) {
	    for (String str : console.readLine().split(" ")) {
		this.addCell(str);
	    }
	} catch (IOException e) {
	}
    }

}

class Table extends Entity {

    List<Column> columns = new ArrayList<>();
    List<Row> rows = new ArrayList<>();

    public Table() {

    }
    
    public Table(Table table) {
	for (Column column : table.getColumns()) {
	    columns.add(new Column(column));
	}
	for (Row row : table.getRows()) {
	    rows.add(new Row(row));
	}
    }
    
    public Table addColumn(Column column) {
	columns.add(column);
	return this;
    }

    public Table addRow(Row row) {
	rows.add(row);
	return this;
    }

    @Override
    public Entity copy() {
	return new Table(this);
    }
    
    @Override
    public List<Row> getRows() {
	return new ArrayList<>(rows);
    }
    
    public List<Column> getColumns() {
	return columns;
    }

    @Override
    public Cell getCell(Entity col, Entity row) {
	int indexOfColumn = columns.indexOf(col);
	if (indexOfColumn == -1) {
	    String name = col.toColumn().getName();
	    for (Column c : columns) {
		if (c.getName().equals(name)) {
		    indexOfColumn = columns.indexOf(c);
		    break;
		}
	    }
	}
	return indexOfColumn == -1 ? new Cell() : row.getCell(indexOfColumn);
    }

    @Override
    public Table toTable() {
	return this;
    }

    @Override
    public String toString() {
	StringBuffer sb = new StringBuffer();
	sb.append(columns.toString());
	sb.append("\n--------\n");
	sb.append(rows.toString());
	return sb.toString();
    }

    public void add(Entity entity) {
	if (entity instanceof Row) {
	    rows.add((Row) entity);
	}
	if (entity instanceof Column) {
	    columns.add((Column) entity);
	}
    }

    public void remove(Entity entity) {
	if (entity instanceof Row) {
	    rows.remove(entity);
	}
	if (entity instanceof Column) {
	    columns.remove(entity);
	}
    }

}

class Cell extends Entity {

    private String content;

    public Cell() {
	content = "";
    }
    
    public Cell(Cell cell) {
	this.content = new String(cell.getContent());
    }
    
    public Entity copy() {
	return new Cell(this);
    }

    public Cell(String content) {
	this.content = content;
    }
    
    public String getContent() {
	return content;
    }

    public String toString() {
	return content;
    }

}