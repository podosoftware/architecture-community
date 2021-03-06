package architecture.community.web.spring.controller.data.v1;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.acls.model.AccessControlEntry;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.NativeWebRequest;

import architecture.community.exception.NotFoundException;
import architecture.community.model.Models;
import architecture.community.security.spring.acls.CommunityPermissions;
import architecture.community.security.spring.acls.JdbcCommunityAclService;
import architecture.community.security.spring.acls.ObjectAccessControlEntry;
import architecture.community.user.CommunityUser;
import architecture.community.user.DefaultRole;
import architecture.community.user.EmailAlreadyExistsException;
import architecture.community.user.Role;
import architecture.community.user.RoleAlreadyExistsException;
import architecture.community.user.RoleManager;
import architecture.community.user.RoleNotFoundException;
import architecture.community.user.User;
import architecture.community.user.UserAlreadyExistsException;
import architecture.community.user.UserManager;
import architecture.community.user.UserNotFoundException;
import architecture.community.user.UserTemplate;
import architecture.community.util.SecurityHelper;
import architecture.community.web.model.ItemList;
import architecture.community.web.model.json.DataSourceRequest;
import architecture.community.web.model.json.Result;
import architecture.ee.service.ConfigService;
import architecture.ee.util.StringUtils;

@Controller("community-data-v1-mgmt-security-controller")
@RequestMapping("/data/api/mgmt/v1/security")
public class SecurityMgmtDataController {

	private Logger logger = LoggerFactory.getLogger(getClass());
	@Inject
	@Qualifier("communityAclService")
	private JdbcCommunityAclService communityAclService;
	
	@Inject
	@Qualifier("userManager")
	private UserManager userManager;
	
	@Inject
	@Qualifier("roleManager")
	private RoleManager roleManager;
	
	@Inject
	@Qualifier("configService")
	private ConfigService configService;
	
	public SecurityMgmtDataController() {
	}

	
	/**
	 * USER API 
	******************************************/
	@Secured({ "ROLE_ADMINISTRATOR" })
	@RequestMapping(value = "/users/list.json", method = { RequestMethod.POST, RequestMethod.GET})
	@ResponseBody
	public ItemList getUsers (
		@RequestBody DataSourceRequest dataSourceRequest,
		NativeWebRequest request) throws NotFoundException {	
				 
		if( dataSourceRequest.getPageSize() == 0)
			dataSourceRequest.setPageSize(15);
		
		int totalCount = userManager.getUserCount();		
		
		List<User> users = userManager.getUsers(dataSourceRequest.getSkip(), dataSourceRequest.getPageSize() );		
		
		return new ItemList(users, totalCount);
	}
	
	@Secured({ "ROLE_ADMINISTRATOR" })
	@RequestMapping(value = "/users/save-or-update.json", method = { RequestMethod.POST, RequestMethod.GET })
    @ResponseBody
    public Result updateRole(@RequestBody CommunityUser user , NativeWebRequest request) throws  UserNotFoundException, UserAlreadyExistsException, EmailAlreadyExistsException { 
		
		logger.debug("Save or update user {} ", user.toString());
		
		if( user.getUserId() > 0 ) {
			userManager.updateUser(user);
		}else {
			userManager.createUser(user);
		}
		return Result.newResult();
    }
	
	@Secured({ "ROLE_ADMINISTRATOR" })
	@RequestMapping(value = "/users/{userId:[\\p{Digit}]+}/roles/list.json", method = { RequestMethod.POST, RequestMethod.GET })
    @ResponseBody
    public ItemList getUserRoles(@PathVariable Long userId, NativeWebRequest request) throws  UserNotFoundException, UserAlreadyExistsException {
		
		ItemList result = new ItemList();
		if( userId > 0 ) {
			User user = userManager.getUser(userId);
			List<Role> items = roleManager.getFinalUserRoles(user.getUserId());
			result.setItems(items);
			result.setTotalCount(items.size());
		}
	
		return result;
    }

	@Secured({ "ROLE_ADMINISTRATOR" })
	@RequestMapping(value = "/users/{userId:[\\p{Digit}]+}/roles/save-or-update.json", method = { RequestMethod.POST, RequestMethod.GET })
    @ResponseBody
    public Result saveOrUpdateUserRoles(@PathVariable Long userId, @RequestBody List<DefaultRole> roles, NativeWebRequest request) throws  UserNotFoundException, UserAlreadyExistsException, RoleNotFoundException {
		
		//ItemList result = new ItemList();
		if( userId > 0 && roles.size() > 0 ) {
			User user = userManager.getUser(userId);
			
			List<Role> list1 = new ArrayList<Role> (roles.size());
			for(Role r : roles ) {
				list1.add( roleManager.getRole(r.getRoleId() ));
			}
			
			List<Role> list2 = roleManager.getFinalUserRoles(user.getUserId());
			
			for( Role r : list2) {
				if( !list1.contains(r) ) {
					roleManager.revokeRole(r, user);
				}
			}			
			for( Role r : list1) {
				if( !list2.contains(r)) {
					roleManager.grantRole(r, user);
				}
			}
		}
		
		return Result.newResult();
    }
	
	@Secured({ "ROLE_ADMINISTRATOR" })
	@RequestMapping(value = "/users/{userId:[\\p{Digit}]+}/roles/add.json", method = { RequestMethod.POST, RequestMethod.GET })
    @ResponseBody
    public Result addUserRoles(@PathVariable Long userId, @RequestParam(value = "roleId", defaultValue = "0", required = false) Long roleId, NativeWebRequest request) throws  UserNotFoundException, UserAlreadyExistsException, RoleNotFoundException {
		
		//ItemList result = new ItemList();
		if( userId > 0 && roleId > 0 ) {
			User user = userManager.getUser(userId);
			Role role = roleManager.getRole(roleId);
			roleManager.grantRole(role, user);			
			//List<Role> items = roleManager.getFinalUserRoles(user.getUserId());
			//result.setItems(items);
			//result.setTotalCount(items.size());
		}	
		return Result.newResult();
    }
	
	@Secured({ "ROLE_ADMINISTRATOR" })
	@RequestMapping(value = "/users/{userId:[\\p{Digit}]+}/roles/remove.json", method = { RequestMethod.POST, RequestMethod.GET })
    @ResponseBody
    public Result removeUserRoles(@PathVariable Long userId, @RequestParam(value = "roleId", defaultValue = "0", required = false) Long roleId, NativeWebRequest request) throws  UserNotFoundException, UserAlreadyExistsException, RoleNotFoundException {
		
		//ItemList result = new ItemList();
		if( userId > 0 && roleId > 0 ) {
			User user = userManager.getUser(userId);
			Role role = roleManager.getRole(roleId);
			roleManager.revokeRole(role, user);			
			//List<Role> items = roleManager.getFinalUserRoles(user.getUserId());
			//result.setItems(items);
			//result.setTotalCount(items.size());
		}	
		return Result.newResult();
    }
	
	/**
	 * ROLE API 
	******************************************/
	@Secured({ "ROLE_ADMINISTRATOR" })
	@RequestMapping(value = "/roles/list.json", method = { RequestMethod.POST, RequestMethod.GET})
	@ResponseBody
	public ItemList getRoles (NativeWebRequest request) {					
		List<Role> roles = roleManager.getRoles();		
		return new ItemList(roles, roles.size());	
	}
	
	@Secured({ "ROLE_ADMINISTRATOR" })
	@RequestMapping(value = "/roles/create.json", method = { RequestMethod.POST, RequestMethod.GET })
    @ResponseBody
    public Result createRole(@RequestBody DefaultRole newRole, NativeWebRequest request) throws RoleNotFoundException, RoleAlreadyExistsException { 
		roleManager.createRole(newRole.getName(), newRole.getDescription());
		return Result.newResult();
    }

	@Secured({ "ROLE_ADMINISTRATOR" })
	@RequestMapping(value = "/roles/update.json", method = { RequestMethod.POST, RequestMethod.GET })
    @ResponseBody
    public Result updateRole(@RequestBody DefaultRole newRole, NativeWebRequest request) throws RoleNotFoundException, RoleAlreadyExistsException {
 
		DefaultRole role = (DefaultRole) roleManager.getRole(newRole.getRoleId());
		if (!org.apache.commons.lang3.StringUtils.equals(newRole.getName(), role.getName())) {
		    role.setName(newRole.getName());
		} 
		if (!org.apache.commons.lang3.StringUtils.equals(newRole.getDescription(), role.getDescription())) {
		    role.setDescription(newRole.getDescription());
		}
		roleManager.updateRole(role);
		return Result.newResult();
    }
	
	
	/**
	 * PERMISSIONS API 
	 ******************************************/
	
	@Secured({ "ROLE_ADMINISTRATOR" })
	@RequestMapping(value = "/permissions/list.json", method = { RequestMethod.POST, RequestMethod.GET})
	@ResponseBody
	public ItemList getPermissions (NativeWebRequest request) {	
		
		List<CommunityPermissions> list = new ArrayList<CommunityPermissions>();		
		for( CommunityPermissions p : CommunityPermissions.values() ) {
			list.add(p);
		}		
		return new ItemList(list, list.size());		
	}
	
	
	/**
	 * 
	 * @param objectType
	 * @param objectId
	 * @param request
	 * @return
	 */
	
	@Secured({ "ROLE_ADMINISTRATOR" })
	@RequestMapping(value = "/permissions/{objectType:[\\p{Digit}]+}/{objectId:[\\p{Digit}]+}/list.json", method = { RequestMethod.POST, RequestMethod.GET})
	@ResponseBody
	public ItemList getAssignedPermissions (@PathVariable Integer objectType, @PathVariable Long objectId, NativeWebRequest request) {			
		List<AccessControlEntry> entries = communityAclService.getAsignedPermissions(Models.valueOf(objectType).getObjectClass(), objectId);
		List<ObjectAccessControlEntry> list = new ArrayList<ObjectAccessControlEntry>(entries.size());
		for( AccessControlEntry entry : entries )
			list.add(new ObjectAccessControlEntry(entry));		
		return new ItemList(list, list.size());		
	}
	
	@Secured({ "ROLE_ADMINISTRATOR" })
	@RequestMapping(value = "/permissions/{objectType:[\\p{Digit}]+}/{objectId:[\\p{Digit}]+}/add.json", method = { RequestMethod.POST})
	@ResponseBody
	public Result addPermission(@PathVariable Integer objectType, @PathVariable Long objectId, @RequestBody ObjectAccessControlEntry entry ) throws UserNotFoundException, RoleNotFoundException {	
		Result result = Result.newResult();
		if(org.apache.commons.lang3.StringUtils.equals(entry.getGrantedAuthority(), "USER")){
			if(StringUtils.isNullOrEmpty(entry.getGrantedAuthorityOwner()) || org.apache.commons.lang3.StringUtils.equals(entry.getGrantedAuthorityOwner(), SecurityHelper.ANONYMOUS.getUsername()) ) {
				
				communityAclService.addAnonymousPermission(Models.valueOf(objectType).getObjectClass(), objectId, CommunityPermissions.getPermissionByName(entry.getPermission()));
				
			}else {
				User user = userManager.getUser(entry.getGrantedAuthorityOwner());
			
				communityAclService.addPermission(Models.valueOf(objectType).getObjectClass(), objectId, user, CommunityPermissions.getPermissionByName(entry.getPermission()));
			
			}		
		}else if (org.apache.commons.lang3.StringUtils.equals(entry.getGrantedAuthority(), "ROLE")){
			Role role = roleManager.getRole(entry.getGrantedAuthorityOwner());
			communityAclService.addPermission(Models.valueOf(objectType).getObjectClass(), objectId, role, CommunityPermissions.getPermissionByName(entry.getPermission()));
		}
		return result;
	}
	
	@Secured({ "ROLE_ADMINISTRATOR" })
	@RequestMapping(value = "/permissions/{objectType:[\\p{Digit}]+}/{objectId:[\\p{Digit}]+}/remove.json", method = { RequestMethod.POST})
	@ResponseBody
	public Result removePermission(@PathVariable Integer objectType, @PathVariable Long objectId, @RequestBody ObjectAccessControlEntry entry ) throws UserNotFoundException, RoleNotFoundException {		
		Result result = Result.newResult();
		if(org.apache.commons.lang3.StringUtils.equals(entry.getGrantedAuthority(), "USER")){
			if(StringUtils.isNullOrEmpty(entry.getGrantedAuthorityOwner()) || org.apache.commons.lang3.StringUtils.equals(entry.getGrantedAuthorityOwner(), SecurityHelper.ANONYMOUS.getUsername()) ) {
				communityAclService.removeAnonymousPermission(Models.valueOf(objectType).getObjectClass(), objectId, CommunityPermissions.getPermissionByName(entry.getPermission()));
			}else {
				User user = userManager.getUser(entry.getGrantedAuthorityOwner());
				communityAclService.removePermission(Models.valueOf(objectType).getObjectClass(), objectId, user, CommunityPermissions.getPermissionByName(entry.getPermission()));
			}		
		}else if (org.apache.commons.lang3.StringUtils.equals(entry.getGrantedAuthority(), "ROLE")){
			Role role = roleManager.getRole(entry.getGrantedAuthorityOwner());
			communityAclService.removePermission(Models.valueOf(objectType).getObjectClass(), objectId, role, CommunityPermissions.getPermissionByName(entry.getPermission()));
		}
		return result;
	}

}
