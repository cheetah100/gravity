package nz.net.orcon.kanban.automation.actions;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.amazonaws.services.identitymanagement.model.ServerCertificateMetadata;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/test-aws.xml" })
public class AwsAutomationActionTest {
	
	@Autowired
	AwsAutomationAction automationAction;

	@Test
	@Ignore
	public void testLoadSSLCertificate() throws IOException {
		
		// String certificate = getFile("/home/peter/key/test2.crt");
		// String privateKey = getFile("/home/peter/key/test2.key");

		String certificate = getFile("/home/peter/devcentre/devcentre.org.crt");
		String privateKey = getFile("/home/peter/devcentre/devcentre.org.key");
		String path = "/cloudfront/production/";
		
		// System.out.println(certificate);
		// System.out.println(privateKey);
		
		// automationAction.loadSSLCertificate("testlb", "testcert2", 443, certificate, privateKey, null);
		String certificateId = automationAction.loadSSLCertificate("prodcert", certificate, privateKey, path);
		
		System.out.println("Certificate ID: " + certificateId );
		
		//automationAction.setLoadBalancerCertificate("testlb", certificateId, 443);
	}
	
	@Test
	public void testGetSSLCertificates() throws IOException {	
		List<ServerCertificateMetadata> certs = automationAction.getCertificates();
		System.out.println("Certificates: " + certs.size() );
		for( ServerCertificateMetadata cert : certs) {
			System.out.println(cert.getServerCertificateId() + " -> " + cert.getServerCertificateName());
		}
	}
	
	@Test
	@Ignore
	public void testCreateCloudFrontDistribution(){
		
		Collection<String> loadBalancerList = automationAction.getLoadBalancerList();
		
		System.out.println("LB Size: " + loadBalancerList.size() );
		for( String item : loadBalancerList){
			System.out.println("LB: " + item );
		}
		
		String domainName = "testlb-1715940860.ap-southeast-2.elb.amazonaws.com";
		String certificateId = automationAction.getCertificateId("prodcert");
		String originId = "testlblink";
		String ref = "TestDistribution";
		String location = 
				automationAction.createCloudFrontDistribution(domainName, certificateId, originId, ref);
		
		System.out.println("Location: " + location );
	}
		
	public void testGetAccountSummary() throws IOException {
		
		Map<String, Integer> accountSummary = automationAction.getAccountSummary();
		
		for( Entry<String,Integer> entry : accountSummary.entrySet()){
			System.out.println(entry.getKey() +" = " + entry.getValue().toString());
		}

	}

	
	public String getFile(String fileName) throws IOException{
		FileInputStream in = new FileInputStream(fileName);
		BufferedInputStream bis = new BufferedInputStream(in);
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		int result = bis.read();
		while(result != -1) {
			byte b = (byte)result;
			buf.write(b);
			result = bis.read();
		}        
		return buf.toString();
	}	
	
}
