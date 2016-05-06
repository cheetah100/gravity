package nz.net.orcon.kanban.security;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;

public interface SecurityTool {

	public static final String SYSTEM = "system";

	public abstract String getCurrentUser();

	public abstract void iAmSystem() throws Exception;

	/**
	 * Determine if the user is authorised based on the supplied roles.
	 * @param roles
	 * @param filter
	 * @return boolean - is the user authorised?
	 */
	public abstract boolean isAuthorised(Map<String, String> roles,
			String filter);

	public abstract List<GrantedAuthority> getRoles(String userName)
			throws Exception;

	public abstract Map<String, String> initRole(Map<String, String> roles);

}