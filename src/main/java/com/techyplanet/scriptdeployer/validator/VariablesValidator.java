package com.techyplanet.scriptdeployer.validator;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.techyplanet.scriptdeployer.component.AppSettings;

@Component
public class VariablesValidator {

	@Autowired
	private AppSettings appSettings;

	public void validate() {
		try {
			validateScriptsLocation();
		} catch (Exception ex) {
			throw new RuntimeException(String.format("Validation failed --> %s", ex.getMessage()));
		}
	}

	private void validateScriptsLocation() {
		String scriptsLocation = appSettings.getScriptsLocation();
		if (StringUtils.isBlank(scriptsLocation)) {
			throw new RuntimeException("app.scripts.location variable not defined.");
		}
		if (SystemUtils.IS_OS_WINDOWS) {
			scriptsLocation = scriptsLocation.replace("\\", "/");
		}
		Path varScriptsLocationPath = Paths.get(scriptsLocation);
		if (!Files.isDirectory(varScriptsLocationPath)) {
			throw new RuntimeException(
					String.format("app.scripts.location [%s] in not a valid directory.", scriptsLocation));
		}
	}

}
