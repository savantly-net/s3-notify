package net.savantly.aws;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.picocli.runtime.annotations.TopCommand;
import picocli.CommandLine;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetBucketNotificationConfigurationResponse;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.model.TopicConfiguration;
import software.amazon.awssdk.services.sns.SnsClient;

@TopCommand
@CommandLine.Command(description = {"Simulates S3 event notifications"})
public class S3Notify implements Runnable {
	
	private Logger log = LoggerFactory.getLogger(S3Notify.class);

    @CommandLine.Option(names = {"-b", "--bucket"}, description = "The bucket name containing the S3 objects", required = true)
    String bucket;

    @CommandLine.Option(names = {"-p", "--prefix"}, description = "Only process objects with this key prefix", required = false, defaultValue = "")
    String prefix;

    @CommandLine.Option(names = {"-m", "--match"}, description = "Only process objects with keys matching this regex", required = false)
    String match;

    @CommandLine.Option(names = {"-e", "--event"}, description = "The event that should be simulated", required = false, defaultValue = "ObjectCreated:Put")
    String event;

    @CommandLine.Option(names = {"-r", "--region"}, description = "The origin region of the event that should be simulated", required = false, defaultValue = "us-east-2")
    String region;

    @CommandLine.Option(names = {"-s", "--service"}, description = "The services that should be sent a notification", required = false, defaultValue = "SNS", type = DestinationType.class)
    List<DestinationType> destinations;

    @CommandLine.Option(names = {"-v", "--verbose"}, description = "Verbose logging")
    boolean verbose;

    @CommandLine.Option(names = {"-d", "--debug"}, description = "Debug logging")
    boolean debug;

    @Inject
	S3Client s3;
    @Inject
	SnsClient sns;
	
	private String eventPrefix;
	private Optional<String> eventSuffix;

    @Override
    public void run() {
    	if (debug) {
    		verbose = true;
    	}
    	
    	eventPrefix = extractEventPrefix(event);
    	eventSuffix = extractEventSuffix(event);
    	
    	GetBucketNotificationConfigurationResponse notificationConfig = 
    			s3.getBucketNotificationConfiguration(getBucketNotificationConfigurationRequest -> getBucketNotificationConfigurationRequest.bucket(bucket));
   	 
    	Optional<String> optTopicArn = getTopicArn(notificationConfig);
    	
    	if (destinations.contains(DestinationType.SNS) && optTopicArn.isEmpty()) {
    		log.info("No matching topic/event. nothing to do");
    		return;
    	}
    	
    	if(destinations.contains(DestinationType.LAMBDA)) {
    		log.warn("LAMBDA not supported yet");
    	}
    	if(destinations.contains(DestinationType.SQS)) {
    		log.warn("SQS not supported yet");
    	}
    	
        log.info("Sending notifications to: {0}, for files in bucket: {1} matching prefix: {2}", destinations, bucket, prefix);
        log.info("debug: {0}, verbose: {1}, event: {2}, match: {3}", debug, verbose, event, match);
        AtomicLong touchCount = new AtomicLong();
        AtomicLong skipCount = new AtomicLong();
        
        s3.listObjectsV2(listObjectsRequestBuilder -> {
        	listObjectsRequestBuilder.bucket(bucket).prefix(prefix);
        }).contents().forEach(f -> {
        	
        	if (Objects.nonNull(match) && !f.key().matches(match)) {
        		if (verbose) log.debug("{0} not matched");
        		skipCount.getAndIncrement();
        	} else {
        		if (optTopicArn.isPresent()) {
        			if (verbose) {
                		log.info("sending notification: {} to {}", f.key(), optTopicArn.get());
                	}
            		sendNotificationToSns(f, optTopicArn.get());
            		touchCount.getAndIncrement();
        		}
        	}
        	
        });
        log.info("notifications: {}, skipped: {}", touchCount.get(), skipCount.get());
    }
    
    protected Optional<String> getTopicArn(GetBucketNotificationConfigurationResponse notificationConfig) {
    	if (destinations.contains(DestinationType.SNS) && notificationConfig.hasTopicConfigurations()) {

       	 	final List<TopicConfiguration> availableTopics = notificationConfig.topicConfigurations();
    		if (verbose) {
    			log.info("available topics: " + availableTopics);
    		}
    		for (TopicConfiguration topicConfiguration : availableTopics) {
    			Optional<String> matched = topicConfiguration.eventsAsStrings().stream().filter(es -> es.startsWith("s3:" + eventPrefix)).findFirst();
    			if (matched.isPresent() && 
    					(matched.get().endsWith("*") || 
    							(eventSuffix.isPresent() && matched.get().endsWith(eventSuffix.get())) 
    					)
    				) {
    				log.info("matched topic: {}", matched.get());
    				return Optional.of(topicConfiguration.topicArn());
    			}
			}
    	}
		return Optional.empty();
	}

	protected String extractEventPrefix(String eventString) {
		String[] parts = eventString.split(":");
		return parts[0];
	}

	protected Optional<String> extractEventSuffix(String eventString) {
		String[] parts = eventString.split(":");
		if (parts.length > 1) {
			return Optional.of(parts[parts.length-1]);
		} else {
			return Optional.empty();
		}
	}

	protected void sendNotificationToSns(S3Object f, String topicArn) {
		sns.publish(publishRequest -> publishRequest.message(createMessageFromTemplate(f)).topicArn(topicArn ));
        
	}

	protected String createMessageFromTemplate(S3Object f) {
		LocalDateTime now = LocalDateTime.now();
		String template = String.format("{\n" + 
				"	\"Records\": [\n" + 
				"		{\n" + 
				"			\"eventVersion\": \"2.0\",\n" + 
				"			\"eventSource\": \"aws:s3\",\n" + 
				"			\"awsRegion\": \"%s\",\n" + 
				"			\"eventTime\": \"%s\",\n" + 
				"			\"eventName\": \"%s\",\n" + 
				"			\"s3\": {\n" + 
				"				\"s3SchemaVersion\": \"1.0\",\n" + 
				"				\"bucket\": {\n" + 
				"					\"name\": \"%s\",\n" + 
				"					\"arn\": \"arn:aws:s3:::%s\"\n" + 
				"				},\n" + 
				"				\"object\": {\n" + 
				"					\"key\": \"%s\",\n" + 
				"					\"size\": \"%s\",\n" + 
				"					\"eTag\": %s\n" + 
				"				}\n" + 
				"			}\n" + 
				"		}\n" + 
				"	]\n" + 
				"}", region, now, event, bucket, bucket, f.key(), f.size(), f.eTag());
		if (debug) {
			log.debug(template);
		}
		return template;
	}
    
    

}