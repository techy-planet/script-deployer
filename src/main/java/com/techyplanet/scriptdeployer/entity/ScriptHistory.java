package com.techyplanet.scriptdeployer.entity;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity(name = "Schema_History")
public class ScriptHistory {

	@Id
	private String path;
	private String type;
	private Integer sequence;
	private String checksum;
	private Date createDate;
	private Date updateDate;

	public ScriptHistory() {
		super();
	}

	public ScriptHistory(String path, String type, Integer sequence, String checksum, Date createDate,
			Date updateDate) {
		super();
		this.path = path;
		this.type = type;
		this.sequence = sequence;
		this.checksum = checksum;
		this.createDate = createDate;
		this.updateDate = updateDate;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Integer getSequence() {
		return sequence;
	}

	public void setSequence(Integer sequence) {
		this.sequence = sequence;
	}

	public String getChecksum() {
		return checksum;
	}

	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

	@Override
	public String toString() {
		return "ScriptHistory [path=" + path + ", type=" + type + ", sequence=" + sequence + ", checksum=" + checksum
				+ ", createDate=" + createDate + ", updateDate=" + updateDate + "]";
	}

}
