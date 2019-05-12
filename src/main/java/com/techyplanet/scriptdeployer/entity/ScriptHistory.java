package com.techyplanet.scriptdeployer.entity;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity(name = "Schema_History")
public class ScriptHistory {

	@Id
	private String path;
	private String type;
	private Long sequence;
	private Long version;
	private String checksum;
	private String pattern;
	private Date createDate;
	private Date updateDate;

	public ScriptHistory() {
		super();
	}

	public ScriptHistory(String path, String type, Long sequence, Long version, String checksum, String pattern,
			Date createDate, Date updateDate) {
		super();
		this.path = path;
		this.type = type;
		this.sequence = sequence;
		this.version = version;
		this.checksum = checksum;
		this.pattern = pattern;
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

	public Long getSequence() {
		return sequence;
	}

	public void setSequence(Long sequence) {
		this.sequence = sequence;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	public String getChecksum() {
		return checksum;
	}

	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
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
		return "ScriptHistory [path=" + path + ", type=" + type + ", sequence=" + sequence + ", version=" + version
				+ ", checksum=" + checksum + ", pattern=" + pattern + ", createDate=" + createDate + ", updateDate="
				+ updateDate + "]";
	}

}
