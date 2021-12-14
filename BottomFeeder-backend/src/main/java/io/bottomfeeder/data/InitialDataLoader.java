package io.bottomfeeder.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import io.bottomfeeder.user.UserService;

/**
 * An implementation of application runner callback that runs on startup and performs 
 * the importing of initial data into application.
 * 
 * The importing process is initiated only when initial data import feature is enabled (via
 * {@code bf.data.enable-initial-data-import} property) and application has no data yet.
 * 
 * When initial data file path is specified through {@code bf.data.initial-data-json} property, 
 * the data will be loaded from that location, otherwise sample data will be loaded from 
 * {@code initial_data_sample.json} file stored in app's resources.   
 */
@Component
class InitialDataLoader implements ApplicationRunner {

	private static final Logger logger = LoggerFactory.getLogger(InitialDataLoader.class);
	
	private final Environment environment;
	private final DataImportService dataImportService;
	private final UserService userService;
	
	
	InitialDataLoader(
			Environment environment, 
			DataImportService dataImportService, 
			UserService userService) {
		this.environment = environment;
		this.dataImportService = dataImportService;
		this.userService = userService;
	}

	
	@Override
	public void run(ApplicationArguments args) throws Exception {
		var enableImport = environment.getProperty("bf.data.enable-initial-data-import", Boolean.class, Boolean.FALSE);
		if (enableImport) {
			if (!userService.hasUsers()) {
				dataImportService.importInitialData(loadInitialDataJson());
				logger.info("Successfully imported initial data");
			}
			else {
				logger.info("Skipping initial data import because database already contains data");
			}
		}
	}

	
	private byte[] loadInitialDataJson() throws IOException {
		var initialDataPath = StringUtils.trimToEmpty(environment.getProperty("bf.data.initial-data-json"));
		if (StringUtils.isNotBlank(initialDataPath))
			return loadInitialDataFromPath(initialDataPath);
		else
			return loadInitialDataSample();
	}
	
	
	private static byte[] loadInitialDataFromPath(String path) throws IOException {
		logger.info(String.format("Importing initial data from path: %s", path));
		return Files.readAllBytes(Paths.get(path));
	}
	
	
	private static byte[] loadInitialDataSample() throws IOException {
		logger.info("Importing initial data sample");
		try (var input = InitialDataLoader.class.getResourceAsStream("/initial_data_sample.json")) {
			return input.readAllBytes();
		}
	}
	
}
