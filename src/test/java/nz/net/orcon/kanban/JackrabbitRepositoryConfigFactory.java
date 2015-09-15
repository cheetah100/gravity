/**
 * GRAVITY WORKFLOW AUTOMATION
 * (C) Copyright 2015 Orcon Limited
 * 
 * This file is part of Gravity Workflow Automation.
 *
 * Gravity Workflow Automation is free software: you can redistribute it 
 * and/or modify it under the terms of the GNU General Public License as 
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * Gravity Workflow Automation is distributed in the hope that it will be 
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *    
 * You should have received a copy of the GNU General Public License
 * along with Gravity Workflow Automation.  
 * If not, see <http://www.gnu.org/licenses/>. 
 */

package nz.net.orcon.kanban;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.xml.sax.InputSource;

import org.apache.jackrabbit.core.config.ConfigurationException;
import org.apache.jackrabbit.core.config.RepositoryConfig;
import org.apache.jackrabbit.core.config.RepositoryConfigurationParser;

public class JackrabbitRepositoryConfigFactory {

	private static final String JCR_REP_HOME = "jcr.rep.home";

	private String jcrHome;

	private String configFilename = "/repository.xml";

	//private String propertiesFilename = "jackrabbit.properties";
	private String propertiesFilename = "/etc/jackrabbit/bootstrap.properties";

	/**
	 * Create a RepositoryConfig. System properties override those in the
	 * properties file, and jcrHome property of this class is overridden by
	 * rep.home in either.
	 * 
	 * @return RepositoryConfig
	 * @throws IOException
	 * @throws ConfigurationException
	 */
	public RepositoryConfig create() {

		Properties properties = new Properties();
		// jcrHome value has lowest priority
		if (jcrHome != null) {
			properties.setProperty(JCR_REP_HOME, jcrHome);
		}
		try {
			// properties file has higher...
			if (propertiesFilename != null) {
								
				InputStream is = new FileInputStream(propertiesFilename);
				
				if (is != null) {
					try {
						Properties p = new Properties();
						p.load(is);
						properties.putAll(p);
					} finally {
						is.close();
					}
				}
			}

			properties.putAll(System.getProperties());
			String home = properties.getProperty(JCR_REP_HOME);
			properties.setProperty(
					RepositoryConfigurationParser.REPOSITORY_HOME_VARIABLE,
					home);

			InputStream is = new FileInputStream(jcrHome + configFilename);
			
			if (is == null) {
				throw new FileNotFoundException(jcrHome + configFilename);
			}

			return RepositoryConfig.create(new InputSource(is), properties);
		} catch (IOException e) {
			throw new RuntimeException(
					"Unable to load Repository configuration from neither: "
							+ propertiesFilename + " or: " + configFilename, e);
		} catch (ConfigurationException e) {
			throw new RuntimeException("Unable to configure repository with: "
					+ configFilename + " and " + properties);
		}

	}

	/**
	 * Get the working-dir-relative (or absolute) filename of Jackrabbit home.
	 * 
	 * @return
	 */
	public String getJcrHome() {
		return jcrHome;
	}

	/**
	 * Set the working-dir-relative (or absolute) filename of Jackrabbit home.
	 * If not set, the value of system property jcr.rep.home will be used.
	 * 
	 * @param jcrHome
	 */
	public void setJcrHome(String jcrHome) {
		this.jcrHome = jcrHome;
	}

	public String getConfigFilename() {
		return configFilename;
	}

	/**
	 * Set the classpath-relative filename of config file (repository.xml)
	 * 
	 * @param configFilename
	 */
	public void setConfigFilename(String configFilename) {
		this.configFilename = configFilename;
	}

	public String getPropertiesFilename() {
		return propertiesFilename;
	}

	/**
	 * Set the classpath-relative filename of properties-file.
	 * 
	 * @param propertiesFilename
	 */
	public void setPropertiesFilename(String propertiesFilename) {
		this.propertiesFilename = propertiesFilename;
	}

}
