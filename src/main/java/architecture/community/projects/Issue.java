package architecture.community.projects;

import java.util.Date;

import architecture.community.model.ModelObjectAware;
import architecture.community.user.User;

public interface Issue extends ModelObjectAware { 

	public abstract long getIssueId();

	public abstract User getRepoter(); 
	
	public abstract void setRepoter(User repoter) ; 

	public abstract void setIssueId(long issueId) ;

	public abstract String getSummary() ;

	public abstract void setSummary(String summary);

	public abstract String getDescription() ;

	public abstract void setDescription(String description) ;
	 
	public abstract Date getCreationDate() ;
  
	public abstract void setCreationDate(Date creationDate) ;
 
	public abstract Date getModifiedDate();
 
	public abstract void setModifiedDate(Date modifiedDate) ;

	public abstract String getIssueType() ;

	public abstract void setIssueType(String issueType) ;
 
	public abstract Date getDueDate() ;
 
	public abstract void setDueDate(Date dueDate);  
 
	public abstract User getAssignee() ;
 
	public abstract void setAssignee(User assignee); 
	
	public abstract String getPriority() ;
	
	public abstract void setPriority(String priority) ;

	public abstract String getComponent();

	public abstract void setComponent(String component) ; 
	
	public abstract String getResolution(); 
	
	public abstract void setResolution(String resolution) ; 
	 
	public abstract Date getResolutionDate() ;
 
	public abstract void setResolutionDate(Date resolutionDate) ;
	
	public abstract String getStatus() ;

	public abstract void setStatus(String status) ;
	
}
