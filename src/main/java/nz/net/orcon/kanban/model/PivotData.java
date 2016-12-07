package nz.net.orcon.kanban.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * PivotData
 * 
 * The purpose of the PivotData Class is to act as a container for data injected into a pivot table.
 * 
 * @author peter
 *
 */
public class PivotData {
	
	Set<String> xAxisSet = new HashSet<String>();
	
	Set<String> yAxisSet = new HashSet<String>();
	
	Map<String, Number> values = new HashMap<String,Number>();
	
	public void addData( String xAxis, String yAxis, Number value ){
		xAxisSet.add(xAxis);
		yAxisSet.add(yAxis);
		String elementKey = xAxis + ":" + yAxis;
		if( values.containsKey(elementKey) ){
			Number newValue = values.get(elementKey).doubleValue() + value.doubleValue();
			values.put(elementKey, newValue);
		} else {
			values.put(elementKey, value);
		}	
	}
	
	public Map<String, Map<String,Number>> getData() {	
		Map<String, Map<String,Number>> result = new HashMap<String, Map<String, Number>>();
		for( String yAxis : yAxisSet){
			Map<String,Number> newLine = new HashMap<String,Number>();
			for( String xAxis : xAxisSet ){
				String elementKey = xAxis + ":" + yAxis;
				Number number = values.get(elementKey);
				newLine.put(xAxis, number);
			}
			result.put(yAxis, newLine);
		}
		return result;
	}
	
}
