package com.castsoftware.jenkins.CastAIPWS;

import hudson.EnvVars;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.tasks.Builder;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.List;

import javax.xml.rpc.ServiceException;
import javax.xml.soap.SOAPException;

import org.kohsuke.stapler.DataBoundConstructor;

import com.castsoftware.batch.CastWebService;
import com.castsoftware.batch.CastWebServiceServiceLocator;
import com.castsoftware.exception.HelperException;
import com.castsoftware.jenkins.CastAIPWS.util.Constants;
import com.castsoftware.jenkins.CastAIPWS.util.Utils;
import com.castsoftware.vps.ValidationProbesService;
import com.castsoftware.vps.vo.ValidationResults;

public class CastAIPValidationBuilder extends Builder
{
	// Fields in config.jelly must match the parameter names in the
	// "DataBoundConstructor"
	@DataBoundConstructor
	public CastAIPValidationBuilder()
	{
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException,
			InterruptedException
	{
		return true;
		/*long startTime = System.nanoTime();
		ValidationProbesService vps = null;
 
		EnvVars envVars = build.getEnvironment(listener);
		int startAt;
		
		
		try {
			startAt = Integer.parseInt(envVars.get(Constants.START_AT));
		} catch (NumberFormatException e) {
			startAt = 0;
		}
		if (startAt > Constants.RunValidation) {
			listener.getLogger().println(" ");
			listener.getLogger().println(
					String.format("${START_AT} = %d, skipping run snapshot validation step.", startAt));
			return true;
		} else {
			listener.getLogger().println("");
			listener.getLogger().println("Validate Snapshot");

			String castDate = envVars.get(Constants.CAST_DATE);
			String webServiceAddress = envVars.get(Constants.CMS_WEB_SERVICE_ADDRESS);
			String appName = envVars.get(Constants.APPLICATION_NAME);
			String versionName = envVars.get(Constants.VERSION_NAME);
			//String snapshotName = envVars.get(Constants.SNAPSHOT_NAME);
			String snapshotName = versionName;

			CastWebServiceServiceLocator cbwsl = new CastWebServiceServiceLocator();
			cbwsl.setCastWebServicePortEndpointAddress(webServiceAddress);
			boolean pass = true;
			
			
			try {
				CastWebService cbws = cbwsl.getCastWebServicePort();
                String validationStopFlag = cbws.getValidationStopFlag();
				if (!Utils.validateWebServiceVersion(webServiceAddress, listener)) {
					return false;
				}

				// Validate Snapshot Results
				listener.getLogger().println(
						String.format("Running Snapshot Validation for %s - %s", appName, snapshotName));
				String validateionProbURL = cbws.getValidationProbURL();
				if (validateionProbURL == null || validateionProbURL.isEmpty()) 
				{
					listener.getLogger().println("Warning: Connection to AOP is not configured - validation check has not been performed");
				} 
				else 
				{	
					vps = new ValidationProbesService(validateionProbURL);
	
					//StringBuffer output = new StringBuffer();
					String output1 = new String();
					//List<ValidationResults> allChecks = cbws.runAllChecks(appName, snapshotName); 
					output1 = cbws.runAllChecks(appName, snapshotName);
					StringBuffer output = new StringBuffer(output1); 
					//output = output1.tos
					if(validationStopFlag.toLowerCase().equals("true"))
					{
					    listener.getLogger().println("Warning: validation.stop flag is set to stop at validation stage");
					    listener.getLogger().println("Please check AOP Rescan Validation Page for details");
						listener.getLogger().println(String.format("AOP URL: %s", validateionProbURL));
					   
					    if(output1 == "false")
					    {
					    	pass = false;
					    }
					    else
					    {
					    	pass = true;
					    }
					} 
					else
					{
						listener.getLogger().println("Warning: validation.stop flag is set to false, hence this application will pass this stage.");
						listener.getLogger().println("Please check AOP Rescan Validation screen for details");
						listener.getLogger().println(String.format("AOP URL: %s", validateionProbURL.replace("ValidationProbesService.asmx", "Validation.aspx")));
						pass = true;
					}
					listener.getLogger().println("Sending Validation Status Message...");
					listener.getLogger().println(String.format("Sending completion message: \"%s\" \"%s\" \"%s\" \"%s\" \"%s\"", appName, versionName, castDate,
							5, pass));
					cbws.UpdateRescanStatus(appName, versionName, castDate, "Validation - "
							+ (pass ? "OK" : "Error"), "Validation");
					
				}
			} catch (ServiceException | RemoteException | UnsupportedOperationException | SOAPException
					| HelperException e) {
				listener.getLogger().println(
						String.format("%s error accured while backing up the CAST Tripplet", e.getMessage()));
				return false;
			} finally {
				listener.getLogger().println(String.format("This application has %s", (pass ? "passed" : "failed")));
				
				if (vps != null) 
				{
					CastWebServiceServiceLocator cbwsl1 = new CastWebServiceServiceLocator();
					cbwsl1.setCastWebServicePortEndpointAddress(webServiceAddress);
					CastWebService cbws;
					try {
						cbws = cbwsl1.getCastWebServicePort();
						cbws.UpdateRescanStatus(appName, versionName, castDate, "Validation - "
								+ (pass ? "OK" : "Error"), "Validation");
					} catch (ServiceException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
				listener.getLogger().println(" ");
			}

			//are there any remaining steps to run, if so run them now
		if (!pass || !Utils.runJobs(build, launcher, listener, this.getClass(), Constants.RunValidation))
			{
				return false;
			}
		//logic to show the message here
		//listener.getLogger().println("Warning: validation.stop flag is set to false, if the flag was set to true, this application would have stopped here.");
		
		
			return pass;
		}
		*/
		
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
////			return "CAST AIP 5: Snapshot Validation";
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
