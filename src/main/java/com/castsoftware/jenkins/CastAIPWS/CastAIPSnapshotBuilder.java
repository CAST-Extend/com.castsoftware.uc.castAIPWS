package com.castsoftware.jenkins.CastAIPWS;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.xml.rpc.ServiceException;

import org.kohsuke.stapler.DataBoundConstructor;

import com.castsoftware.batch.CastWebService;
import com.castsoftware.batch.CastWebServiceServiceLocator;
import com.castsoftware.exception.HelperException;
import com.castsoftware.jenkins.CastAIPWS.util.Constants;
import com.castsoftware.jenkins.CastAIPWS.util.Utils;
import com.castsoftware.jenkins.data.Snapshot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import hudson.EnvVars;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.tasks.Builder;

public class CastAIPSnapshotBuilder extends Builder
{
	

	// Fields in config.jelly must match the parameter names in the
	// "DataBoundConstructor"
	@DataBoundConstructor
	public CastAIPSnapshotBuilder()
	{
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException,
			InterruptedException
	{ 
		List<Snapshot> snapshotListToDeleteQA = null;
		int taskId;
		long startTime = System.nanoTime();

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
		if (startAt > Constants.RunSnapshot) {
			listener.getLogger().println(" ");
			listener.getLogger().println(String.format("${START_AT} = %d, skipping run snapshot step.", startAt));
		} else {
			listener.getLogger().println("");
			listener.getLogger().println("Run Snapshot");

			boolean failBuild = false;
			try {
				String castDate = envVars.get(Constants.CAST_DATE);
				String webServiceAddress = envVars.get(Constants.CMS_WEB_SERVICE_ADDRESS);
				String appName = envVars.get(Constants.APPLICATION_NAME);
				
				String snapshotName= envVars.get(Constants.VERSION_NAME);
				
				String versionName = envVars.get(Constants.VERSION_NAME);
				String castSchemaPrefix = envVars.get(Constants.SCHEMA_PREFIX);
				String castMSConnectionProfile = envVars.get(Constants.CONNECTION_PROFILE);
				String retentionPolicy = envVars.get(Constants.RETENTION_POLICY);
				String workFlow = envVars.get(Constants.WORK_FLOW);
				failBuild = workFlow.trim().toLowerCase().equals("no");

				CastWebServiceServiceLocator cbwsl = new CastWebServiceServiceLocator();
				cbwsl.setCastWebServicePortEndpointAddress(webServiceAddress);
				CastWebService cbws = cbwsl.getCastWebServicePort();
				
				
				String centralDbName = String.format("%s_central", castSchemaPrefix);
				String snapshots = cbws.getSnapshotList(centralDbName, appName);					
				Gson gson = new Gson(); 
		        Type collectionType = new TypeToken<List<Snapshot>>(){}.getType();
		        List<Snapshot> snapshotList = gson.fromJson(snapshots, collectionType);
		        snapshotListToDeleteQA = snapshotList;
		        
				if (!Utils.validateWebServiceVersion(webServiceAddress, listener)) {
					return false;
				}
				
				Calendar cal = Utils.convertCastDate(castDate);

				int retentionPolicyInterval = 0;
				if (!retentionPolicy.isEmpty() || startAt > 0)
				{
					String policyName = "";
					if (retentionPolicy.equals("12") ) policyName="Monthy";
					else if (retentionPolicy.equals("4") ) policyName="Quarterly";
					else if (retentionPolicy.equals("2") ) policyName="Every 6 Months";
					
					if (policyName.isEmpty()) listener.getLogger().println("Snapshot retention policy has NOT been set for this application");
					else listener.getLogger().println(String.format("Snapshot retention policy is set to %s",policyName));
					 
			        
			        //we always want to keep the first snapshot so, 
			        //make sure there are at least two snapshots to work with 
			        if (snapshotList != null && snapshotList.size()>1)
			        {
				        //get the latest snapshot
			        	Snapshot latestSnapshot = snapshotList.get(snapshotList.size()-1);
			        	Date lsd = Snapshot.DATE_CONVERTION.parse(latestSnapshot.getFunctionalDate());
		        		Calendar testDate = Calendar.getInstance();
		        		testDate.setTime(lsd);
		        		
		        		int lsdMonth = testDate.get(Calendar.MONTH);
		        		int cdMonth = cal.get(Calendar.MONTH);

		        		//is there a snapshot for the last snapshot date or
		        		//is the snapshot retention policy set?
		        		if (lsd.compareTo(Constants.castDateFormat.parse(castDate))==0 || 
		        				(!policyName.isEmpty() && lsdMonth == cdMonth))
			        	{
							startTime = System.nanoTime();
							if (policyName.isEmpty()) listener.getLogger().println("Deleting snapshot to allow for replacement");
							else listener.getLogger().println("Deleting snapshot to maintain the retention policy");
							
							taskId = cbws.deleteSnapshot(appName, castMSConnectionProfile, testDate, centralDbName);

							if (taskId < 0) {
								listener.getLogger().println(String.format("Error: %s", cbws.getErrorMessage(-taskId)));
								return false || failBuild;
							} else if (!Utils.getLog(cbws, taskId, startTime, listener)) {
								listener.getLogger().println(" ");
								listener.getLogger().println("Warning:  Delete Snapshot failed");
								listener.getLogger().println(" ");
							}
			        	} 
			        } else if (snapshotList != null && snapshotList.size()==1 && startAt > 0 )  {
			        	Snapshot latestSnapshot = snapshotList.get(snapshotList.size()-1);
			        	Date lsd = Snapshot.DATE_CONVERTION.parse(latestSnapshot.getFunctionalDate());
			        	
			        	if (lsd.compareTo(Constants.castDateFormat.parse(castDate))==0) {
			        		Calendar testDate = Calendar.getInstance();
			        		testDate.setTime(lsd);
							startTime = System.nanoTime();
							listener.getLogger().println("Deleting snapshot to allow for the creation of a new one");
							taskId = cbws.deleteSnapshot(appName, castMSConnectionProfile, testDate, centralDbName);			        	
							if (taskId < 0) {
								listener.getLogger().println(String.format("Error: %s", cbws.getErrorMessage(-taskId)));
								return false || failBuild;
							} else if (!Utils.getLog(cbws, taskId, startTime, listener)) {
								listener.getLogger().println(" ");
								listener.getLogger().println("Warning:  Delete Snapshot failed");
								listener.getLogger().println(" ");
							}
			        	}
			        } else {
						if (snapshotList==null) {
							listener.getLogger().println("Warning:  This is the fist snapshot fo this application or an error might have occured while retriving the snapshot list");
						} else {
							listener.getLogger().println("There is only one  snapshot, nothing to delete");
						}
					}
				} 
				
				startTime = System.nanoTime();
				taskId = cbws.runSnapshot(appName, castMSConnectionProfile, snapshotName, versionName, cal,
						Boolean.toString(false),rescanType);

				if (taskId < 0) {
					listener.getLogger().println(String.format("Error: %s", cbws.getErrorMessage(-taskId)));
					return false || failBuild;
				} else if (!Utils.getLog(cbws, taskId, startTime, listener)) {
					return false || failBuild;
				}

			} catch (IOException | ServiceException | ParseException | HelperException e) {
				listener.getLogger().println(
						String.format("%s error accured while generating the snapshot!", e.getMessage()));
				return false || failBuild;
			}
		}
		//are there any remaining steps to run, if so run them now
		if (!Utils.runJobs(build, launcher, listener, this.getClass(), Constants.RunSnapshot))
		{
			return false;
		}

		if(strQAScan.toLowerCase().equals("true"))
		{
		//if scan type is PROD, then delete all QA snapshots-here
		if(!rescanType.equals("QA"))
		{
		if (snapshotListToDeleteQA != null && snapshotListToDeleteQA.size()>1)
        {
			for (Snapshot snap : snapshotListToDeleteQA) {
			    String strSnapshotName = snap.getSnapshotName();
			    
			    Date lsd1 = null;
				try {
					lsd1 = Snapshot.DATE_CONVERTION.parse(snap.getFunctionalDate());
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
        		Calendar dateSnapshotDate = Calendar.getInstance();
        		dateSnapshotDate.setTime(lsd1); 
			    if(strSnapshotName.startsWith("QA"))
			    {
			    //delete this snapshot

					String webServiceAddress = envVars.get(Constants.CMS_WEB_SERVICE_ADDRESS);
					String appName = envVars.get(Constants.APPLICATION_NAME);
					String castMSConnectionProfile = envVars.get(Constants.CONNECTION_PROFILE);
					String castSchemaPrefix = envVars.get(Constants.SCHEMA_PREFIX);
					String centralDbName = String.format("%s_central", castSchemaPrefix);
					boolean failBuild = false;
					
			    	CastWebServiceServiceLocator cbwsl1 = new CastWebServiceServiceLocator();
					cbwsl1.setCastWebServicePortEndpointAddress(webServiceAddress);
					CastWebService cbws = null;
					try {
						cbws = cbwsl1.getCastWebServicePort();
					} catch (ServiceException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
			    	taskId = cbws.deleteSnapshot(appName, castMSConnectionProfile, dateSnapshotDate, centralDbName);			        	
					if (taskId < 0) {
						listener.getLogger().println(String.format("Error deleting QA snapshot: %s", cbws.getErrorMessage(-taskId)));
						return false || failBuild;
					} else if (!Utils.getLog(cbws, taskId, startTime, listener)) {
						listener.getLogger().println(" ");
						listener.getLogger().println("Warning:  Delete Snapshot failed");
						listener.getLogger().println(" ");
					}
			    }
			}
        }
		}
		}
		
		
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
////			return String.format("CAST AIP %d: Generate Snapshot", Constants.RunSnapshot)  ;
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
