package com.castsoftware.jenkins.CastAIPWS;

import hudson.EnvVars;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.tasks.Builder;

import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;

import javax.xml.rpc.ServiceException;
import javax.xml.soap.SOAPException;

import org.kohsuke.stapler.DataBoundConstructor;

import com.castsoftware.batch.CastWebService;
import com.castsoftware.batch.CastWebServiceServiceLocator;
import com.castsoftware.exception.HelperException;
import com.castsoftware.jenkins.CastAIPWS.util.Constants;
import com.castsoftware.jenkins.CastAIPWS.util.Utils;
import com.castsoftware.vps.ValidationProbesService;

public class CastAIPPublishBuilder extends Builder
{
	// Fields in config.jelly must match the parameter names in the
	// "DataBoundConstructor"
	@DataBoundConstructor
	public CastAIPPublishBuilder()
	{
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException,
			InterruptedException
	{
		int taskId=0;
		long startTime = System.nanoTime();
        /*
		EnvVars envVars = build.getEnvironment(listener);
		
		String cmsWebServiceAddress1 = envVars.get(Constants.CMS_WEB_SERVICE_ADDRESS); 
		CastWebServiceServiceLocator cbwsl11 = new CastWebServiceServiceLocator();
		cbwsl11.setCastWebServicePortEndpointAddress(cmsWebServiceAddress1);
		CastWebService cbws11 = null;
		try {
			cbws11 = cbwsl11.getCastWebServicePort();
		} catch (ServiceException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		String strQAScan = cbws11.getQAScanFlag();
		
		
		String rescanType;
		try {
			rescanType = envVars.get(Constants.RESCAN_TYPE);
		} catch (Exception e) {
			rescanType = "PROD";
		}
		int startAt;
		try {
			startAt = Integer.parseInt(envVars.get(Constants.START_AT));
		} catch (NumberFormatException e) {
			startAt=0;
		}
		if (startAt > Constants.RunPublishAAD) {
			listener.getLogger().println(" ");
			listener.getLogger().println(String.format("${START_AT} = %d, skipping publish snapshot step.", startAt));
		} else {
			listener.getLogger().println(" ");
			listener.getLogger().println("Publish Snapshot");

			boolean failBuild = false;
			try {
				String castDate = envVars.get(Constants.CAST_DATE);
				String webServiceAddress = envVars.get(Constants.CMS_WEB_SERVICE_ADDRESS);
				String appName = envVars.get(Constants.APPLICATION_NAME);
				String versionName = envVars.get(Constants.VERSION_NAME);
				String castSchemaPrefix = envVars.get(Constants.SCHEMA_PREFIX);
				String aadSchemaName = envVars.get(Constants.AAD_SCHEMA_NAME);
				String workFlow = envVars.get(Constants.WORK_FLOW);
				failBuild = workFlow.trim().toLowerCase().equals("no");

				CastWebServiceServiceLocator cbwsl = new CastWebServiceServiceLocator();
				cbwsl.setCastWebServicePortEndpointAddress(webServiceAddress);
				CastWebService cbws = cbwsl.getCastWebServicePort();
				if (!Utils.validateWebServiceVersion(webServiceAddress, listener)) {
					return false;
				}

				Calendar cal = Calendar.getInstance();
				cal.setTime(Constants.castDateFormat.parse(castDate));

				startTime = System.nanoTime();
				
				if(strQAScan.toLowerCase().equals("true"))
				{
				if(rescanType.equals("QA"))
				{
					listener.getLogger().println(" ");
					listener.getLogger().println("NO GO: Publication not allowed for QA scans. Publication action NOT PERFORMED.");

					String validateionProbURL = cbws.getValidationProbURL();
					ValidationProbesService vps = null;
					if (validateionProbURL==null || validateionProbURL.isEmpty()) 
					{
						listener.getLogger().println("Warning: Connection to AOP is not configured - validation check has not been performed");
					} 
					else 
					{	
					vps = new ValidationProbesService(validateionProbURL);
					
					if (vps != null) 
						{
							vps.UpdateRescanStatus(appName, versionName, castDate, "Publish Snapshot - OK" , "Report");
						}
					}
					
					return true;
				}
				else
				{
				taskId = cbws.runPublishSnapshot(aadSchemaName, castSchemaPrefix+"_central", appName, versionName, cal);
				if (taskId < 0) 
				{
					listener.getLogger().println(String.format("Error: %s", cbws.getErrorMessage(-taskId)));
					return false || failBuild;
				} 
				else if (!Utils.getLog(cbws, taskId, startTime, listener)) 
				{
					return false;
				}
				} 
				}
				else
				{
					taskId = cbws.runPublishSnapshot(aadSchemaName, castSchemaPrefix+"_central", appName, versionName, cal);
					if (taskId < 0) 
					{
						listener.getLogger().println(String.format("Error: %s", cbws.getErrorMessage(-taskId)));
						return false || failBuild;
					} 
					else if (!Utils.getLog(cbws, taskId, startTime, listener)) 
					{
						return false;
					}
				}
				
			} catch (IOException | ServiceException | ParseException | UnsupportedOperationException | HelperException e) {
				listener.getLogger().println(
						String.format("%s error accured while generating the publishing the snapshot!", e.getMessage()));
				return false || failBuild;
			} catch (SOAPException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		//are there any remaining steps to run, if so run them now
		if (!Utils.runJobs(build, launcher, listener, this.getClass(), Constants.RunPublishAAD))
		{
			return false;
		}
*/
		return true;
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
//	@Extension
//	// This indicates to Jenkins that this is an implementation of an extension
//	// point.
//	public static final class DescriptorImpl extends BuildStepDescriptor<Builder>
//	{
//
//		private boolean useDMT;
//
//		public DescriptorImpl()
//		{
//			load();
//		}
//
//		@SuppressWarnings("rawtypes")
//		public boolean isApplicable(Class<? extends AbstractProject> aClass)
//		{
//			// Indicates that this builder can be used with all kinds of project
//			// types
//			return true;
//		}
//
//		/**
//		 * This human readable name is used in the configuration screen.
//		 */
//		public String getDisplayName()
//		{
//			return "";
////			return String.format("CAST AIP %d: Publish Snapshot", Constants.RunPublishAAD)  ;
//		}
//
//		@Override
//		public boolean configure(StaplerRequest req, JSONObject formData) throws FormException
//		{
//			useDMT = formData.getBoolean("useDMT");
//			save();
//			return super.configure(req, formData);
//		}
//
//		public boolean getUseDMT()
//		{
//			return useDMT;
//		}
//	}
}
