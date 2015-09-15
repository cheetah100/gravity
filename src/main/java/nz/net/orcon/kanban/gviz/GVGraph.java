/**
 * GRAVITY WORKFLOW AUTOMATION
 * (C) Copyright 2015 Orcon Limited
 * 
 * This file is part of Gravity Workflow Automation.
 *
 * Gravity Workflow Automation is free software: you can redistribute it 
 * and/or modify it under the terms of the GNU General Public License as 
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * Gravity Workflow Automation is distributed in the hope that it will be 
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *    
 * You should have received a copy of the GNU General Public License
 * along with Gravity Workflow Automation.  
 * If not, see <http://www.gnu.org/licenses/>. 
 */

package nz.net.orcon.kanban.gviz;

import java.util.HashMap;
import java.util.Map;

public class GVGraph {

	private String name;
	
	private Map<String,GVNode> nodes;
	
	public GVGraph(String name){
		this.name = name;
		this.nodes = new HashMap<String,GVNode>();
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}	

	public GVNode getNode( String nodeName ) {
		return this.nodes.get(nodeName);
	}
	
	public void addNode( GVNode node){
		nodes.put(node.getName(),node);
	}

	public GVNode addCloneNode(String name, String cloneFrom) throws Exception{
		
		GVNode newNode = new GVNode(name);
		GVNode cloneNode = this.nodes.get(cloneFrom);
	
		if(cloneNode==null){
			throw new Exception("Clone Node Not Found");
		}
		
		newNode.setColor(cloneNode.getColor());
		newNode.setShape(cloneNode.getShape());
		newNode.setStyle(cloneNode.getStyle());
		addNode(newNode);
		return newNode;
	}
	
	public void removeNode( String nodeName){
		nodes.remove(nodeName);
	}
	
	public void linkNodes( String from, String to) throws Exception{
		GVNode fromNode = this.nodes.get(from);
		if(fromNode==null){
			throw new Exception("From Node Not Found");
		}
		
		GVNode toNode = this.nodes.get(from);
		if(toNode==null){
			throw new Exception("To Node Not Found");
		}
		
		fromNode.addLink(to);
	}
	
	public void linkNodes( String from, String to, String color) throws Exception{
		GVNode fromNode = this.nodes.get(from);
		if(fromNode==null){
			throw new Exception("From Node Not Found");
		}
		
		GVNode toNode = this.nodes.get(from);
		if(toNode==null){
			throw new Exception("To Node Not Found");
		}
		
		fromNode.addLink(to,color);
	}
	
	public String toString(){
		
		StringBuilder builder = new StringBuilder();
		builder.append("digraph \""); 
		builder.append(this.name);
		builder.append("\" {\n");
		
		for( GVNode node : this.nodes.values() ){
			node.inject(builder);
		}
		
		builder.append("}\n");
		
		return builder.toString();
	}
}
