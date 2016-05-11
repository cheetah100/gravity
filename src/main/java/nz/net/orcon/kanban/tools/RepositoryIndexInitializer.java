package nz.net.orcon.kanban.tools;

import org.apache.jackrabbit.oak.plugins.index.IndexUtils;
import org.apache.jackrabbit.oak.spi.lifecycle.RepositoryInitializer;
import org.apache.jackrabbit.oak.spi.state.NodeBuilder;

import com.google.common.collect.ImmutableList;

public class RepositoryIndexInitializer implements RepositoryInitializer{

	@Override
	public void initialize(NodeBuilder builder) {
		
		NodeBuilder root = builder.getBaseState().builder(); 
		NodeBuilder index = IndexUtils.getOrCreateOakIndex(root); 

		ImmutableList fields = 
				ImmutableList.builder().add( "id","name" ).build(); 

		IndexUtils.createIndexDefinition(index, "gravityIndexes", true, false, fields, null); 
		
	}
}
