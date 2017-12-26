package architecture.community.projects;

import java.util.Calendar;
import java.util.Date;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import architecture.community.model.ModelObjectAwareSupport;
import architecture.community.model.json.JsonDateDeserializer;
import architecture.community.model.json.JsonDateSerializer;
import architecture.community.model.json.JsonUserDeserializer;
import architecture.community.user.User;

public class DefaultIssue extends ModelObjectAwareSupport implements Issue {
	
	
	private User repoter;
	
	private User assignee;
	
	// 오류 , 기술지원, ..정기정검...
	private String issueType;
	
	// 접수일자
	// 작업일자 계획  시작 - 종료
	// 작업완료 실적
	// 작업시간 계획 
	// 작업시간 실적  
	
	private long issueId;
	
	private String summary;
	
	private String description;
	
	// 0(하), 1(중), 2 (상)
	private String priority;
	
	// 0(하), 1(중), 2 (상)
	private String component;
	
		
	// 예정일 
	private Date dueDate;
	
	private Date creationDate;
	
	private Date modifiedDate;
	
	public DefaultIssue() {
		super(-1, -1L);
		this.issueId = -1L;
		this.issueType = null;
		this.summary = null;
		this.description = null;
		this.creationDate = Calendar.getInstance().getTime();
		this.modifiedDate = creationDate;
		this.dueDate = null;
		this.assignee = null;
		this.repoter = null;
	}

	
	public DefaultIssue(long issueId) {
		super(-1, -1L);
		this.issueId = issueId;
		this.issueType = null;
		this.summary = null;
		this.description = null;
		this.creationDate = Calendar.getInstance().getTime();
		this.modifiedDate = creationDate;
		this.dueDate = null;
		this.assignee = null;
		this.repoter = null;
	}
	
	public DefaultIssue(int objectType, long objectId) {
		super(objectType, objectId);
		this.issueId = -1L;
		this.issueType = null;
		this.summary = null;
		this.description = null;
		this.creationDate = Calendar.getInstance().getTime();
		this.modifiedDate = creationDate;
		this.dueDate = null;
		this.assignee = null;
		this.repoter = null;
	}
	
	public DefaultIssue(int objectType, long objectId, User repoter) {
		super(objectType, objectId);
		this.issueId = -1L;
		this.issueType = null;
		this.summary = null;
		this.description = null;
		this.creationDate = Calendar.getInstance().getTime();
		this.modifiedDate = creationDate;
		this.dueDate = null;
		this.assignee = null;
		this.repoter = repoter;
	}
	
	
	public User getRepoter() {
		return repoter;
	}


	@JsonDeserialize(using = JsonUserDeserializer.class)
	public void setRepoter(User repoter) {
		this.repoter = repoter;
	}



	public long getIssueId() {
		return issueId;
	}

	public void setIssueId(long issueId) {
		this.issueId = issueId;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	

	/**
	 * @return creationDate
	 */
	@JsonSerialize(using = JsonDateSerializer.class)
	public Date getCreationDate() {
		return creationDate;
	}

	/**
	 * @param creationDate
	 *            설정할 creationDate
	 */
	@JsonDeserialize(using = JsonDateDeserializer.class)
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	/**
	 * @return modifiedDate
	 */
	@JsonSerialize(using = JsonDateSerializer.class)
	public Date getModifiedDate() {
		return modifiedDate;
	}

	/**
	 * @param modifiedDate
	 *            설정할 modifiedDate
	 */
	@JsonDeserialize(using = JsonDateDeserializer.class)
	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	public String getIssueType() {
		return issueType;
	}

	public void setIssueType(String issueType) {
		this.issueType = issueType;
	}

	@JsonSerialize(using = JsonDateSerializer.class)
	public Date getDueDate() {
		return dueDate;
	}

	@JsonDeserialize(using = JsonDateDeserializer.class)
	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}



	public User getAssignee() {
		return assignee;
	}


	@JsonDeserialize(using = JsonUserDeserializer.class)
	public void setAssignee(User assignee) {
		this.assignee = assignee;
	}



	public String getPriority() {
		return priority;
	}



	public void setPriority(String priority) {
		this.priority = priority;
	}



	public String getComponent() {
		return component;
	}



	public void setComponent(String component) {
		this.component = component;
	}

}