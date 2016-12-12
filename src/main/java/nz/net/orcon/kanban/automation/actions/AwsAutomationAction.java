/**
 * GRAVITY WORKFLOW AUTOMATION
 * (C) Copyright 2016 Peter Harrison
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

package nz.net.orcon.kanban.automation.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudfront.AmazonCloudFrontClient;
import com.amazonaws.services.cloudfront.model.CookiePreference;
import com.amazonaws.services.cloudfront.model.CreateDistributionRequest;
import com.amazonaws.services.cloudfront.model.CreateDistributionResult;
import com.amazonaws.services.cloudfront.model.CustomOriginConfig;
import com.amazonaws.services.cloudfront.model.DefaultCacheBehavior;
import com.amazonaws.services.cloudfront.model.DistributionConfig;
import com.amazonaws.services.cloudfront.model.ForwardedValues;
import com.amazonaws.services.cloudfront.model.Headers;
import com.amazonaws.services.cloudfront.model.Origin;
import com.amazonaws.services.cloudfront.model.OriginProtocolPolicy;
import com.amazonaws.services.cloudfront.model.Origins;
import com.amazonaws.services.cloudfront.model.TrustedSigners;
import com.amazonaws.services.cloudfront.model.ViewerCertificate;
import com.amazonaws.services.cloudfront.model.ViewerProtocolPolicy;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
import com.amazonaws.services.elasticloadbalancing.model.DescribeLoadBalancersResult;
import com.amazonaws.services.elasticloadbalancing.model.LoadBalancerDescription;
import com.amazonaws.services.elasticloadbalancing.model.SetLoadBalancerListenerSSLCertificateRequest;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.identitymanagement.model.GetAccountSummaryResult;
import com.amazonaws.services.identitymanagement.model.ListServerCertificatesResult;
import com.amazonaws.services.identitymanagement.model.ServerCertificateMetadata;
import com.amazonaws.services.identitymanagement.model.UploadServerCertificateRequest;
import com.amazonaws.services.identitymanagement.model.UploadServerCertificateResult;

@Component
public class AwsAutomationAction {
	
	@Autowired
	private AmazonElasticLoadBalancingClient loadBalanceClient;
	
	@Autowired
	private AmazonIdentityManagementClient identityClient;
	
	@Autowired
	private AmazonCloudFrontClient cloudFrontClient;
	
	public  List<ServerCertificateMetadata> getCertificates(){
		ListServerCertificatesResult listSigningCertificates = identityClient.listServerCertificates();
		return listSigningCertificates.getServerCertificateMetadataList();
	}
	
	public String loadSSLCertificate( 
			String certificateName, 
			String certificate, 
			String privateKey,
			String path){
		
		String certificateId = this.getCertificateId(certificateName);
		if(certificateId==null){
		
			UploadServerCertificateRequest certificateRequest =
				new UploadServerCertificateRequest( certificateName, certificate, privateKey);
			certificateRequest.setPath(path);
		
			UploadServerCertificateResult uploadServerCertificate = 
				getIdentityClient().uploadServerCertificate(certificateRequest);
		
			certificateId = uploadServerCertificate.getServerCertificateMetadata().getServerCertificateId();
		}
		return certificateId;
		
	}
	
	public void setLoadBalancerCertificate( String loadBalancerName, String certificateId, Integer port  ){
		
		System.out.println( "LB CertID: " + certificateId);
		
		this.loadBalanceClient.setRegion(Region.getRegion(Regions.AP_SOUTHEAST_2));
		
		SetLoadBalancerListenerSSLCertificateRequest lbRequest = 
				new SetLoadBalancerListenerSSLCertificateRequest(
						loadBalancerName,
						port,
						certificateId);
		
		getLoadBalanceClient().setLoadBalancerListenerSSLCertificate(lbRequest);
	}
	
	public boolean isSSLCertificateLoaded( String certificateName ){
		List<ServerCertificateMetadata> certificates = this.getCertificates();
		for( ServerCertificateMetadata certificate : certificates){
			if(certificate.getServerCertificateName().equals(certificateName)) {
				return true;
			}
		}
		return false;
	}
	
	public String getCertificateId( String certificateName){
		List<ServerCertificateMetadata> certificates = this.getCertificates();
		for( ServerCertificateMetadata certificate : certificates){
			if(certificate.getServerCertificateName().equals(certificateName)) {
				return certificate.getServerCertificateId();
			}
		}
		return null;
	}
	
	public Collection<String> getLoadBalancerList(){
		
		DescribeLoadBalancersResult loadBalancers = this.loadBalanceClient.describeLoadBalancers();
		
		System.out.println(loadBalancers.toString());
		
		Collection returnList = new ArrayList<String>();
		List<LoadBalancerDescription> loadBalancerDescriptions = loadBalancers.getLoadBalancerDescriptions();
		for( LoadBalancerDescription desc : loadBalancerDescriptions){
			returnList.add(desc.getDNSName());
		}
		return returnList;
	}
	
	public String createCloudFrontDistribution(
			String domainName,
			String certificateId,
			String originId,
			String callerReference) {
		
		CreateDistributionRequest createDistributionRequest = new CreateDistributionRequest();
		DistributionConfig distributionConfig = new DistributionConfig();
		DefaultCacheBehavior defaultCacheBehavior = new DefaultCacheBehavior();
		ViewerCertificate viewerCertificate = new ViewerCertificate();
		ForwardedValues forwardedValues = new ForwardedValues();
		Headers headers = new Headers();
		TrustedSigners trustedSigners = new TrustedSigners();
		CookiePreference cookies = new CookiePreference();
		Origin origin = new Origin();
		Origins origins = new Origins();
		Collection<Origin> items = new ArrayList<Origin>();
		CustomOriginConfig customOriginConfig = new CustomOriginConfig();
		
		// Origins
		customOriginConfig.setHTTPPort(80);
		customOriginConfig.setHTTPSPort(443);
		customOriginConfig.setOriginProtocolPolicy(OriginProtocolPolicy.MatchViewer);
		origin.setDomainName(domainName);
		origin.setId(originId);
		origin.setOriginPath("");
		origin.setCustomOriginConfig(customOriginConfig);
		
		items.add(origin);
		origins.setItems(items);
		origins.setQuantity(1);
		
		// Forwarded Values & Headers & Cookies
		cookies.setForward("all");
		Collection<String> headerItems = new ArrayList<String>();
		headers.setItems(headerItems);
		headers.setQuantity(0);
		forwardedValues.setHeaders(headers);
		forwardedValues.setQueryString(false);
		forwardedValues.setCookies(cookies);
		
		// Trusted Signers
		trustedSigners.setEnabled(false);
		trustedSigners.setQuantity(0);
		
		// Viewer Certificate
		viewerCertificate.setIAMCertificateId(certificateId);
		
		// Default Cache BEhaviour
		defaultCacheBehavior.setMinTTL(30l);
		defaultCacheBehavior.setViewerProtocolPolicy(ViewerProtocolPolicy.AllowAll);
		defaultCacheBehavior.setForwardedValues(forwardedValues);
		defaultCacheBehavior.setTargetOriginId("");
		defaultCacheBehavior.setTrustedSigners(trustedSigners);
		
		// Distribution Config
		distributionConfig.setOrigins(origins);
		distributionConfig.setViewerCertificate(viewerCertificate);
		distributionConfig.setCallerReference(callerReference);
		distributionConfig.setDefaultCacheBehavior(defaultCacheBehavior);
		distributionConfig.setEnabled(true);
		distributionConfig.setComment("no comment");
		distributionConfig.setDefaultRootObject("");
		
		createDistributionRequest.setDistributionConfig(distributionConfig);
		CreateDistributionResult createDistribution 
			= this.cloudFrontClient.createDistribution(createDistributionRequest);
		
		return createDistribution.getLocation();
		
	}
	
	
	public Map<String, Integer> getAccountSummary(){
		GetAccountSummaryResult accountSummary = getIdentityClient().getAccountSummary();
		Map<String, Integer> summaryMap = accountSummary.getSummaryMap();
		return summaryMap;
	}

	public AmazonElasticLoadBalancingClient getLoadBalanceClient() {
		return loadBalanceClient;
	}

	public void setLoadBalanceClient(AmazonElasticLoadBalancingClient loadBalanceClient) {
		this.loadBalanceClient = loadBalanceClient;
	}

	public AmazonIdentityManagementClient getIdentityClient() {
		return identityClient;
	}

	public void setIdentityClient(AmazonIdentityManagementClient identityClient) {
		this.identityClient = identityClient;
	}

	public AmazonCloudFrontClient getCloudFrontClient() {
		return cloudFrontClient;
	}

	public void setCloudFrontClient(AmazonCloudFrontClient cloudFrontClient) {
		this.cloudFrontClient = cloudFrontClient;
	}

}
