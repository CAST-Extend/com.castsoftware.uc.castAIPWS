package com.castsoftware.jenkins.CastAIPWS;

import java.io.IOException;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.xml.rpc.ServiceException;
import javax.xml.soap.SOAPException;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import com.castsoftware.batch.CastWebService;
import com.castsoftware.batch.CastWebServiceServiceLocator;
import com.castsoftware.exception.HelperException;
import com.castsoftware.jenkins.CastAIPWS.CastAIPBuilder.DescriptorImpl;
import com.castsoftware.jenkins.CastAIPWS.util.AdBlock;
import com.castsoftware.jenkins.CastAIPWS.util.ArchiveBlock;
import com.castsoftware.jenkins.CastAIPWS.util.BackupBlock;
import com.castsoftware.jenkins.CastAIPWS.util.Constants;
import com.castsoftware.jenkins.CastAIPWS.util.OptimizeBlock;
import com.castsoftware.jenkins.CastAIPWS.util.PublishBlock;
import com.castsoftware.jenkins.CastAIPWS.util.RaBlock;
import com.castsoftware.jenkins.CastAIPWS.util.RsBlock;
import com.castsoftware.jenkins.CastAIPWS.util.RsvBlock;
import com.castsoftware.jenkins.CastAIPWS.util.Utils;
import com.castsoftware.jenkins.util.EnvTemplater;
import com.castsoftware.jenkins.util.PublishEnvVarAction;
import com.castsoftware.profiles.ConnectionProfile;
import com.castsoftware.util.CastUtil;
import com.castsoftware.util.VersionInfo;
import com.castsoftware.vps.ValidationProbesService;
import com.castsoftware.webservice.RemoteHelper;

import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.*;
import net.sf.json.JSONObject;

public class CastAIPBuilder extends Builder
{
	private final String dmtWebServiceAddress;
	private final String cmsWebServiceAddress;
	private final String appName;
	private final String versionName;
	private final String referenceVersion;

	private final String referenceVersionPROD; 
	
	private final String castMSConnectionProfile;
	private final String schemaPrefix;
	private final String aadSchemaName;
	private final String retentionPolicy;
	private String lastRunDate;
	
	

	private final boolean backup;
	private final boolean da;
	private final boolean ad;
	private final boolean ra;
	private final boolean rs;
	private final boolean archive;
	private final boolean rav;
	private final boolean publish;
	private final boolean optimize;
	private final boolean useJnlp;

	// private final String snapshotName;
	// private final String captureDate;
	private final String workFlow;

	// Fields in config.jelly must match the parameter names in the
	// "DataBoundConstructor"
	@DataBoundConstructor
	public CastAIPBuilder(String dmtWebServiceAddress, String cmsWebServiceAddress, String appName,
			String referenceVersion, String versionName, String castMSConnectionProfile, BackupBlock backupBlock,
			AdBlock daBlock, AdBlock adBlock, RaBlock raBlock, RsBlock rsBlock, RsvBlock ravBlock,
			PublishBlock publishBlock, ArchiveBlock archiveBlock, OptimizeBlock optimizeBlock, String workFlow,
			String schemaPrefix, String aadSchemaName, String lastRunDate, String retentionPolicy, boolean useJnlp,
			String referenceVersionPROD)
	{
		this.dmtWebServiceAddress = dmtWebServiceAddress;
		this.cmsWebServiceAddress = cmsWebServiceAddress;
		this.appName = appName;
		this.versionName = versionName;
		this.referenceVersion = referenceVersion;
		this.castMSConnectionProfile = castMSConnectionProfile;
		this.schemaPrefix = schemaPrefix;
		this.aadSchemaName = aadSchemaName;
		this.workFlow = workFlow;
		this.lastRunDate = lastRunDate;
		this.referenceVersionPROD = referenceVersionPROD; 
		this.useJnlp=useJnlp;
		
		// Publish snapshot
		if (backupBlock != null) {
			this.backup = true;
		} else {
			this.backup = false;
		}

		// Deliver Code
		if (daBlock != null) {
			this.da = true;
		} else {
			this.da = false;
		}

		// Accept Delivery
		if (adBlock != null) {
			this.ad = true;
		} else {
			this.ad = false;
		}

		// Run Analysis
		if (raBlock != null) {
			this.ra = true;
		} else {
			this.ra = false;
		}

		// Run Snapshot
		if (rsBlock != null) {
			this.retentionPolicy=retentionPolicy;
			this.rs = true;
		} else {
			this.retentionPolicy="";
			this.rs = false;
		}

		// Run Analysis Validation
		if (ravBlock != null) {
			this.rav = true;
		} else {
			this.rav = false;
		}

		// Publish snapshot
		if (publishBlock != null) {
			this.publish = true;
		} else {
			this.publish = false;
		}

		// Publish snapshot
		if (optimizeBlock != null) {
			this.optimize = true;
		} else {
			this.optimize = false;
		}

		// Archive Delivery
		if (archiveBlock != null) {
			this.archive = true;
		} else {
			this.archive = false;
		}
	}

	public boolean isUseJnlp()
	{
		return useJnlp;
	}
	
	private Boolean checkWebServiceCompatibility(String version)
	{
		return Constants.wsVersionCompatibility.equals(version);
	}

	public String getDmtWebServiceAddress()
	{
		return dmtWebServiceAddress;
	}

	public String getCmsWebServiceAddress()
	{
		String retVal = "";
		if (cmsWebServiceAddress == null || cmsWebServiceAddress.isEmpty()) {
			retVal = dmtWebServiceAddress;
		} else {
			retVal = cmsWebServiceAddress;
		}
		return retVal;
	}

	public String getAadSchemaName()
	{
		return aadSchemaName;
	}

	public String getSchemaPrefix()
	{
		return schemaPrefix.toLowerCase();
	}
	 

	public String getAppName()
	{
		return appName;
	}

	public String getVersionName()
	{
		return versionName;
	}

	public String getReferenceVersion()
	{
		if(referenceVersion == null)
		{
			return "Ref";
		}
		else
		{
			return referenceVersion;
		}
	}

	public String getReferenceVersionPROD()
	{
		if(referenceVersionPROD == null)
		{
			return "Ref";
		}
		else
		{
			return referenceVersionPROD;
		} 
	}

	public String getCastMSConnectionProfile()
	{
		return castMSConnectionProfile;
	}

	public boolean isBackup()
	{
		return backup;
	}

	public boolean isDa()
	{
		return da;
	}

	public boolean isAaws()
	{
		return da;
	}

	public boolean isOptimize()
	{
		return optimize;
	}

	public boolean isAd()
	{
		return ad;
	}

	public boolean isRa()
	{
		return ra;
	}

	public boolean isRs()
	{
		return rs;
	}

	public boolean isRav()
	{
		return rav;
	}

	public boolean isArchive()
	{
		return archive;
	}

	public String getRetentionPolicy()
	{
		return retentionPolicy==null?"":retentionPolicy;
	}

	public boolean isPublish()
	{
		return publish;
	}

	

	public String getVersionNameWithTag(Date date, EnvVars envVars, String strQAScan)
	{ 
		
			String rescanType;
			try {
				rescanType = envVars.get(Constants.RESCAN_TYPE);
			} catch (Exception e) {
				rescanType = "PROD";
			}
			
			String s = versionName.replace("[TODAY]", Constants.dateFormatVersion.format(date));
			
			if(s.contains("[SCAN_TYPE]"))
			{
				if(strQAScan == "True")
				{
				s = s.replace("[SCAN_TYPE]", rescanType);
				}
				else
				{
					s = s.replace("[SCAN_TYPE]", "");
				}
			}
			
			EnvTemplater jEnv = new EnvTemplater(envVars);
			s = jEnv.templateString(s);
	
			return s;
	}

	public String getWorkFlow()
	{
		return workFlow;
	}

//	public ListBoxModel doFillAppNameItems(@QueryParameter("dmtWebServiceAddress") final String webServiceAddress)
//	{
//		ListBoxModel m = new ListBoxModel();
//
//		try {
//			Collection<String> apps = RemoteHelper.listApplications(webServiceAddress);
//
//			for (String app : apps) {
//				m.add(app);
//			}
//
//		} catch (HelperException e) {
//			return m;
//		}
//		return m;
//	}

//	public ListBoxModel doFillCastMSConnectionProfileItems(
//			@QueryParameter("cmsWebServiceAddress") final String webServiceAddress)
//	{
//		ListBoxModel m = new ListBoxModel();
//
//		List<String> logNames = (List<String>) LogManager.getLogManager().getLoggerNames();
//
//		try {
//			Collection<ConnectionProfile> cpList = RemoteHelper.listConnectionProfiles(webServiceAddress);
//
//			for (ConnectionProfile cp : cpList) {
//				m.add(cp.getName(), cp.getName());
//			}
//
//		} catch (HelperException e) {
//			return m;
//		}
//		return m;
//	}
	
	@SuppressWarnings("rawtypes")
	public boolean publishVariables(AbstractBuild build, BuildListener listener, String castDate, String strQAScan) throws IOException,
			InterruptedException
	{
		boolean rslt = true;
		Date dateForToday;
		try {
			EnvVars envVars = build.getEnvironment(listener);
			dateForToday = Utils.convertCastDate(castDate).getTime();

			String snapshotName = "Computed on " + Constants.dateFormatVersion.format(dateForToday);

			build.addAction(new PublishEnvVarAction(Constants.CAST_DATE, castDate));
			build.addAction(new PublishEnvVarAction(Constants.DMT_WEB_SERVICE_ADDRESS, getDmtWebServiceAddress()));
			build.addAction(new PublishEnvVarAction(Constants.CMS_WEB_SERVICE_ADDRESS, getCmsWebServiceAddress()));
			build.addAction(new PublishEnvVarAction(Constants.APPLICATION_NAME, getAppName()));
			build.addAction(new PublishEnvVarAction(Constants.VERSION_NAME,	getVersionNameWithTag(dateForToday, envVars, strQAScan)));
			
			build.addAction(new PublishEnvVarAction(Constants.AAD_SCHEMA_NAME, getAadSchemaName()));
			build.addAction(new PublishEnvVarAction(Constants.SCHEMA_PREFIX, getSchemaPrefix()));
			build.addAction(new PublishEnvVarAction(Constants.CONNECTION_PROFILE, getCastMSConnectionProfile()));
			build.addAction(new PublishEnvVarAction(Constants.SNAPSHOT_NAME, snapshotName));

			build.addAction(new PublishEnvVarAction(Constants.RETENTION_POLICY, getRetentionPolicy()));
			
			build.addAction(new PublishEnvVarAction(Constants.WORK_FLOW, getWorkFlow()));
			build.addAction(new PublishEnvVarAction(Constants.REFERENCE_VERSION, getReferenceVersion()));
			build.addAction(new PublishEnvVarAction(Constants.REFERENCE_VERSION_PROD, getReferenceVersionPROD()));

			// build flags
			build.addAction(new PublishEnvVarAction(Constants.RUN_BACKUP, Boolean.toString(isBackup())));
			build.addAction(new PublishEnvVarAction(Constants.RUN_DELIVERY_APPLICATION, Boolean.toString(isDa())));
			build.addAction(new PublishEnvVarAction(Constants.RUN_ACCEPT_DELIVERY, Boolean.toString(isAd())));
			build.addAction(new PublishEnvVarAction(Constants.RUN_ANALYSIS, Boolean.toString(isRa())));
			build.addAction(new PublishEnvVarAction(Constants.RUN_SNAPSHOT, Boolean.toString(isRs())));
			build.addAction(new PublishEnvVarAction(Constants.RUN_VALIDATION, Boolean.toString(isRav())));
			build.addAction(new PublishEnvVarAction(Constants.RUN_PUBLISH_SNAPSHOT, Boolean.toString(isPublish())));
			build.addAction(new PublishEnvVarAction(Constants.RUN_OPTIMIZE_DATABASE, Boolean.toString(isOptimize())));
			build.addAction(new PublishEnvVarAction(Constants.RUN_ARCHIVE_DELIVERY, Boolean.toString(isArchive())));
			build.addAction(new PublishEnvVarAction(Constants.RUN_JNLP_DELIVERY, Boolean.toString(isUseJnlp())));
			
			rslt = Utils.validateBuildVariables(build, listener);

		} catch (ParseException e) {
			listener.error("Unable to publish CAST Enviroment Variables: %s", e.getMessage());
			return false;
		}
		return rslt;
	}

	public int getStartAt(AbstractBuild build, BuildListener listener)
	{
		try {
			EnvVars envVars = build.getEnvironment(listener);
			return Integer.parseInt(envVars.get(Constants.START_AT));
		} catch (NumberFormatException | IOException | InterruptedException e) {
			return 0;
		}

	}

	public String getRescanType(AbstractBuild build, BuildListener listener)
	{
		try {
			EnvVars envVars = build.getEnvironment(listener);
			return envVars.get(Constants.RESCAN_TYPE);
		} catch (Exception e) {
			return "PROD";
		}

	}

	public String getLastRunDate()
	{
		return lastRunDate;
	}
	public void setLastRunDate(String lastRunDate)
	{
		this.lastRunDate = lastRunDate;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException,
			InterruptedException
	{
		long startTime = System.nanoTime();
		EnvVars envVars = build.getEnvironment(listener);
		
		CastWebServiceServiceLocator cbwsl1 = new CastWebServiceServiceLocator();
		cbwsl1.setCastWebServicePortEndpointAddress(cmsWebServiceAddress);
		CastWebService cbws = null;
		try {
			cbws = cbwsl1.getCastWebServicePort();
		} catch (ServiceException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		String strQAScan = cbws.getQAScanFlag();
		
		
		String rescanType = getRescanType(build, listener);
		
		// calculate castDate
		int startAt = getStartAt(build, listener);
		String lastRunDate = getLastRunDate();

		// handle start at here
		//calculate cast date
		String castDate = envVars.get(Constants.CAST_DATE);
		if (startAt > 0) {
			if (castDate == null || castDate.isEmpty()) {
				if (lastRunDate == null || lastRunDate.isEmpty()) 
				{
					castDate = Constants.castDateFormat.format(new Date());				
				} else {
					castDate = lastRunDate;									
				}
			} else {
				lastRunDate = castDate;
			}
		} else { // new run, start at is zero
			castDate = Constants.castDateFormat.format(new Date());				
			lastRunDate = castDate;
			
			if(strQAScan.toLowerCase().equals("true"))
			{
				setCurrentScanType(build, listener, getAppName(),rescanType);
			}
		}
		setLastRunDate(lastRunDate);
		setSchemaNamesInAOP(build, listener, getAppName());
		
		if (!publishVariables(build, listener, castDate, strQAScan)) {
			return false;
		}
		
		
		
		
		listener.getLogger().println("****CAST Application Inteligence Platform****");

		listener.getLogger().println(String.format("START_AT: %d", startAt));
		listener.getLogger().println(String.format("CAST_DATE: %s", castDate));
		if(strQAScan.toLowerCase().equals("true"))
		{
		listener.getLogger().println(String.format("RESCAN_TYPE: %s", rescanType));
		}
		else 
		{
			listener.getLogger().println(String.format("Quality Scans disabled. To enable, change the qa.scan flag to true in the properties file"));
		}
		
		boolean failBuild = getWorkFlow().trim().toLowerCase().equals("no");

		listener.getLogger().println(String.format("DMT Web Service Address:  %s", getDmtWebServiceAddress()));
		listener.getLogger().println(String.format("CMS Web Service Address:  %s", getCmsWebServiceAddress()));
		
		try
		{
			
		
		//Create Job list and run the first job
		CastWebServiceServiceLocator cbwsl = new CastWebServiceServiceLocator();
		cbwsl.setCastWebServicePortEndpointAddress(getCmsWebServiceAddress());
		try {
			if (!Utils.validateWebServiceVersion(getCmsWebServiceAddress(), listener)) {
				return false;
			}

			listener.getLogger().println(String.format("Sending Jenkins Console URL to AOP"));
			
			
			//Send Jenkins Console location to AOP
			sendJenkinsConsolURL(build, listener, getAppName(),castDate);
			
			listener.getLogger().println(String.format("Sent Console URL to AOP"));
			
			if (!Utils.runJobs(build, launcher, listener, this.getClass(), -1))
			{
					return false || failBuild;
			}
			
		} catch (RemoteException | InterruptedException | HelperException | UnsupportedOperationException e) {
			listener.getLogger().println(
					String.format("Interrupted after: %s\n%s: %s",
							CastUtil.formatNanoTime(System.nanoTime() - startTime), e.getClass().getName(),
							e.getMessage()));
			return false || failBuild;
		}
		return true;}
		catch(Exception ex)
		{
			listener.getLogger().println(String.format("Exception:  %s", ex.getMessage()));
			return false || failBuild;
		}
	}
	
	/**
	 * Set current scan type from AOP
	 * @param appName 
	 */
	void setSchemaNamesInAOP(AbstractBuild build, BuildListener listener, String appName)
	{
		String webServiceAddress = getCmsWebServiceAddress();
	    String schemaPrefix = getSchemaPrefix();
		CastWebServiceServiceLocator cbwsl = new CastWebServiceServiceLocator();
		cbwsl.setCastWebServicePortEndpointAddress(webServiceAddress);
		
		try {
			CastWebService cbws = cbwsl.getCastWebServicePort();
			
			String validateionProbURL = cbws.getValidationProbURL();
			if (validateionProbURL==null || validateionProbURL.isEmpty()) {
				listener.getLogger().println("Warning: Connection to AOP is not configured - schema names not updated");
			} else {	
				ValidationProbesService vps = new ValidationProbesService(validateionProbURL);
				
				EnvVars envVars = build.getEnvironment(listener); 
				vps.setSchemaNamesInAOP(appName, schemaPrefix);
				
				
			}
		} catch (ServiceException | UnsupportedOperationException | SOAPException | IOException | InterruptedException e) {
			listener.getLogger().println("Error reading schema prefix from Jenkins");
		}
	}
	
	/**
	 * Set current scan type from AOP
	 * @param appName
	 * @param rescanType
	 */
	void setCurrentScanType(AbstractBuild build, BuildListener listener, String appName, String rescanType)
	{
		String webServiceAddress = getCmsWebServiceAddress();
		
		CastWebServiceServiceLocator cbwsl = new CastWebServiceServiceLocator();
		cbwsl.setCastWebServicePortEndpointAddress(webServiceAddress);
		
		try {
			CastWebService cbws = cbwsl.getCastWebServicePort();
			
			String validateionProbURL = cbws.getValidationProbURL();
			if (validateionProbURL==null || validateionProbURL.isEmpty()) {
				listener.getLogger().println("Warning: Connection to AOP is not configured - validation check has not been performed");
			} else {	
				ValidationProbesService vps = new ValidationProbesService(validateionProbURL);
				
				EnvVars envVars = build.getEnvironment(listener); 
				vps.updateCurrentRescanTypeAOP(appName,rescanType);
				
				
			}
		} catch (ServiceException | UnsupportedOperationException | SOAPException | IOException | InterruptedException e) {
			listener.getLogger().println("Error reading Rescan Type from Jenkins");
		}
	}

	/**
	 *  Send Jenkins console location to AOP
	 * 
	 * @param build 
	 * @param listener
	 * @param appName
	 * @param castDate
	 */
	void sendJenkinsConsolURL(AbstractBuild build, BuildListener listener, String appName,String castDate)
	{
		String webServiceAddress = getCmsWebServiceAddress();
		
		CastWebServiceServiceLocator cbwsl = new CastWebServiceServiceLocator();
		cbwsl.setCastWebServicePortEndpointAddress(webServiceAddress);
		
		try {
			CastWebService cbws = cbwsl.getCastWebServicePort();
			
			String validateionProbURL = cbws.getValidationProbURL();
			if (validateionProbURL==null || validateionProbURL.isEmpty()) {
				listener.getLogger().println("Warning: Connection to AOP is not configured - validation check has not been performed");
			} else {	
				ValidationProbesService vps = new ValidationProbesService(validateionProbURL);
				
				EnvVars envVars = build.getEnvironment(listener);
				String jobName = envVars.get("JOB_NAME");
				String buildNo = envVars.get("BUILD_NUMBER");
				String consoleURL = String.format("job/%s/%s/console",jobName,buildNo);
				vps.sendJenkinsConsolInfo(appName, castDate, consoleURL);
				
			}
		} catch (ServiceException | UnsupportedOperationException | SOAPException | IOException | InterruptedException e) {
			listener.getLogger().println("Error sending Jenkins console information to AOP");
		}
	}
	



	// Overridden for better type safety.
	// If your plugin doesn't really define any property on Descriptor,
	// you don't have to do this.
	@Override
	public DescriptorImpl getDescriptor()
	{
		return (DescriptorImpl) super.getDescriptor();
	}

	/**
	 * Descriptor for {@link CastAIPBuilder}. Used as a singleton. The class is
	 * marked as public so that it can be accessed from views.
	 *
	 * <p>
	 * See
	 * <tt>src/main/resources/hudson/plugins/hello_world/CastDMTBuilder/*.jelly</tt>
	 * for the actual HTML fragment for the configuration screen.
	 */
	@Extension
	// This indicates to Jenkins that this is an implementation of an extension
	// point.
	public static final class DescriptorImpl extends BuildStepDescriptor<Builder>
	{
		public DescriptorImpl()
		{
			load();
		}

		@SuppressWarnings("rawtypes")
		public boolean isApplicable(Class<? extends AbstractProject> aClass)
		{
			// Indicates that this builder can be used with all kinds of project
			// types
			return true;
		}

		/**
		 * This human readable name is used in the configuration screen.
		 */
		public String getDisplayName()
		{
			return "CAST AIP 0: Configuration";
		}

		public FormValidation doTestConnection(
				@QueryParameter("dmtWebServiceAddress") final String dmtWebServiceAddress,
				@QueryParameter("cmsWebServiceAddress") String cmsWebServiceAddress) throws IOException,
				ServletException
		{
			boolean ok = true;
			if (dmtWebServiceAddress.isEmpty()) {
				return FormValidation.error("Delivery Web Service Address must have a value!");
			} else if (cmsWebServiceAddress.replaceAll("\t", "").isEmpty()) {
				cmsWebServiceAddress = dmtWebServiceAddress;
			}

			StringBuffer returnMsg = new StringBuffer();
			try {
				VersionInfo dvi = RemoteHelper.getVersionInfo(dmtWebServiceAddress);
				returnMsg.append(String.format("Delivery Address Success (%s)\n", dvi.toString()));
			} catch (HelperException e) {
				returnMsg.append(String.format("Delivery Address Error %s\n", e.getMessage()));
				ok = false;
			}
			try {
				VersionInfo dvi = RemoteHelper.getVersionInfo(cmsWebServiceAddress);
				if (dvi.getVersion().equals(Constants.wsVersionCompatibility)) {
					returnMsg.append(String.format("Analysis Address Success (%s)\n", dvi.toString()));
				} else {
					returnMsg
							.append(String
									.format("WARNING ****** CAST Batch Web Service Version is not supported by this plugin ******* (%s)\n",
											dvi.toString()));
					ok = false;
				}
			} catch (HelperException e) {
				returnMsg.append(String.format("Analysis Address Error %s", e.getMessage()));
				ok = false;
			}

			if (ok) {
				return FormValidation.ok(returnMsg.toString());
			} else {
				return FormValidation.error(returnMsg.toString());
			}

		}

		public ListBoxModel doFillReferenceVersionPRODItems(
				@QueryParameter("cmsWebServiceAddress") final String cmsWebServiceAddress,
				@QueryParameter("appName") final String appName)
		{
			ListBoxModel m = new ListBoxModel();
			
			try {
				Collection<String> versions = RemoteHelper.listVersions(cmsWebServiceAddress, appName);

				for (String version : versions) {
					m.add(version);
				}

			} catch (HelperException e) {
				return m;
			}
			return m;
		}

		public ListBoxModel doFillReferenceVersionItems(
				@QueryParameter("cmsWebServiceAddress") final String cmsWebServiceAddress,
				@QueryParameter("appName") final String appName)
		{
			ListBoxModel m = new ListBoxModel();
			
			try {
				Collection<String> versions = RemoteHelper.listVersions(cmsWebServiceAddress, appName);

				for (String version : versions) {
					m.add(version);
				}

			} catch (HelperException e) {
				return m;
			}
			return m;
		}

		public ListBoxModel doFillCastMSConnectionProfileItems(
				@QueryParameter("dmtWebServiceAddress") final String dmtWebServiceAddress,
				@QueryParameter("cmsWebServiceAddress") final String cmsWebServiceAddress)
		{
			Logger log = LogManager.getLogManager().getLogger(Logger.GLOBAL_LOGGER_NAME);
			log.addHandler(new ConsoleHandler());
			log.entering(getClass().getName(), String.format(
					"doFillCastMSConnectionProfileItems (dmtWebServiceAddress<%s>)", cmsWebServiceAddress));

			ListBoxModel m = new ListBoxModel();

			try {
				String webServiceAddress =  (cmsWebServiceAddress==null||cmsWebServiceAddress.isEmpty())?dmtWebServiceAddress:cmsWebServiceAddress;

				Collection<ConnectionProfile> cpList = RemoteHelper.listConnectionProfiles(webServiceAddress);
				for (ConnectionProfile cp : cpList) {
					m.add(cp.getName(), cp.getName());
				}

			} catch (HelperException e) {
				return m;
			}
			return m;
		}
		
		public ListBoxModel doFillRetentionPolicyItems(@QueryParameter String retentionPolicy) 
		{
			ListBoxModel m = new ListBoxModel(
					new ListBoxModel.Option("--None--","",retentionPolicy.matches("")),
					new ListBoxModel.Option("Monthly","12",retentionPolicy.matches("12"))
//					new ListBoxModel.Option("Quarterly","4",retentionPolicy.matches("4")),
//					new ListBoxModel.Option("Every 6 Months","2",retentionPolicy.matches("2"))
					);
			return m;
		}

		public ListBoxModel doFillAppNameItems(@QueryParameter("dmtWebServiceAddress") final String webServiceAddress, @QueryParameter("status") String status)
		{
			ListBoxModel m = new ListBoxModel();
			
			try {
				Collection<String> apps = RemoteHelper.listApplications(webServiceAddress);

				for (String app : apps) {
					m.add(app);
				}
				
			} catch (HelperException e) {
				return m;
			}
			return m;
		}

		@Override
		public boolean configure(StaplerRequest req, JSONObject formData) throws FormException
		{
			save();
			return super.configure(req, formData);
		}
	}
}
