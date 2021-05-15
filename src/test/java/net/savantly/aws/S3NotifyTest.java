package net.savantly.aws;

import java.util.Optional;

import javax.enterprise.inject.Any;
import javax.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sns.SnsClient;

@QuarkusTest
public class S3NotifyTest {
	
	static {
		System.setProperty("aws.region", "us-east-2");
	}
	
	@InjectMock
	S3Client s3;
	@InjectMock
	SnsClient sns;
	
	@Any
	@Inject
	S3Notify notify;

    @Test
    public void extractEventPrefixTest() {
    	Assertions.assertEquals("ObjectCreated", notify.extractEventPrefix("ObjectCreated:Put"));
    }
    
    @Test
    public void extractEventSuffixExistsTest() {
    	Optional<String> opt = notify.extractEventSuffix("ObjectCreated:Put");
    	Assertions.assertTrue(opt.isPresent());
    	Assertions.assertTrue(opt.get().contentEquals("Put"));
    }
    
    @Test
    public void extractEventSuffixMissingTest() {
    	Optional<String> opt = notify.extractEventSuffix("ObjectCreated");
    	Assertions.assertFalse(opt.isPresent());
    }

}