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

import java.util.ArrayList;
import java.util.List;

public class GVNode {
	
	private String name;
	private GVShape shape;
	private GVStyle style;
	private String color;
	private List<GVLink> links;

	public GVNode( String name ){
		this.name = name;
		this.color = "white";
		this.shape = GVShape.box;
		this.setStyle(GVStyle.solid);
		this.links = new ArrayList<GVLink>();
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public GVShape getShape() {
		return shape;
	}

	public void setShape(GVShape shape) {
		this.shape = shape;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}
	
	public GVStyle getStyle() {
		return style;
	}

	public void setStyle(GVStyle style) {
		this.style = style;
	}

	public List<GVLink> getLinks() {
		return links;
	}

	public void addLink(String gvNode){
		this.links.add( new GVLink(gvNode,null));
	}

	public void addLink(String gvNode, String color){
		this.links.add( new GVLink(gvNode,color));
	}
	
	/*
	public void removeLink(GVNode gvNode){
		this.links.remove(gvNode);
	}
	*/
	
	public void inject( StringBuilder builder){
		builder.append("\"");
		builder.append(this.name);
		builder.append("\" [");
		appendProperty("color", this.color, builder);
		appendProperty("shape", this.shape, builder);
		appendProperty("style", this.style, builder);		
		builder.append("]\n");
		
		for( GVLink link : this.links){
			builder.append("\"");
			builder.append(this.name);
			builder.append("\"->\"");
			builder.append(link.getName());
			builder.append("\"");
			if( link.getColor()!=null){
				builder.append("[color=\"");
				builder.append(link.getColor());
				builder.append("\"]");
			}
			builder.append("\n");
		}
	}
	
	private void appendProperty( String field, Object value, StringBuilder builder ){
		if(value==null) return;
		builder.append(field);
		builder.append("=");
		builder.append(value);
		builder.append(" ");
	}
	
}
