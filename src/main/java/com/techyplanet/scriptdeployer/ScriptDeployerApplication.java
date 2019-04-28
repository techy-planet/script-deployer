package com.techyplanet.scriptdeployer;

import java.io.File;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.techyplanet.scriptdeployer.component.AppSettings;
import com.techyplanet.scriptdeployer.component.DBSpooler;
import com.techyplanet.scriptdeployer.service.FileProcessorService;
import com.techyplanet.scriptdeployer.validator.VariablesValidator;

@SpringBootApplication
public class ScriptDeployerApplication implements CommandLineRunner {

	private static final Logger LOGGER = LoggerFactory.getLogger(ScriptDeployerApplication.class);

	@Autowired
	private AppSettings appSettings;

	@Autowired
	private FileProcessorService fileProcessorService;

	@Autowired
	private VariablesValidator variablesValidator;
	
	@Autowired
	private DBSpooler dbSpooler;

	public static void main(String[] args) {
		SpringApplication.run(ScriptDeployerApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		try {
			dbSpooler.spoolDB("before_");
			LOGGER.info("Execution Started");
			LOGGER.info("=================================================");
			variablesValidator.validate();
			File scriptsDir = Paths.get(appSettings.getScriptsLocation()).toFile();
			fileProcessorService.processOneTimeFiles(scriptsDir);
			fileProcessorService.processRepeatableFiles(scriptsDir);
			fileProcessorService.processRunAllTimeFiles(scriptsDir);
			LOGGER.info("=================================================");
			dbSpooler.spoolDB("after_");
			LOGGER.info("Execution completed.");
		} catch (Exception ex) {
			if (appSettings.isTraceRequired()) {
				LOGGER.error(ex.getMessage(), ex);
			} else {
				LOGGER.error(ex.getMessage());
			}
		}
	}

}
