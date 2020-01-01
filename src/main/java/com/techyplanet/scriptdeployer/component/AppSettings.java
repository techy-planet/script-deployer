package com.techyplanet.scriptdeployer.component;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AppSettings {

	@Value("${app.exception.trace}")
	private boolean traceRequired;

	@Value("${app.scripts.location}")
	private String scriptsLocation;

	@Value("${app.scripts.oneTime.file.pattern}")
	private String oneTimeFilePattern;

	@Value("${app.scripts.repeatable.file.pattern}")
	private String repeatableFilePattern;

	@Value("${app.scripts.pre.run.file.pattern}")
	private String preRunFilePattern;

	@Value("${app.scripts.post.run.file.pattern}")
	private String postRunFilePattern;

	@Value("${app.scripts.file.pattern.conflict}")
	private String filePatternConflict;

	@Value("${app.script.execute.command}")
	private String consoleCommand;

	@Value("${app.script.execute.command.log}")
	private String consoleCommandLogging;

	@Value("${app.script.execute.command.output}")
	private boolean consoleCommandOutputEnabled;

	@Value("${app.script.execute.stopOnfail}")
	private boolean stopOnScriptFail;

	@Value("${app.script.sequence.file.modified.error}")
	private String fileModifyError;

	@Value("${app.script.template.variables}")
	private String scriptDBVariables;

	@Value("${app.scripts.deployer.home}/logs")
	private String logDir;

	@Value("${app.scripts.db.metadata.spool}")
	private boolean scriptMetadataSpoolEnabled;

	public boolean isTraceRequired() {
		return traceRequired;
	}

	public String getScriptsLocation() {
		return scriptsLocation;
	}

	public String getOneTimeFilePattern() {
		return oneTimeFilePattern;
	}

	public String getRepeatableFilePattern() {
		return repeatableFilePattern;
	}

	public String getConsoleCommand() {
		return consoleCommand;
	}

	public String getConsoleCommandLogging() {
		return consoleCommandLogging;
	}

	public boolean isConsoleCommandOutputEnabled() {
		return consoleCommandOutputEnabled;
	}

	public String getFileModifyError() {
		return fileModifyError;
	}

	public String getPreRunFilePattern() {
		return preRunFilePattern;
	}

	public String getPostRunFilePattern() {
		return postRunFilePattern;
	}

	public String getFilePatternConflict() {
		return filePatternConflict;
	}

	public String getScriptDBVariables() {
		return scriptDBVariables;
	}

	public boolean isStopOnScriptFail() {
		return stopOnScriptFail;
	}

	public String getLogDir() {
		return logDir;
	}

	public boolean isScriptMetadataSpoolEnabled() {
		return scriptMetadataSpoolEnabled;
	}

}
