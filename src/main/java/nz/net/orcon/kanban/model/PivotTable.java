package nz.net.orcon.kanban.model;

import java.io.Serializable;

import org.apache.jackrabbit.ocm.mapper.impl.annotation.Field;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Node;

@Node
public class PivotTable extends AbstractNamedModelClass implements Serializable{
	
	private static final long serialVersionUID = -8796432214417007588L;

	@Field
	private String filter;
	
	@Field
	private String xAxis;
	
	@Field
	private String yAxis;
	
	@Field
	private String field;
	
	@Field
	private PivotType type;

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	public String getxAxis() {
		return xAxis;
	}

	public void setxAxis(String xAxis) {
		this.xAxis = xAxis;
	}

	public String getyAxis() {
		return yAxis;
	}

	public void setyAxis(String yAxis) {
		this.yAxis = yAxis;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}
	
}
