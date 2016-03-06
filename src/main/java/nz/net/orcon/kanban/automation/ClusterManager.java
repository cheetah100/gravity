package nz.net.orcon.kanban.automation;

public interface ClusterManager {
	
	public Long getId(String path, String field) throws Exception;
	
	public String getIdString(String path, String field, String prefix) throws Exception;

	public void setLeader(boolean leader);

	public boolean isLeader();

	public long getStartTime();

	public String getServerId();
	
}
