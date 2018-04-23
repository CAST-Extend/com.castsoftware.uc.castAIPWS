package com.castsoftware.jenkins.CastAIPWS;

import hudson.EnvVars;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.tasks.Builder;

import java.io.IOException;

import javax.xml.rpc.ServiceException;
import javax.xml.soap.SOAPException;

import org.kohsuke.stapler.DataBoundConstructor;

import com.castsoftware.batch.CastWebService;
import com.castsoftware.batch.CastWebServiceServiceLocator;
import com.castsoftware.jenkins.CastAIPWS.util.Constants;
import com.castsoftware.vps.ValidationProbesService;

public class CastAIPFinalBuilder extends Builder
{
	// Fields in config.jelly must match the parameter names in the
	// "DataBoundConstructor"
	@DataBoundConstructor
	public CastAIPFinalBuilder()
	{
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException,
			InterruptedException
	{
		/*
		EnvVars envVars = build.getEnvironment(listener);

		String castDate = envVars.get(Constants.CAST_DATE);
		String webServiceAddress = envVars.get(Constants.CMS_WEB_SERVICE_ADDRESS);
		String appName = envVars.get(Constants.APPLICATION_NAME);
		String versionName = envVars.get(Constants.VERSION_NAME);

		CastWebServiceServiceLocator cbwsl = new CastWebServiceServiceLocator();
		cbwsl.setCastWebServicePortEndpointAddress(webServiceAddress);

		CastWebService cbws;
		try {
			cbws = cbwsl.getCastWebServicePort();
			listener.getLogger().println("Sending process complete message to rescan console");
			String validateionProbURL = cbws.getValidationProbURL();
			if (validateionProbURL==null || validateionProbURL.isEmpty()) {
				listener.getLogger().println("Warning: Connection to AOP is not configured");
			} else {	
				ValidationProbesService vps = new ValidationProbesService(validateionProbURL);
				cbws.UpdateRescanStatus(appName, versionName, castDate, "Rescan Success", "Rescan Success");
			}
		} catch (ServiceException | UnsupportedOperationException | SOAPException e) {
			listener.getLogger().println(
					String.format("%s error accured while finalizing analysis job.", e.getMessage()));
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
