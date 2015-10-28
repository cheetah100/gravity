package nz.net.orcon.kanban.model;

import static org.junit.Assert.*;

import org.junit.Test;

public class UserTest {

	@Test
	public void testCheckPassword() throws Exception {		
		User user = new User();
		user.setName("test");
		user.setKey("password");
		assertTrue( user.checkPassword("password") );
	}

}
