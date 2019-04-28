package com.techyplanet.scriptdeployer.component;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.techyplanet.scriptdeployer.entity.ScriptHistory;
import com.techyplanet.scriptdeployer.repository.ScriptHistoryRepository;

@Component
public class DBSpooler {

	@Autowired
	private ScriptHistoryRepository scriptHistoryRepository;

	@Autowired
	private AppSettings appSettings;

	private static final Logger LOGGER = LoggerFactory.getLogger(DBSpooler.class);

	public void spoolDB(String filePrefix) {
		String spoolFileName = appSettings.getLogDir() + "/" + filePrefix + "db_dump_"
				+ new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()) + ".csv";
		LOGGER.info(" ----- Spooling DB table in a CSV file ----- {}", spoolFileName);
		Iterable<ScriptHistory> records = scriptHistoryRepository.findAll();
		File csvOutputFile = new File(spoolFileName);
		try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
			pw.println("path,type,sequence,version,checksum,create date,update date");
			for (ScriptHistory r : records) {
				pw.println(String.format("%s,%s,%d,%d,%s,%s,%s", r.getPath(), r.getType(), r.getSequence(), r.getVersion(),
						r.getChecksum(), r.getCreateDate(), r.getUpdateDate()));
			}
		} catch (FileNotFoundException ex) {
			LOGGER.error("Failed to write DB Dump in csv file.");
		}
	}

}
