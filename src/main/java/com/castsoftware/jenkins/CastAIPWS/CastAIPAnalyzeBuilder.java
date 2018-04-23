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
import com.castsoftware.jenkins.CastAIPWS.util.Constants;
import com.castsoftware.jenkins.CastAIPWS.util.Utils;
import com.castsoftware.restapi.JsonResponse;
import com.castsoftware.util.CastUtil;
import com.castsoftware.vps.ValidationProbesService;
import com.google.gson.Gson;

public class CastAIPAnalyzeBuilder extends Builder 
{
    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public CastAIPAnalyzeBuilder() 
    {
    }
     
    @SuppressWarnings("rawtypes")
	@Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException 
    {
    	int taskId;
    	long startTime = System.nanoTime() ; 

    	/*
		EnvVars envVars = build.getEnvironment(listener);
		
		
		
		int startAt;
		try {
			startAt = Integer.parseInt(envVars.get(Constants.START_AT));
		} catch (NumberFormatException e) {
			startAt=0;
		}
    	if (startAt > Constants.RunAnalysis) {
			listener.getLogger().println(" ");
			listener.getLogger().println(
					String.format("${START_AT} = %d, skipping run analysis step.", startAt));
		} else {
	   		listener.getLogger().println("");
	    	listener.getLogger().println("Run Analysis");
	
	    	boolean failBuild = false;
			try {
		    	String webServiceAddress = envVars.get(Constants.CMS_WEB_SERVICE_ADDRESS);
		    	String castDate = envVars.get(Constants.CAST_DATE);
		    	String appName = envVars.get(Constants.APPLICATION_NAME);
		    	String schemaPrefix = envVars.get(Constants.SCHEMA_PREFIX,"");

		    
				
				
		    	String dbPrefix = envVars.get(Constants.SCHEMA_PREFIX);
		    	String versionName = envVars.get(Constants.VERSION_NAME);
		    	String castMSConnectionProfile = envVars.get(Constants.CONNECTION_PROFILE);
		    	String workFlow = envVars.get(Constants.WORK_FLOW);
		    	failBuild = workFlow.trim().toLowerCase().equals("no");
	
		        CastWebServiceServiceLocator cbwsl = new CastWebServiceServiceLocator();
				cbwsl.setCastWebServicePortEndpointAddress(webServiceAddress);
				CastWebService cbws = cbwsl.getCastWebServicePort();
				cbws.setSchemaNamesInAOP(appName, schemaPrefix);
				CastWebService cbws12 = cbwsl.getCastWebServicePort();
				Calendar cal = Utils.convertCastDate(castDate);
				
	        	startTime = System.nanoTime(); 
	        	taskId = cbws.runAnalysis(appName, versionName,  castMSConnectionProfile, cal);
	        	
	        	
	        	
	        	if (taskId < 0)
	        	{
    				listener.getLogger().println(" ");
	        		listener.getLogger().println("Trying Analysis response less than 0");
	        		String validateionProbURL = cbws12.getValidationProbURL();
	    			if (validateionProbURL==null || validateionProbURL.isEmpty()) {

	    				listener.getLogger().println(" ");
		        		listener.getLogger().println(" ");
	    				
	    			}
	    			else
	    			{
	    				listener.getLogger().println(" ");
		        		listener.getLogger().println("Sending analyisis logs to Application Operations Portal");
		        		
		        		String mngtDB = String.format("%s_mngt", dbPrefix);
		        		String rslt = cbws12.sendAnalysisLogs(mngtDB,appName, castDate);
		        		listener.getLogger().println("Response recieved from CBWS");
		        		JsonResponse response = new Gson().fromJson(rslt, JsonResponse.class);
		        		listener.getLogger().println("Response recieved from CBWS processed");
		        		if (response.getCode() < 0)
		        		{
			        		listener.getLogger().println("Error sending analyisis logs to Application Operations Portal");
		        		}
		        		listener.getLogger().println(response.getJsonString());
	    			}
	        		
	        		listener.getLogger().println(String.format("Error: %s",cbws.getErrorMessage(-taskId)));
	            	return false || failBuild;
	        	}
	        	else 
	        	{

    				listener.getLogger().println(" ");
	        		listener.getLogger().println("Trying Analysis response not less than 0");
	        		
	        		boolean runStatus = Utils.getLog(cbws,taskId,startTime,listener);
	        		
	        		String validateionProbURL = cbws12.getValidationProbURL();
	    			if (validateionProbURL==null || validateionProbURL.isEmpty()) {

	    				listener.getLogger().println(" ");
		        		listener.getLogger().println(" ");
	    				
	    			}
	    			else
	    			{
	    				listener.getLogger().println(" ");
		        		listener.getLogger().println("Sending analyisis logs to Application Operations Portal");
		        		
		        		String mngtDB = String.format("%s_mngt", dbPrefix);
		        		String rslt = cbws12.sendAnalysisLogs(mngtDB,appName, castDate);
		        		listener.getLogger().println("Response recieved from CBWS");
		        		JsonResponse response = new Gson().fromJson(rslt, JsonResponse.class);
		        		listener.getLogger().println("Response recieved from CBWS processed");
		        		if (response.getCode() < 0)
		        		{
			        		listener.getLogger().println("Error sending analyisis logs to Application Operations Portal");
		        		}
		        		listener.getLogger().println(response.getJsonString());
	    			}
	        		return runStatus;
	        	}
	
			} catch (IOException | ServiceException | ParseException e) 
			{
				listener.getLogger().println(
						String.format("Interrupted after: %s\n%s: %s", 
								CastUtil.formatNanoTime(System.nanoTime() - startTime),
								e.getClass().getName(),  
								e.getMessage()));
				return false || failBuild;
			}
		}  

		listener.getLogger().println(" ");
		listener.getLogger().println("Running in case any jobs remains");
		//are there any remaining steps to run, if so run them now
		if (!Utils.runJobs(build, launcher, listener, this.getClass(), Constants.RunAnalysis))
		{
			listener.getLogger().println("Run remaining jobs returned failure");
			return false;
		}
		listener.getLogger().println("All Analysis jobs ran successfully");
		
		*/
    	return true;
    }
    
    void setSchemaNamesInAOP(AbstractBuild build, BuildListener listener, String appName) throws IOException, InterruptedException
	{
    	EnvVars envVars = build.getEnvironment(listener);
		String webServiceAddress = envVars.get(Constants.CMS_WEB_SERVICE_ADDRESS);
	    String schemaPrefix = envVars.get(Constants.SCHEMA_PREFIX);
		CastWebServiceServiceLocator cbwsl = new CastWebServiceServiceLocator();
		cbwsl.setCastWebServicePortEndpointAddress(webServiceAddress);
		
		try {
			CastWebService cbws = cbwsl.getCastWebServicePort();
			
			String validateionProbURL = cbws.getValidationProbURL();
			if (validateionProbURL==null || validateionProbURL.isEmpty()) {
				listener.getLogger().println("Warning: Connection to AOP is not configured - schema names not updated");
			} else {	
				ValidationProbesService vps = new ValidationProbesService(validateionProbURL);
				
				vps.setSchemaNamesInAOP(appName, schemaPrefix);
				
				
			}
		} catch (ServiceException | UnsupportedOperationException | SOAPException | IOException e) {
			listener.getLogger().println("Error reading schema prefix from Jenkins");
		}
	}
    
    /**
     * Descriptor for {@link CastAIPBuilder}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     *
     * <p>
     * See <tt>src/main/resources/hudson/plugins/hello_world/CastDMTBuilder/*.jelly</tt>
     * for the actual HTML fragment for the configuration screen.
     */
//    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
//    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> 
//    {
//
//        private boolean useDMT;
//
//        public DescriptorImpl() {
//            load();
//        }
//
//         @SuppressWarnings("rawtypes")
//		public boolean isApplicable(Class<? extends AbstractProject> aClass) {
//            // Indicates that this builder can be used with all kinds of project types 
//            return true;
//        }
//
//        /**
//         * This human readable name is used in the configuration screen.
//         */
//        public String getDisplayName() {
//			return "";
////			return String.format("CAST AIP %d: Analyze Application", Constants.RunAnalysis)  ;
//        }        
// 
//        @Override
//        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
//        	useDMT = formData.getBoolean("useDMT");
//            save();
//            return super.configure(req,formData);
//        }
//
//        public boolean getUseDMT() {
//            return useDMT;
//        }
//    }
}

