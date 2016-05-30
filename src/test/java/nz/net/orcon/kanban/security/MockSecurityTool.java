package nz.net.orcon.kanban.security;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;

public class MockSecurityTool implements SecurityTool {

	@Override
	public String getCurrentUser() {
		return this.SYSTEM;
	}

	@Override
	public void iAmSystem() throws Exception {
		// Do Nothing
	}

	@Override
	public boolean isAuthorised(Map<String, String> roles, String filter) {
		return true;
	}

	@Override
	public List<GrantedAuthority> getRoles(String userName) throws Exception {
		return null;
	}

	@Override
	public Map<String, String> initRole(Map<String, String> roles) {
		return null;
	}

}
