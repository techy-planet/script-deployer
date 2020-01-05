package com.techyplanet.scriptdeployer.service;

import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.techyplanet.scriptdeployer.component.AppSettings;
import com.techyplanet.scriptdeployer.entity.ScriptHistory;
import com.techyplanet.scriptdeployer.repository.ScriptHistoryRepository;
import com.techyplanet.scriptdeployer.utils.CommandUtils;
import com.techyplanet.scriptdeployer.utils.CommonUtils;

@Service
public class FileProcessorService {

	private static final Logger LOGGER = LoggerFactory.getLogger(FileProcessorService.class);

	@Autowired
	private AppSettings appSettings;

	@Autowired
	private StringSubstitutor dbVariablesSubstitutor;

	@Autowired
	private ScriptHistoryRepository scriptHistoryRepository;

	public void processOneTimeFiles(File scriptsDir) {

		LOGGER.info("=========== checking one time scripts ===========");
		String oneTimeFilePatternProp = appSettings.getOneTimeFilePattern();
		if (StringUtils.isBlank(oneTimeFilePatternProp)) {
			LOGGER.info(
					"<-- Skipping --> No pattern defined for sequential scripts [app.scripts.oneTime.file.pattern], hence skipping one time scripts deployment.");
			return;
		}

		String[] oneTimeFilePatterns = oneTimeFilePatternProp.split(appSettings.getFilePatternDelimiter());
		for (String oneTimeFilePattern : oneTimeFilePatterns) {
			processOneTimeFile(scriptsDir, oneTimeFilePattern);
		}
	}

	private void processOneTimeFile(File scriptsDir, String oneTimeFilePattern) {
		if (!oneTimeFilePattern.contains("<seq_num>")) {
			throw new RuntimeException(String
					.format("'<seq_num>' must be defined as part of one time file pattern [%s]", oneTimeFilePattern));
		}

		LOGGER.info("<-- Pattern Lookup  --> {}", oneTimeFilePattern);

		String oneTimeFileRegexPattern = oneTimeFilePattern.replace("<seq_num>", "(\\d+)");
		IOFileFilter oneTimeFilesFilter = new RegexFileFilter(oneTimeFileRegexPattern);
		List<File> oneTimeFiles = (List<File>) FileUtils.listFiles(scriptsDir, oneTimeFilesFilter,
				TrueFileFilter.INSTANCE);

		Collections.sort(oneTimeFiles, CommonUtils.scriptPrioritySorter(oneTimeFileRegexPattern, false));

		for (File oneTimeFile : oneTimeFiles) {
			String oneTimeFilePath = oneTimeFile.getAbsolutePath();
			String fileName = oneTimeFile.getName();

			String relativePath = scriptsDir.toURI().relativize(oneTimeFile.toURI()).getPath();
			String checksum = CommonUtils.generateFileChecksum(oneTimeFile.toPath());
			Long sequence = CommonUtils.getFileSequence(oneTimeFileRegexPattern, fileName);

			Date currentDate = new Date();
			ScriptHistory sameSequenceEntry = scriptHistoryRepository.findBySequenceAndPattern(sequence,
					oneTimeFilePattern);
			ScriptHistory sameFileEntry = scriptHistoryRepository.findByPath(relativePath);

			if (sameSequenceEntry == null) {
				if (sameFileEntry != null && "error".equalsIgnoreCase(appSettings.getFilePatternConflict())) {
					throw new RuntimeException(String.format(
							"File [%s] execution with pattern [%s] conflicts with previous execution of file having pattern [%s]",
							relativePath, oneTimeFilePattern, sameFileEntry.getPattern()));
				}

				LOGGER.info("<-- first run --> {}", relativePath);
				executeScript(oneTimeFilePath);
				scriptHistoryRepository.save(new ScriptHistory(relativePath, "SEQ", sequence, 1L, checksum,
						oneTimeFilePattern, currentDate, currentDate));
			} else if (!relativePath.equals(sameSequenceEntry.getPath())) {
				throw new RuntimeException(String.format(
						"[%s] file was executed in previous runs with same sequence number, [%s] can't use same sequence number.",
						sameSequenceEntry.getPath(), relativePath));
			} else if (!checksum.equals(sameSequenceEntry.getChecksum())) {
				if ("reset-hash".equals(appSettings.getFileModifyError())) {
					sameSequenceEntry.setChecksum(checksum);
					sameSequenceEntry.setVersion(sameSequenceEntry.getVersion() + 1);
					sameSequenceEntry.setUpdateDate(currentDate);
					LOGGER.info("<-- reset-hash --> {}", relativePath);
					scriptHistoryRepository.save(sameSequenceEntry);
				} else {
					throw new RuntimeException(String.format(
							"File modification is not allowed for sequential file [%s], it can't be re-deployed with changes.",
							relativePath));
				}
			} else {
				LOGGER.info("<-- Skipping  --> {}", relativePath);
			}

		}
	}

	public void processRepeatableFiles(File scriptsDir) {

		LOGGER.info("=========== checking repeatable scripts =========");
		String repeatableFilePatternProp = appSettings.getRepeatableFilePattern();
		if (StringUtils.isBlank(repeatableFilePatternProp)) {
			LOGGER.info(
					"<-- Skipping --> No pattern defined for rerunnable scripts [app.scripts.repeatable.file.pattern], hence skipping re-runnable scripts deployment.");
			return;
		}

		String[] repeatableFilePatterns = repeatableFilePatternProp.split(appSettings.getFilePatternDelimiter());
		for (String repeatableFilePattern : repeatableFilePatterns) {
			processRepeatableFile(scriptsDir, repeatableFilePattern);
		}
	}

	private void processRepeatableFile(File scriptsDir, String repeatableFilePattern) {

		boolean seqNumApplicable = false;
		if (repeatableFilePattern.contains("<seq_num>")) {
			seqNumApplicable = true;
		}

		LOGGER.info("<-- Pattern Lookup  --> {}", repeatableFilePattern);

		String repeatableFileRegexPattern = repeatableFilePattern.replace("<seq_num>", "(\\d+)");
		IOFileFilter repeatableFilesFilter = new RegexFileFilter(repeatableFileRegexPattern);
		List<File> repeatableFiles = (List<File>) FileUtils.listFiles(scriptsDir, repeatableFilesFilter,
				TrueFileFilter.INSTANCE);

		Collections.sort(repeatableFiles,
				CommonUtils.scriptPrioritySorter(repeatableFileRegexPattern, seqNumApplicable, true));

		for (File repeatableFile : repeatableFiles) {
			String repeatableFilePath = repeatableFile.getAbsolutePath();
			String fileName = repeatableFile.getName();

			String relativePath = scriptsDir.toURI().relativize(repeatableFile.toURI()).getPath();
			String checksum = CommonUtils.generateFileChecksum(repeatableFile.toPath());
			Long sequence = CommonUtils.getFileSequence(repeatableFileRegexPattern, fileName, seqNumApplicable);

			Date currentDate = new Date();
			ScriptHistory previousEntry = scriptHistoryRepository.findByPath(relativePath);
			if (previousEntry == null) {
				LOGGER.info("<-- first run --> {}", relativePath);
				executeScript(repeatableFilePath);
				scriptHistoryRepository.save(new ScriptHistory(relativePath, "REP", sequence, 1L, checksum,
						repeatableFilePattern, currentDate, currentDate));
			} else if ("error".equalsIgnoreCase(appSettings.getFilePatternConflict())
					&& !repeatableFilePattern.equals(previousEntry.getPattern())) {
				throw new RuntimeException(String.format(
						"File [%s] execution with pattern [%s] conflicts with previous execution of file having pattern [%s]",
						relativePath, repeatableFilePattern, previousEntry.getPattern()));
			} else if (!checksum.equals(previousEntry.getChecksum())) {
				previousEntry.setChecksum(checksum);
				previousEntry.setPattern(repeatableFilePattern);
				previousEntry.setVersion(previousEntry.getVersion() + 1);
				previousEntry.setUpdateDate(currentDate);
				LOGGER.info("<-- run --> {}", relativePath);
				executeScript(repeatableFilePath);
				scriptHistoryRepository.save(previousEntry);
			} else {
				LOGGER.info("<-- Skipping  --> {}", relativePath);
			}

		}
	}

	public void processPreRunFiles(File scriptsDir) {
		LOGGER.info("=========== checking pre scripts for each run =======");
		String preRunFilePattern = appSettings.getPreRunFilePattern();
		if (StringUtils.isBlank(preRunFilePattern)) {
			LOGGER.info("<-- Skipping --> No pattern defined for scripts [app.scripts.pre.run.file.pattern]");
			return;
		}

		String[] preRunFilePatterns = preRunFilePattern.split(appSettings.getFilePatternDelimiter());
		for (String preRunFilePatternEntry : preRunFilePatterns) {
			processRunAllTimeFile(scriptsDir, preRunFilePatternEntry);
		}

	}

	public void processPostRunFiles(File scriptsDir) {

		LOGGER.info("=========== checking post scripts for each run =======");
		String postRunFilePattern = appSettings.getPostRunFilePattern();
		if (StringUtils.isBlank(postRunFilePattern)) {
			LOGGER.info("<-- Skipping --> No pattern defined for scripts [app.scripts.post.run.file.pattern]");
			return;
		}

		String[] postRunFilePatterns = postRunFilePattern.split(appSettings.getFilePatternDelimiter());
		for (String postRunFilePatternEntry : postRunFilePatterns) {
			processRunAllTimeFile(scriptsDir, postRunFilePatternEntry);
		}
	}

	private void processRunAllTimeFile(File scriptsDir, String allTimeFilePattern) {

		boolean seqNumApplicable = false;
		if (allTimeFilePattern.contains("<seq_num>")) {
			seqNumApplicable = true;
		}

		LOGGER.info("<-- Pattern Lookup  --> {}", allTimeFilePattern);

		String allTimeFileRegexPattern = allTimeFilePattern.replace("<seq_num>", "(\\d+)");
		IOFileFilter allTimeFilesFilter = new RegexFileFilter(allTimeFileRegexPattern);
		List<File> allTimeFiles = (List<File>) FileUtils.listFiles(scriptsDir, allTimeFilesFilter,
				TrueFileFilter.INSTANCE);

		Collections.sort(allTimeFiles,
				CommonUtils.scriptPrioritySorter(allTimeFileRegexPattern, seqNumApplicable, true));

		for (File allTimeFile : allTimeFiles) {
			String allTimeFilePath = allTimeFile.getAbsolutePath();
			String fileName = allTimeFile.getName();

			String relativePath = scriptsDir.toURI().relativize(allTimeFile.toURI()).getPath();
			String checksum = CommonUtils.generateFileChecksum(allTimeFile.toPath());
			Long sequence = CommonUtils.getFileSequence(allTimeFileRegexPattern, fileName, seqNumApplicable);

			Date currentDate = new Date();
			ScriptHistory previousEntry = scriptHistoryRepository.findByPath(relativePath);
			if (previousEntry == null) {
				LOGGER.info("<-- first run --> {}", relativePath);
				executeScript(allTimeFilePath);
				scriptHistoryRepository.save(new ScriptHistory(relativePath, "REP", sequence, 1L, checksum,
						allTimeFilePattern, currentDate, currentDate));
			} else if ("error".equalsIgnoreCase(appSettings.getFilePatternConflict())
					&& !allTimeFilePattern.equals(previousEntry.getPattern())) {
				throw new RuntimeException(String.format(
						"File [%s] execution with pattern [%s] conflicts with previous execution of file having pattern [%s]",
						relativePath, allTimeFilePattern, previousEntry.getPattern()));
			} else {
				LOGGER.info("<-- run --> {}", relativePath);
				executeScript(allTimeFilePath);
				if (!checksum.equals(previousEntry.getChecksum())) {
					previousEntry.setChecksum(checksum);
					previousEntry.setVersion(previousEntry.getVersion() + 1);
					previousEntry.setUpdateDate(currentDate);
					scriptHistoryRepository.save(previousEntry);
				}
			}

		}
	}

	private void executeScript(String srcScript) {
		String script = srcScript;
		String consoleCommand = appSettings.getConsoleCommand();
		String loggingPattern = appSettings.getConsoleCommandLogging();
		boolean stopOnScriptFail = appSettings.isStopOnScriptFail();

		LOGGER.info("<-- Executing --> {}", loggingPattern.replace("<script>", srcScript));

		String scriptDBVariables = appSettings.getScriptDBVariables();

		if (StringUtils.isNotBlank(scriptDBVariables)) {
			String logDir = appSettings.getLogDir();
			script = logDir + "/dbTmpFile.txt";
			try (PrintWriter pw = new PrintWriter(script);
					Stream<String> lines = Files.lines(new File(srcScript).toPath(), StandardCharsets.UTF_8)) {
				lines.forEach(line -> {
					pw.println(dbVariablesSubstitutor.replace(line));
				});
			} catch (Exception ex) {
				throw new RuntimeException("DB script place holder replacement failed.", ex);
			}
		}

		String commandToExecute = consoleCommand.replace("<script>", new File(script).getPath());

		LOGGER.debug("final command to execute --> [{}]", commandToExecute);
		if (appSettings.isConsoleCommandOutputEnabled()) {
			CommandUtils.execute(commandToExecute);
		} else {
			CommandUtils.executeAndGetOutput(commandToExecute, stopOnScriptFail);
		}
	}
}
