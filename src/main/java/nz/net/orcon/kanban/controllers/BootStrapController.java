package nz.net.orcon.kanban.controllers;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.jackrabbit.ocm.manager.ObjectContentManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import nz.net.orcon.kanban.model.Team;
import nz.net.orcon.kanban.model.User;
import nz.net.orcon.kanban.tools.IdentifierTools;
import nz.net.orcon.kanban.tools.ListTools;
import nz.net.orcon.kanban.tools.OcmMapperFactory;

@Controller
@RequestMapping("/bootstrap")
public class BootStrapController {
	
	private static final Logger logger = LoggerFactory.getLogger(BootStrapController.class);
	
	@Resource(name="ocmFactory")
	OcmMapperFactory ocmFactory;
	
	@Autowired
	TeamCache teamCache;
	
	@Autowired
	TeamController teamController;
	
	@Autowired 
	private ListTools listTools;
	
	@RequestMapping(value = "", method=RequestMethod.GET)
	public @ResponseBody boolean bootstrap() throws Exception {
			
		boolean result = true;
		
		ObjectContentManager ocm = ocmFactory.getOcm();
		try {
			// Determine if there are new users
			Map<String, String> userList = listTools.list(String.format(URI.USER_URI,""), "name", ocm.getSession());
			logger.info("Users: " + userList.toString());
			result = userList.isEmpty();
			
			if(!userList.containsKey("admin")){
				User user = new User();
				user.setId("admin");
				user.setName("admin");
				user.setFirstname("Admin");
				user.setSurname("");
				user.setEmail("");
				user.setKey("password");
				String newId = IdentifierTools.getIdFromNamedModelClass(user);
				user.setPath(String.format(URI.USER_URI, newId.toString()));
				user.setKey(null);
				ocm.insert(user);			
				ocm.save();
				logger.info("Bootstrapping New User: "+ user.getName());
			}
						
		} finally {
			ocm.logout();		
		}
		
		try{ 
			Team team = teamCache.getItem("administrators");
			logger.info("Administration Team Exists");
				
			Map<String,String> ownerList = team.getOwners();
			if(!ownerList.isEmpty()){
				logger.info("Administration Owners: " + ownerList.toString());
			}
	
			Map<String,String> memberList = team.getMembers();
			if(!memberList.isEmpty()){
				logger.info("Administration Owners: " + memberList.toString());
			}
			result = false;
			
		} catch( Exception e){
			Map<String,String> members = new HashMap<String,String>();
			members.put("admin", "ADMIN");
			Team team = new Team();
			team.setName("Administrators");
			team.setMembers(members);
			team.setOwners(members);
			teamController.createTeam(team);
			logger.info("Bootstrapping New Administration Team");
		}
		return result;
	}

}
