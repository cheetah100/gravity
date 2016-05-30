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

package nz.net.orcon.kanban.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.jcr.Credentials;
import javax.jcr.LoginException;
import javax.jcr.NamespaceRegistry;
import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.apache.jackrabbit.ocm.manager.ObjectContentManager;
import org.apache.jackrabbit.ocm.manager.impl.ObjectContentManagerImpl;
import org.apache.jackrabbit.ocm.mapper.Mapper;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.AnnotationMapperImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class OcmMapperFactory {
	
	private static final Logger logger = LoggerFactory.getLogger(OcmMapperFactory.class);
		
	private Repository repository;
	
	private String domainPackage;
	private List<String> classList;
	private List<String> mainNodes;
	
	private String user;
	private String password;
	
	private Credentials credentials;
	private Mapper mapper;
	private boolean mainNodesChecked = false;
	
	private RepositoryFactory repositoryFactory;

	private void generateMapper() throws ClassNotFoundException{
		List<Class> classes = new ArrayList<Class>();
		for( String classItem : classList){
			Class<?> forName = Class.forName(domainPackage + "." + classItem);
			classes.add( forName );
		}
		this.mapper = new AnnotationMapperImpl(classes);
	}
	
	public ObjectContentManager getOcm() throws Exception {
		
		if(this.repository==null){
			logger.info("Creating Repository using factory: " + this.repositoryFactory.getClass().getName());
			this.repository = this.repositoryFactory.getFactoryInstance();
		}
		
		if(this.mapper==null){
			logger.info("Generating Mapper");
			generateMapper();
		}
		
		if(this.credentials==null){
			this.credentials = new SimpleCredentials(this.user, this.password.toCharArray());
		}
		Session session = repository.login(this.credentials);
		
		if(!this.mainNodesChecked){
			initialise(session);
			this.mainNodesChecked = true;
		}
		return new ObjectContentManagerImpl(session, this.mapper);
	}

	public void initialise(Session session) throws LoginException, RepositoryException {
		final NamespaceRegistry registry = session.getWorkspace().getNamespaceRegistry();
		
		if (!Arrays.asList(registry.getPrefixes()).contains("ocm")) {
			session.getWorkspace().getNamespaceRegistry().registerNamespace("ocm", "http://jackrabbit.apache.org/ocm");
		}
		
		Node rootNode = session.getRootNode();
		for( String nodeName : this.mainNodes){
			if(!rootNode.hasNode(nodeName)){
				rootNode.addNode(nodeName);
			}
		}
		session.save();
	}
	
	/**
	 * EventListener listener,  - listener
	 * int eventTypes,          - to be determined
	 * String absPath,          - /board
	 * boolean isDeep,          - true
	 * String[] uuid,           - null
	 * String[] nodeTypeName,   - null
	 * boolean noLocal          - false
	 * 
	 * @param session
	 */
	private void setupListener(){
		
		//session.getWorkspace().getObservationManager().addEventListener(arg0, arg1, arg2, arg3, arg4, arg5, arg6)
		
	}

	public void setClassList(List<String> classList) {
		this.classList = classList;
	}

	public List<String> getClassList() {
		return classList;
	}

	public void setDomainPackage(String domainPackage) {
		this.domainPackage = domainPackage;
	}

	public String getDomainPackage() {
		return domainPackage;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public Repository getRepository() {
		return repository;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getUser() {
		return user;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword() {
		return password;
	}

	public void setMainNodes(List<String> mainNodes) {
		this.mainNodes = mainNodes;
	}

	public List<String> getMainNodes() {
		return mainNodes;
	}

	public RepositoryFactory getRepositoryFactory() {
		return repositoryFactory;
	}

	public void setRepositoryFactory(RepositoryFactory repositoryFactory) {
		this.repositoryFactory = repositoryFactory;
	}
}
