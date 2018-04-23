package com.castsoftware.jenkins.CastAIPWS;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import net.sf.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

public class CastCmsWebServiceAddrs extends AbstractDescribableImpl<CastCmsWebServiceAddrs>
{
	//private List<YourObject> projects
	private ArrayList<String> cmsWebServiceAddrsLst;
	
	@DataBoundConstructor
	public CastCmsWebServiceAddrs(ArrayList CmsWebServiceAddrsLst)
	{
		this.cmsWebServiceAddrsLst = CmsWebServiceAddrsLst;
	}
}
