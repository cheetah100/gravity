package nz.net.orcon.kanban.tools;

import javax.jcr.Repository;
import org.apache.jackrabbit.oak.Oak;
import org.apache.jackrabbit.oak.jcr.Jcr;

public class MemoryRepositoryFactory implements RepositoryFactory {
	
	@Override
	public Repository getFactoryInstance() {
		return new Jcr(new Oak()).createRepository();
	}

}
