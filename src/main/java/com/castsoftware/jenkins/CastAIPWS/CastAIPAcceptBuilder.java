package com.castsoftware.jenkins.CastAIPWS;

import hudson.EnvVars;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.tasks.Builder;

import java.io.IOException;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import javax.xml.rpc.ServiceException;

import org.apache.axis.AxisFault;
import org.kohsuke.stapler.DataBoundConstructor;

import com.castsoftware.batch.CastWebService;
import com.castsoftware.batch.CastWebServiceServiceLocator;
import com.castsoftware.exception.HelperException;
import com.castsoftware.jenkins.CastAIPWS.util.Constants;
import com.castsoftware.jenkins.CastAIPWS.util.Utils;
import com.castsoftware.util.CastUtil;
import com.castsoftware.util.VersionInfo;
import com.castsoftware.webservice.RemoteHelper;

public class CastAIPAcceptBuilder extends Builder
{

	// Fields in config.jelly must match the parameter names in the
	// "DataBoundConstructor"
	@DataBoundConstructor
	public CastAIPAcceptBuilder()
	{
	}

	private Boolean checkWebServiceCompatibility(String version)
	{
		return Constants.wsVersionCompatibility.equals(version);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException,
			InterruptedException
	{
		int taskId;
		long startTime = System.nanoTime();
        
		// Calculate which CMS address to use ?
		// Publish/Set web service name for future use
		// Register the application with Web service.
		
/*		EnvVars envVars = build.getEnvironment(listener);
		int startAt;
		try {
			startAt = Integer.parseInt(envVars.get(Constants.START_AT));
		} catch (NumberFormatException e) {
			startAt=0;
		}
		if (startAt > Constants.RunAcceptDelivery) {
			listener.getLogger().println(" ");
			listener.getLogger().println(
					String.format("${START_AT} = %d, skipping delivery acceptance step.", startAt));
		} else {
	 	 	listener.getLogger().println("");
			listener.getLogger().println("Accept Delivery");
	
			String castDate = envVars.get(Constants.CAST_DATE);
			String webServiceAddress = envVars.get(Constants.CMS_WEB_SERVICE_ADDRESS);
			String dmtWebServiceAddress = envVars.get(Constants.DMT_WEB_SERVICE_ADDRESS);
			String appName = envVars.get(Constants.APPLICATION_NAME);
			String versionName = envVars.get(Constants.VERSION_NAME,"");
			String castMSConnectionProfile = envVars.get(Constants.CONNECTION_PROFILE);
			String workFlow = envVars.get(Constants.WORK_FLOW);
			boolean failBuild = workFlow.trim().toLowerCase().equals("no");
			listener.getLogger().println("Web Service: " + webServiceAddress);
			
			CastWebServiceServiceLocator cbwst = new CastWebServiceServiceLocator();
			cbwst.setCastWebServicePortEndpointAddress(dmtWebServiceAddress);
	
			CastWebServiceServiceLocator cbwsl = new CastWebServiceServiceLocator();
			cbwsl.setCastWebServicePortEndpointAddress(webServiceAddress);

			//CastWebServiceServiceLocator cbwsldmt1 = new CastWebServiceServiceLocator();
			//cbwsldmt1.setCastWebServicePortEndpointAddress(webServiceAddress);
			try {
				CastWebService cbws = cbwsl.getCastWebServicePort();
				CastWebService cbwstt = cbwst.getCastWebServicePort();
				//CastWebService cbws_dmt1 = cbwsldmt1.getCastWebServicePort();
				String deliveryFolder = cbwstt.getDMTDeliveryFolder();
				
				String latestDeliveryVersion = cbwstt.getlatestDmtVersion(deliveryFolder, appName, versionName);
				Date dateForToday = Constants.castDateFormat.parse(castDate);
				String versionNameWithTag = versionName
						.replace("[TODAY]", Constants.dateFormatVersion.format(dateForToday));
	

				listener.getLogger().println(String.format(" Parsing Delivery folder(%s) with Version(%s) to find the latest delivery ",deliveryFolder,versionName ));
				listener.getLogger().println(String.format(" Latest Delivery is %s ",latestDeliveryVersion ));
				
				listener.getLogger().println(String.format("Application Name: %s", appName));
				listener.getLogger().println(String.format("Version Name: %s", latestDeliveryVersion));
				listener.getLogger().println(String.format("Connection Profile Name: %s", castMSConnectionProfile));
	
				VersionInfo vi = RemoteHelper.getVersionInfo(webServiceAddress);
				if (!checkWebServiceCompatibility(vi.getVersion())) {
					listener.getLogger().println(
							String.format("Incompatible Web Service Version %s (Supported: %s)", vi.getVersion(),
									Constants.wsVersionCompatibility));
					return false || failBuild;
				}
	
				// Accept Deliver
				startTime = System.nanoTime();
				Calendar cal = Utils.convertCastDate(castDate);
	
				int retryCount = 0;
				taskId=0;
				while (true)
				{
					try 
					{
						taskId = cbws.acceptDelivery(appName, latestDeliveryVersion, castMSConnectionProfile, cal);
				 	 	listener.getLogger().println("Accepting delivered code ...");
						break;
					} catch (AxisFault af) {
						retryCount++;
						if (retryCount<3)
						{
							listener.getLogger().printf("%d Access fault encountered\n",retryCount);
						} else {
					 	 	listener.getLogger().println("Aborging, too many access faults encounerd.");
					 	 	return false;
						}
					}
				}
				if (taskId < 0) {
					listener.getLogger().println(String.format("Error: %s", cbws.getErrorMessage(-taskId)));
					return false || failBuild;
				} else if (!Utils.getLog(cbws, taskId, startTime, listener)) {
					return false;
				}
	
				// Set As Current Version
				listener.getLogger().println("");
				listener.getLogger().println("Set As Current Version");
				startTime = System.nanoTime();
				//taskId = cbws.setAsCurrentVersion(appName, versionNameWithTag, castMSConnectionProfile, cal);
				//taskId = cbws.setAsCurrentVersion(appName, latestDeliveryVersion, castMSConnectionProfile, cal);
	
				if (taskId < 0) {
					listener.getLogger().println(String.format("Error: %s", cbws.getErrorMessage(-taskId)));
					return false || failBuild;
				}
	
				if (!Utils.getLog(cbws, taskId, startTime, listener)) {
					return false;
				}
	
				listener.getLogger().println(" ");
				
			} catch (ServiceException | RemoteException | ParseException | HelperException e) {
				listener.getLogger().println(
						String.format("Interrupted after: %s\n%s: %s",
								CastUtil.formatNanoTime(System.nanoTime() - startTime), e.getClass().getName(),
								e.getMessage()));
				return false || failBuild;
			}
		}
		//are there any remaining steps to run, if so run them now
		if (!Utils.runJobs(build, launcher, listener, this.getClass(), Constants.RunAcceptDelivery))
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
////			return String.format("CAST AIP %d: Accept Delivery", Constants.RunAcceptDelivery)  ;
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
