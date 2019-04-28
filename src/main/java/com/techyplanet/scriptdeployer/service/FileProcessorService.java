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
		String oneTimeFilePattern = appSettings.getOneTimeFilePattern();
		if (StringUtils.isBlank(oneTimeFilePattern)) {
			LOGGER.info(
					"<-- Skipping --> No pattern defined for sequential scripts [app.scripts.oneTime.file.pattern], hence skipping one time scripts deployment.");
			return;
		}
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
			Integer sequence = CommonUtils.getFileSequence(oneTimeFileRegexPattern, fileName);

			Date currentDate = new Date();
			ScriptHistory sameSequenceEntry = scriptHistoryRepository.findBySequenceAndType(sequence, "SEQ");
			if (sameSequenceEntry == null) {
				LOGGER.info("<-- first run --> {}", relativePath);
				executeScript(oneTimeFilePath);
				scriptHistoryRepository
						.save(new ScriptHistory(relativePath, "SEQ", sequence, checksum, currentDate, currentDate));
			} else if (!relativePath.equals(sameSequenceEntry.getPath())) {
				throw new RuntimeException(String.format(
						"[%s] file was executed in previous runs with same sequence number, [%s] can't use same sequence number.",
						sameSequenceEntry.getPath(), relativePath));
			} else if (!checksum.equals(sameSequenceEntry.getChecksum())) {
				if ("reset-hash".equals(appSettings.getFileModifyError())) {
					sameSequenceEntry.setChecksum(checksum);
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
		String repeatableFilePattern = appSettings.getRepeatableFilePattern();
		if (StringUtils.isBlank(repeatableFilePattern)) {
			LOGGER.info(
					"<-- Skipping --> No pattern defined for rerunnable scripts [app.scripts.repeatable.file.pattern], hence skipping re-runnable scripts deployment.");
			return;
		}
		String repeatableFileRegexPattern = repeatableFilePattern.replace("<seq_num>", "(\\d+)");
		IOFileFilter repeatableFilesFilter = new RegexFileFilter(repeatableFileRegexPattern);
		List<File> repeatableFiles = (List<File>) FileUtils.listFiles(scriptsDir, repeatableFilesFilter,
				TrueFileFilter.INSTANCE);

		Collections.sort(repeatableFiles, CommonUtils.scriptPrioritySorter(repeatableFileRegexPattern, true));

		for (File repeatableFile : repeatableFiles) {
			String repeatableFilePath = repeatableFile.getAbsolutePath();
			String fileName = repeatableFile.getName();

			String relativePath = scriptsDir.toURI().relativize(repeatableFile.toURI()).getPath();
			String checksum = CommonUtils.generateFileChecksum(repeatableFile.toPath());
			Integer sequence = CommonUtils.getFileSequence(repeatableFileRegexPattern, fileName);

			Date currentDate = new Date();
			ScriptHistory previousEntry = scriptHistoryRepository.findByPath(relativePath);
			if (previousEntry == null) {
				LOGGER.info("<-- first run --> {}", relativePath);
				executeScript(repeatableFilePath);
				scriptHistoryRepository
						.save(new ScriptHistory(relativePath, "REP", sequence, checksum, currentDate, currentDate));
			} else if (!checksum.equals(previousEntry.getChecksum())) {
				previousEntry.setChecksum(checksum);
				previousEntry.setUpdateDate(currentDate);
				LOGGER.info("<-- re-run --> {}", relativePath);
				executeScript(repeatableFilePath);
				scriptHistoryRepository.save(previousEntry);
			} else {
				LOGGER.info("<-- Skipping  --> {}", relativePath);
			}

		}
	}

	public void processRunAllTimeFiles(File scriptsDir) {

		LOGGER.info("=========== checking scripts for each run =======");
		String allTimeFilePattern = appSettings.getAllTimeFilePattern();
		if (StringUtils.isBlank(allTimeFilePattern)) {
			LOGGER.info(
					"<-- Skipping --> No pattern defined for everytime scripts [app.scripts.run.always.file.pattern], hence skipping everytime scripts deployment.");
			return;
		}
		String allTimeFileRegexPattern = allTimeFilePattern.replace("<seq_num>", "(\\d+)");
		IOFileFilter allTimeFilesFilter = new RegexFileFilter(allTimeFileRegexPattern);
		List<File> allTimeFiles = (List<File>) FileUtils.listFiles(scriptsDir, allTimeFilesFilter,
				TrueFileFilter.INSTANCE);

		Collections.sort(allTimeFiles, CommonUtils.scriptPrioritySorter(allTimeFileRegexPattern, true));

		for (File allTimeFile : allTimeFiles) {
			String allTimeFilePath = allTimeFile.getAbsolutePath();
			String fileName = allTimeFile.getName();

			String relativePath = scriptsDir.toURI().relativize(allTimeFile.toURI()).getPath();
			String checksum = CommonUtils.generateFileChecksum(allTimeFile.toPath());
			Integer sequence = CommonUtils.getFileSequence(allTimeFileRegexPattern, fileName);

			Date currentDate = new Date();
			ScriptHistory previousEntry = scriptHistoryRepository.findByPath(relativePath);
			if (previousEntry == null) {
				LOGGER.info("<-- first run --> {}", relativePath);
				executeScript(allTimeFilePath);
				scriptHistoryRepository
						.save(new ScriptHistory(relativePath, "REP", sequence, checksum, currentDate, currentDate));
			} else {
				previousEntry.setChecksum(checksum);
				previousEntry.setUpdateDate(currentDate);
				LOGGER.info("<-- re-run --> {}", relativePath);
				executeScript(allTimeFilePath);
				scriptHistoryRepository.save(previousEntry);
			}

		}
	}

	private void executeScript(String srcScript) {
		String script = srcScript;
		String consoleCommand = appSettings.getConsoleCommand();
		String loggingPattern = appSettings.getConsoleCommandLogging();

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
			CommandUtils.executeAndGetOutput(commandToExecute);
		}
	}
}
