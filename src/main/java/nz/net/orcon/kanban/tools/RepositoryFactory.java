package nz.net.orcon.kanban.tools;

import javax.jcr.Repository;

public interface RepositoryFactory {
	
	public Repository getFactoryInstance() throws Exception;

}
