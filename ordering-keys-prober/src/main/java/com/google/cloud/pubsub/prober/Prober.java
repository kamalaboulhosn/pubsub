// Copyright 2020 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
////////////////////////////////////////////////////////////////////////////////
package com.google.cloud.pubsub.prober;

import static java.lang.Math.max;
import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutures;
import com.google.api.gax.batching.FlowControlSettings;
import com.google.api.gax.batching.BatchingSettings;
import com.google.api.gax.batching.FlowController.LimitExceededBehavior;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.cloud.pubsub.v1.SubscriptionAdminSettings;
import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.cloud.pubsub.v1.TopicAdminSettings;
import com.google.cloud.pubsub.v1.stub.GrpcSubscriberStub;
import com.google.cloud.pubsub.v1.stub.SubscriberStubSettings;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableScheduledFuture;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.AcknowledgeRequest;
import com.google.pubsub.v1.ModifyAckDeadlineRequest;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.PullRequest;
import com.google.pubsub.v1.PullResponse;
import com.google.pubsub.v1.ReceivedMessage;
import com.google.pubsub.v1.Subscription;
import com.google.pubsub.v1.Topic;
import com.google.pubsub.v1.TopicName;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import ipc.pubsub2.schema.PersonOuterClass;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import java.io.ByteArrayOutputStream;
import com.google.protobuf.util.JsonFormat;
import java.util.Arrays;
import ipc.pubsub2.schema.Datetimemessage;
import org.apache.avro.io.Decoder;
import java.io.InputStream;
import org.apache.avro.io.DecoderFactory;
import java.io.ByteArrayInputStream;
import org.apache.avro.specific.SpecificDatumReader;

import java.util.Base64;

/**
 * Manages a load test on a single topic and a single subscription with a configurable number of
 * subscriber clients. Tracks the latency of delivered messages as well as a count of duplicates.
 * Can be extended by overriding updateTopicBuilder, updateSubscriptionBuilder,
 * updatePublisherBuilder, updateSubscriberBuilder, processMessage and updateNextMessage.
 */
public class Prober {
  enum SubscriptionType {
    STREAMING_PULL,
    PULL
  };

  static class Builder {
    String project = new String("");
    String endpoint = new String("pubsub.googleapis.com:443");
    String topicName = new String("");
    String subscriptionName = new String("");
    SubscriptionType subscriptionType = SubscriptionType.STREAMING_PULL;
    double messageFailureProbability = 0.0;
    long publishFrequency = 1_000_000L;
    int ackDelayMilliseconds = 0;
    int ackDeadlineSeconds = 10;
    int threadCount = 8;
    int subscriberCount = 1;
    int pullCount = 10;
    int maxPullMessages = 100;
    int subscriberStreamCount = 1;
    int messageSize = 100;
    double messageFilteredProbability = 0.0;
    long subscriberMaxOutstandingMessageCount = 10_000L;
    long subscriberMaxOutstandingBytes = 1_000_000_000L;
    int publishMultiplier = 1;

    public Builder setProject(String project) {
      this.project = project;
      return this;
    }

    public Builder setEndpoint(String endpoint) {
      this.endpoint = endpoint;
      return this;
    }

    public Builder setTopicName(String topicName) {
      this.topicName = topicName;
      return this;
    }

    public Builder setSubscriptionName(String subscriptionName) {
      this.subscriptionName = subscriptionName;
      return this;
    }

    public Builder setSubscriptionType(SubscriptionType subscriptionType) {
      this.subscriptionType = subscriptionType;
      return this;
    }

    public Builder setMessageFailureProbability(double messageFailureProbability) {
      this.messageFailureProbability = messageFailureProbability;
      return this;
    }

    public Builder setPublishFrequency(long publishFrequency) {
      this.publishFrequency = publishFrequency;
      return this;
    }

    public Builder setAckDelayMilliseconds(int ackDelayMilliseconds) {
      this.ackDelayMilliseconds = ackDelayMilliseconds;
      return this;
    }

    public Builder setAckDeadlineSeconds(int ackDeadlineSeconds) {
      this.ackDeadlineSeconds = ackDeadlineSeconds;
      return this;
    }

    public Builder setThreadCount(int threadCount) {
      this.threadCount = threadCount;
      return this;
    }

    public Builder setSubscriberCount(int subscriberCount) {
      this.subscriberCount = subscriberCount;
      return this;
    }

    public Builder setPullCount(int pullCount) {
      this.pullCount = pullCount;
      return this;
    }

    public Builder setMaxPullMessages(int maxPullMessages) {
      this.maxPullMessages = maxPullMessages;
      return this;
    }

    public Builder setSubscriberStreamCount(int subscriberStreamCount) {
      this.subscriberStreamCount = subscriberStreamCount;
      return this;
    }

    public Builder setMessageSize(int messageSize) {
      this.messageSize = messageSize;
      return this;
    }

    public Builder setMessageFilteredProbability(double messageFilteredProbability) {
      this.messageFilteredProbability = messageFilteredProbability;
      return this;
    }

    public Builder setSubscriberMaxOutstandingMessageCount(
        long subscriberMaxOutstandingMessageCount) {
      this.subscriberMaxOutstandingMessageCount = subscriberMaxOutstandingMessageCount;
      return this;
    }

    public Builder setSubscriberMaxOutstandingBytes(long subscriberMaxOutstandingBytes) {
      this.subscriberMaxOutstandingBytes = subscriberMaxOutstandingBytes;
      return this;
    }

    public Builder setPublishMultiplier(int publishMultiplier) {
      this.publishMultiplier = publishMultiplier;
      return this;
    }

    public Prober build() {
      return new Prober(this);
    }
  }

  private static final Logger logger = Logger.getLogger(Prober.class.getName());
  private static final String FILTERED_ATTRIBUTE = "filtered";
  private static final String INSTANCE_ATTRIBUTE = "instance";
  protected static final String MESSAGE_SEQUENCE_NUMBER_KEY = "message_sequence_number";

  private final String project;
  private final String endpoint;
  private final String topicName;
  private final String subscriptionName;
  private final SubscriptionType subscriptionType;
  private final double messageFailureProbability;
  private final long publishFrequency;
  private final int ackDelayMilliseconds;
  private final int ackDeadlineSeconds;
  private final int threadCount;
  private final int subscriberCount;
  private final int pullCount;
  private final int maxPullMessages;
  private final int subscriberStreamCount;
  private final int messageSize;
  private final double messageFilteredProbability;
  private final long subscriberMaxOutstandingMessageCount;
  private final long subscriberMaxOutstandingBytes;
  private final int publishMultiplier;

  private final Random r;
  private ScheduledFuture<?> generatePublishesFuture;
  private boolean shutdown;
  private boolean started;
  private Publisher publisher;
  private final Subscriber[] subscribers;
  private GrpcSubscriberStub[] pullSubscribers;
  private final Future<?>[] pullSubscriberFutures;
  private TopicAdminClient topicAdminClient;
  private SubscriptionAdminClient subscriptionAdminClient;
  private final TopicName fullTopicName;
  private final ProjectSubscriptionName fullSubscriptionName;
  private long publishedMessageCount;
  private final List<ListenableScheduledFuture<?>> awaitingAckFutures = new ArrayList<>();
  private final String instanceId = UUID.randomUUID().toString();
  private AtomicLong publishCount = new AtomicLong();
  protected AtomicLong receivedCount = new AtomicLong();

  protected final ListeningScheduledExecutorService executor;

  public static Builder newBuilder() {
    return new Builder();
  }

  Prober(Builder builder) {
    this.subscriberMaxOutstandingBytes = builder.subscriberMaxOutstandingBytes;
    this.subscriberMaxOutstandingMessageCount = builder.subscriberMaxOutstandingMessageCount;
    this.messageFilteredProbability = builder.messageFilteredProbability;
    this.messageSize = builder.messageSize;
    this.subscriberStreamCount = builder.subscriberStreamCount;
    this.maxPullMessages = builder.maxPullMessages;
    this.pullCount = builder.pullCount;
    this.subscriberCount = builder.subscriberCount;
    this.threadCount = builder.threadCount;
    this.ackDeadlineSeconds = builder.ackDeadlineSeconds;
    this.ackDelayMilliseconds = builder.ackDelayMilliseconds;
    this.publishFrequency = builder.publishFrequency;
    this.messageFailureProbability = builder.messageFailureProbability;
    this.subscriptionType = builder.subscriptionType;
    this.subscriptionName = builder.subscriptionName;
    this.topicName = builder.topicName;
    this.endpoint = builder.endpoint;
    this.project = builder.project;
    this.publishMultiplier = builder.publishMultiplier;
    subscribers = new Subscriber[subscriberCount];
    pullSubscriberFutures = new Future<?>[subscriberCount];

    this.r = new Random();
    this.started = false;
    this.shutdown = false;
    this.executor = MoreExecutors.listeningDecorator(Executors.newScheduledThreadPool(threadCount));

    this.fullTopicName = TopicName.of(project, topicName);
    this.fullSubscriptionName = ProjectSubscriptionName.of(project, subscriptionName);

    try {
      TopicAdminSettings.Builder topicAdminClientBuilder = TopicAdminSettings.newBuilder()
          .setEndpoint(endpoint);
      this.topicAdminClient = TopicAdminClient.create(topicAdminClientBuilder.build());

      SubscriptionAdminSettings.Builder subscriptionAdminClientBuilder =
          SubscriptionAdminSettings.newBuilder().setEndpoint(endpoint);
      this.subscriptionAdminClient =
          SubscriptionAdminClient.create(subscriptionAdminClientBuilder.build());
    } catch (Exception e) {
      logger.log(Level.WARNING, "Admin client creation failed", e);
    }
  }

  /**
   * Publish 'message' using 'publisher' and return the ApiFuture associated with that publish. If
   * 'filteredOut' is true, then the subscriber should not expect to receive the message.
   */
  protected ApiFuture<String> publish(
      Publisher publisher, PubsubMessage message, boolean filteredOut) {
    return publisher.publish(message);
  }

  /**
   * Ensure that message was published by this instance and if so, process it. Otherwise, indicate
   * that the message should be acked.
   */
  private boolean checkAndProcessMessage(PubsubMessage message, int subscriberIndex) {
    String messageInstanceId = message.getAttributes().get(INSTANCE_ATTRIBUTE);
    if (!instanceId.equals(messageInstanceId)) {
      return true;
    }
    return processMessage(message, subscriberIndex);
  }

  /**
   * Process the received message, which was received by subscriber client with index 0 <=
   * subscriberIndex < subscriberCount. If overridden, it is best to call this method first to track
   * the end-to-end latency accurately.
   */
  protected boolean processMessage(PubsubMessage message, int subscriberIndex) {
    return true;
  }

  /**
   * Update the provided Topic builder (which will already have the topic name set) with addition
   * properties
   */
  protected Topic.Builder updateTopicBuilder(Topic.Builder builder) {
    return builder;
  }

  /**
   * Update the provided Subscription builder (which will already have the subscription name and
   * topic name set) with addition properties
   */
  protected Subscription.Builder updateSubscriptionBuilder(Subscription.Builder builder) {
    return builder;
  }

  /**
   * Update the provided Publisher builder (which will already have the topic name set) with
   * addition properties
   */
  protected Publisher.Builder updatePublisherBuilder(Publisher.Builder builder) {
    return builder;
  }

  /**
   * Update the provided Subscriber builder (which will already have the subscription name set) with
   * addition properties
   */
  protected Subscriber.Builder updateSubscriberBuilder(Subscriber.Builder builder) {
    return builder;
  }

  /**
   * Run the load test by deleting old topics and subscriptions, creating new ones, starting
   * subscriber, and publishing messages.
   */
  public synchronized void start() {
    if (started || shutdown) {
      return;
    }
    logger.log(Level.INFO, "Starting probes");
    started = true;
    // Cleanup old instances of topic and subscription if necessary.
    // if (cleanup()) {
    //   // If we have deleted the old topic or subscriber, wait two minutes before creating new ones
    //   // to give times for caches to get flushed. Otherwise, we run into situations where acks may
    //   // not get processed right away or we could even try to pull from the old subscription.
    //   try {
    //     logger.log(Level.INFO, "Waiting 2 minutes before creating new topic and subscription.");
    //     Thread.sleep(2 * 60 * 1000);
    //   } catch (InterruptedException e) {
    //     logger.log(
    //         Level.WARNING, "Sleep before creating new topic and subscription interrupted.", e);
    //   }
    // }
    // createTopic();
    // createSubscription();
    createPublisher();
    // switch (subscriptionType) {
    //   case STREAMING_PULL:
    //     createStreamingPullSubscribers();
    //     break;
    //   case PULL:
    //     createPullSubscribers();
    //     break;
    // }

    generatePublishLoad();
  }

  /** Discontinue the load test. */
  public synchronized void shutdown() {
    if (shutdown || !started) {
      return;
    }
    logger.log(Level.INFO, "Shutting down");
    shutdown = true;
    if (generatePublishesFuture != null) {
      generatePublishesFuture.cancel(true);
    }
    if (publisher != null) {
      publisher.shutdown();
    }

    try {
      Futures.allAsList(awaitingAckFutures).get();
    } catch (InterruptedException | ExecutionException e) {
      logger.log(Level.WARNING, "Could not send acks", e);
    }

    for (Subscriber s : subscribers) {
      if (s != null) {
        s.stopAsync().awaitTerminated();
      }
    }

    if (subscriptionType == SubscriptionType.PULL) {
      for (Future<?> future : pullSubscriberFutures) {
        future.cancel(true);
        try {
          future.get();
        } catch (InterruptedException | ExecutionException e) {
          logger.log(Level.INFO, "Interruption shutting down pull subscriber", e);
        }
      }
    }

    cleanup();
  }

  private void createSubscription() {
    logger.info("Creating subscription " + fullSubscriptionName);
    Subscription.Builder builder =
        Subscription.newBuilder()
            .setName(fullSubscriptionName.toString())
            .setTopic(fullTopicName.toString())
            .setAckDeadlineSeconds(ackDeadlineSeconds);
    if (messageFilteredProbability > 0.0) {
      builder.setFilter("attributes." + FILTERED_ATTRIBUTE + " != \"true\"");
    }
    builder = updateSubscriptionBuilder(builder);
    Subscription subscription = builder.build();
    try {
      subscriptionAdminClient.createSubscription(builder.build());
      logger.info("Created subscription " + fullSubscriptionName);
    } catch (Exception e) {
      if (e instanceof InterruptedException) {
        Thread.currentThread().interrupt();
      }
      logger.log(Level.WARNING, "Failed to create subscription " + fullSubscriptionName, e);
    }
  }

  private void createTopic() {
    logger.info("Creating topic " + fullTopicName);
    Topic.Builder builder = Topic.newBuilder().setName(fullTopicName.toString());
    builder = updateTopicBuilder(builder);
    try {
      topicAdminClient.createTopic(builder.build());
      logger.info("Created topic " + fullTopicName);
    } catch (Exception e) {
      if (e instanceof InterruptedException) {
        Thread.currentThread().interrupt();
      }
      logger.log(Level.WARNING, "Failed to create topic " + fullTopicName, e);
    }
  }

  private void createPublisher() {
    try {
      Publisher.Builder builder = Publisher.newBuilder(fullTopicName).setEndpoint(endpoint);
      builder = updatePublisherBuilder(builder);
      FlowControlSettings flowControlSettings =
          FlowControlSettings.newBuilder()
              // Block more messages from being published when the limit is reached. The other
              // options are Ignore (or continue publishing) and ThrowException (or error out).
              .setLimitExceededBehavior(LimitExceededBehavior.Block)
              .setMaxOutstandingRequestBytes(40 * 1024 * 1024L) // 40 MiB
              .setMaxOutstandingElementCount(1000L) // 100 messages
              .build();

      // By default, messages are not batched.
      BatchingSettings batchingSettings =
          BatchingSettings.newBuilder()
              .setElementCountThreshold(1000L)
              .setRequestByteThreshold(10_000_000L)
              .setDelayThreshold(org.threeten.bp.Duration.ofMillis(100))
              .setFlowControlSettings(flowControlSettings)
              .build();
      publisher = builder.build();
      logger.log(Level.INFO, "Created Publisher");
    } catch (Exception e) {
      logger.log(Level.WARNING, "Failed to create publisher for " + fullTopicName, e);
    }
  }

  private static void ackNackMessage(boolean ack, DateTime received, AckReplyConsumer consumer) {
    if (ack) {
      DateTime ackTime = DateTime.now();
      consumer.ack();
      Duration ackLatency = new Duration(received, ackTime);
    } else {
      consumer.nack();
    }
  }

  private void createStreamingPullSubscribers() {
    for (int i = 0; i < subscriberCount; ++i) {
      try {
        final int index = i;
        MessageReceiver receiver =
            new MessageReceiver() {
              @Override
              public void receiveMessage(PubsubMessage message, AckReplyConsumer consumer) {
                DateTime received = DateTime.now();
                boolean ack = checkAndProcessMessage(message, index);
                if (ackDelayMilliseconds == 0) {
                  ackNackMessage(ack, received, consumer);
                } else {
                  awaitingAckFutures.add(
                      executor.schedule(
                          () -> ackNackMessage(ack, received, consumer),
                          ackDelayMilliseconds,
                          MILLISECONDS));
                }
              }
            };
        FlowControlSettings flowControlSettings =
            FlowControlSettings.newBuilder()
                .setMaxOutstandingElementCount(subscriberMaxOutstandingMessageCount)
                .setMaxOutstandingRequestBytes(subscriberMaxOutstandingBytes)
                .build();
        Subscriber.Builder builder =
            Subscriber.newBuilder(fullSubscriptionName, receiver)
                .setParallelPullCount(subscriberStreamCount)
                .setFlowControlSettings(flowControlSettings)
                .setEndpoint(endpoint);;
        builder = updateSubscriberBuilder(builder);
        Subscriber subscriber = builder.build();
        subscribers[i] = subscriber;
        subscriber.startAsync().awaitRunning();
        logger.log(Level.INFO, "Created Subscriber");
      } catch (RuntimeException e) {
        logger.log(Level.WARNING, "Failed to create subscriber for " + fullSubscriptionName, e);
      }
    }
  }

  private void doPullIteration(int subscriberIndex) {
    PullRequest pullRequest =
        PullRequest.newBuilder()
            .setSubscription(fullSubscriptionName.toString())
            .setMaxMessages(maxPullMessages)
            .build();
    ApiFuture<PullResponse> pullResponseFuture =
        pullSubscribers[subscriberIndex].pullCallable().futureCall(pullRequest);
    pullResponseFuture.addListener(
        () -> {
          PullResponse pullResponse = null;
          try {
            pullResponse = pullResponseFuture.get();
          } catch (InterruptedException | ExecutionException e) {
            logger.log(Level.WARNING, "Could not get pull result.", e);
            doPullIteration(subscriberIndex);
            return;
          }
          List<String> messagesToAck = new ArrayList<>();
          List<String> messagesToNack = new ArrayList<>();
          for (ReceivedMessage message : pullResponse.getReceivedMessagesList()) {
            boolean ack = checkAndProcessMessage(message.getMessage(), subscriberIndex);
            if (ack) {
              messagesToAck.add(message.getAckId());
            } else {
              messagesToNack.add(message.getAckId());
            }
          }
          if (!messagesToAck.isEmpty()) {
            AcknowledgeRequest acknowledgeRequest =
                AcknowledgeRequest.newBuilder()
                    .setSubscription(fullSubscriptionName.toString())
                    .addAllAckIds(messagesToAck)
                    .build();

            pullSubscribers[subscriberIndex].acknowledgeCallable().call(acknowledgeRequest);
          }
          if (!messagesToNack.isEmpty()) {
            ModifyAckDeadlineRequest modAckRequest =
                ModifyAckDeadlineRequest.newBuilder()
                    .setSubscription(fullSubscriptionName.toString())
                    .setAckDeadlineSeconds(0)
                    .addAllAckIds(messagesToNack)
                    .build();
            pullSubscribers[subscriberIndex].modifyAckDeadlineCallable().call(modAckRequest);
          }
          doPullIteration(subscriberIndex);
        },
        executor);
  }

  private void createPullSubscribers() {
    pullSubscribers = new GrpcSubscriberStub[subscriberCount];
    for (int i = 0; i < subscriberCount; ++i) {
      final int index = i;
      pullSubscriberFutures[i] =
          executor.submit(
              () -> {
                SubscriberStubSettings.Builder subscriberStubSettings =
                    SubscriberStubSettings.newBuilder().setEndpoint(endpoint);
                try {
                  pullSubscribers[index] =
                      GrpcSubscriberStub.create(subscriberStubSettings.build());
                } catch (IOException e) {
                  logger.log(Level.SEVERE, "Could not create pull subscriber.", e);
                  return;
                }
                for (int j = 0; j < pullCount; ++j) {
                  doPullIteration(index);
                }
              });
    }
  }

  private boolean deleteTopic(TopicName topic) {
    try {
      topicAdminClient.deleteTopic(topic);
      logger.log(Level.INFO, "Deleted topic %s", topic);
      return true;
    } catch (RuntimeException e) {
      logger.log(Level.WARNING, "Failed to delete topic " + topic, e);
      return false;
    }
  }

  private boolean deleteSubscription(ProjectSubscriptionName subscription) {
    try {
      subscriptionAdminClient.deleteSubscription(subscription);
      logger.log(Level.INFO, "Deleted subscription %s", subscription);
      return true;
    } catch (RuntimeException e) {
      logger.log(Level.WARNING, "Failed to delete subscription " + subscription, e);
      return false;
    }
  }

  // Returns true if a topic or subscription was deleted.
  private boolean cleanup() {
    boolean deleted = deleteSubscription(fullSubscriptionName);
    deleted = deleted || deleteTopic(fullTopicName);
    return deleted;
  }

  private void generatePublishLoad() {
    logger.log(Level.INFO, "Beginning publishing");
    generatePublishesFuture =
        executor.scheduleWithFixedDelay(
            () -> {
          try {
                List<ApiFuture<String>> publishFutures = new ArrayList<ApiFuture<String>>();
                for (int i = 0; i < publishMultiplier; ++i) {
                  String messageSequenceNumber = Long.toString(publishedMessageCount++);
                  // The maximum message size allowed by the service is 10 MB.
                  // int nextMessageSize =
                  //     messageSize <= 0 ? max(1, (int) (10000000 * r.nextDouble())) : messageSize;
                  // byte[] bytes = new byte[messageSize];
                  // r.nextBytes(bytes);

                  try {
                  // PersonOuterClass.Person.Address a = PersonOuterClass.Person.Address.newBuilder().setStreetName("River Terrace").setStreetNumber(1010).setCity("New York").setState("PA").setZip("11701").setCountry("NZ").setExtraField("ABC").build();
                  // PersonOuterClass.Person p = PersonOuterClass.Person.newBuilder().setFirstName("N").setLastName("O").setAge(30).setAddress(a).setAnotherExtraField("HA").build();
                  // String jsonString = JsonFormat.printer().omittingInsignificantWhitespace().print(p);
                  // byte[] bytes = p.toByteArray();

                  // AddressRecord a = new AddressRecord();
                  // a.setStreetNumber(500);
                  // a.setStreetName("57th St.");
                  // a.setCountry("USA");
                  // a.setState("NY");
                  // a.setCity("NYC");
                  // a.setZip("10001");
                  // a.setAnotherExtraField("XYZ");
                  // Person p = new Person();
                  // p.setAddress(a);
                  // p.setFirstName("G");
                  // p.setLastName("H");
                  // p.setAge(10);
                  // p.setExtraField("BBBBBB");

                  // Datetimemessage.DateMessage dt = Datetimemessage.DateMessage.newBuilder().setTimestampf("2022-08-17 03:00:00").setDatef("2022-09-16").setTimef("20:00:03").build();
                  // byte[] bytes = dt.toByteArray();

                  Defaults.ExampleMessage em = Defaults.ExampleMessage.newBuilder().setDefaultstr("HI2").setDefaultbool(false).build();
                  byte[] bytes = em.toByteArray();

                  System.out.println(Base64.getEncoder().encodeToString(bytes));
                  return;


                  // SpecificDatumWriter<Person> w = new SpecificDatumWriter(Person.getClassSchema());
                  // ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                  // Encoder encoder = EncoderFactory.get().directBinaryEncoder(byteStream, /*reuse=*/ null);
                  // w.write(p, encoder);
                  // byte[] bytes = byteStream.toByteArray();

                   //ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                  //   java.nio.ByteBuffer byteBuffer;
                  //  try {
                  //   byteBuffer = p.toByteBuffer();
                  //  } catch (Exception e) {
                  //    logger.log(Level.INFO, "NO ENCODE ", e);
                  //    return;
                  //  }
                  // byte[] bytes = byteBuffer.array();

                  // Time testing
                  // java.time.LocalDate now = java.time.LocalDate.now();
                  // java.time.LocalDateTime timeNow = java.time.LocalDateTime.now();
                 //  java.time.Instant instantNow = java.time.Instant.now();
                  // java.time.LocalTime localTimeNow = java.time.LocalTime.now();
                  // Message m = new Message();
                  // m.setTimeMicrosField(instantNow);
                  // m.setDateField(now);
                  // m.setLocalTimestampMicrosField(timeNow);
                  // m.setLocalTimestampMillisField(timeNow);
                  // m.setTimeMicrosField(localTimeNow);
                  // m.setTimeMillisField(localTimeNow);
                  // m.setTimestampMicrosField(instantNow);
                  // m.setTimestampMillisField(instantNow);

                  // SpecificDatumWriter<Message> w = new SpecificDatumWriter(Message.getClassSchema());
                  // ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                  // Encoder encoder = EncoderFactory.get().directBinaryEncoder(byteStream, /*reuse=*/ null);
                  // w.write(m, encoder);
                  // byte[] bytes = byteStream.toByteArray();

                  // java.nio.ByteBuffer byteBuffer;
                  //  try {
                  //    byteBuffer = m.toByteBuffer();
                  //  } catch (Exception e) {
                  //    logger.log(Level.INFO, "NO ENCODE ", e);
                  //    return;
                  //  }
                  // byte[] bytes = byteBuffer.array();

                  // EnumRecord r = new EnumRecord();
                  // r.setStringField("HI");
                  // r.setEnumField(Suit.DIAMONDS);
                  // SpecificDatumWriter<EnumRecord> w = new SpecificDatumWriter(EnumRecord.getClassSchema());
                  // ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                  // Encoder encoder = EncoderFactory.get().directBinaryEncoder(byteStream, /*reuse=*/ null);
                  // w.write(r, encoder);
                  // byte[] bytes = byteStream.toByteArray();

                  // String jsonString = "{\"string_field\": \"AGAIN\", \"enum_field\": \"HEARTS\"}";

                  // LongRecord r = new LongRecord();
                  // r.setLongField(34234252342340L);
                  // SpecificDatumWriter<LongRecord> w = new SpecificDatumWriter(LongRecord.getClassSchema());
                  // ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                  // Encoder encoder = EncoderFactory.get().directBinaryEncoder(byteStream, /*reuse=*/ null);
                  // w.write(r, encoder);
                  // byte[] bytes = byteStream.toByteArray();

                  // NullTest nt = new NullTest();
                  // nt.setStrField("HI");
                  // nt.setEnumField(Suit.SPADES);

                  // AddressRecord ar = new AddressRecord();
                  // ar.setStreetNumber(30);
                  // nt.setRecordField(ar);

                  // List<java.lang.CharSequence> strs = new ArrayList<java.lang.CharSequence>();
                  // strs.add("ABC");

                  // nt.setArrayField(strs);

                  // ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                  // Encoder encoder = EncoderFactory.get().jsonEncoder(NullTest.getClassSchema(), byteStream);
                  // nt.customEncode(encoder);
                  // encoder.flush();
                  // System.out.println(byteStream.toString());

                  // PubsubMessage builder =
                  //     PubsubMessage.newBuilder()
                  //         .setData(ByteString.copyFrom(bytes))
                  //         //.setData(ByteString.copyFromUtf8(jsonString))
                  //         .putAttributes(MESSAGE_SEQUENCE_NUMBER_KEY, messageSequenceNumber)
                  //         .putAttributes(INSTANCE_ATTRIBUTE, instanceId)
                  //         .build();
                  // publishFutures.add(publish(publisher, builder, false));
                  } catch (Exception e) {
                     logger.log(Level.INFO, "NO ENCODE ", e);
                     return;
                   }
                  // ApiFuture<String> publishFuture = publish(publisher, builder, filteredOut);
                  // publishFuture.addListener(
                  //     () -> {
                  //       try {
                  //         DateTime publishAckTime = DateTime.now();
                  //         Duration publishLatency = new Duration(sendTime, publishAckTime);
                  //         String messageId = publishFuture.get();
                  //         logger.fine(
                  //             "Published " + messageId + " in " + publishLatency.getMillis() + "ms");
                  //         long currentPublishCount = publishCount.incrementAndGet();
                  //         if (currentPublishCount % 10000 == 0) {
                  //           logger.info(
                  //               String.format(
                  //                   "Successfully published %d messages.", currentPublishCount));
                  //         }
                  //       } catch (InterruptedException | ExecutionException e) {
                  //         logger.log(Level.WARNING, "Failed to publish", e);
                  //         messageSendTime.remove(messageSequenceNumber);
                  //       }
                  //     },
                  //     executor);
                }
                ApiFuture<List<String>> allFutures = ApiFutures.allAsList(publishFutures);
                allFutures.addListener(
                    () -> {
                      try {
                        allFutures.get();
                        long currentPublishCount = publishCount.addAndGet(publishMultiplier);
                        if (currentPublishCount % 10 == 0) {
                          logger.info(
                              String.format(
                                  "Successfully published %d messages.", currentPublishCount));
                        }
                      } catch (InterruptedException | ExecutionException e) {
                        logger.log(Level.WARNING, "Failed to publish", e);
                      }
                    },
                    executor);
              } catch (Exception e) {
                logger.log(Level.WARNING, "Failed to publish", e);
              }
            },
            0,
            publishFrequency,
            MICROSECONDS);
  }
}
