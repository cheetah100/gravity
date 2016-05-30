package nz.net.orcon.kanban.tools;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.jcr.Repository;

import org.apache.jackrabbit.oak.Oak;
import org.apache.jackrabbit.oak.jcr.Jcr;
import org.apache.jackrabbit.oak.plugins.document.DocumentMK;
import org.apache.jackrabbit.oak.plugins.document.DocumentNodeStore;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;

public class MongoRepositoryFactory implements RepositoryFactory {

	private String host;
	
	@Override
	public Repository getFactoryInstance() throws UnknownHostException {
		String[] hostArray = this.getHost().split(",");
		List<String> hostList = Arrays.asList(hostArray);
		
		List<ServerAddress> serverList = new ArrayList<ServerAddress>();
		for( String hostURL : hostList){
			ServerAddress sa = new ServerAddress(hostURL);
			serverList.add(sa);
		}
		
		MongoClient mc = new MongoClient(serverList);
		DB db = mc.getDB("gravity");
	    DocumentNodeStore ns = new DocumentMK.Builder().
	            setMongoDB(db).getNodeStore();		    
	    
	    return new Jcr(new Oak(ns))
	    	.with(new RepositoryIndexInitializer())
	    	.withAsyncIndexing()
	    	.createRepository();	
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}
	
}
