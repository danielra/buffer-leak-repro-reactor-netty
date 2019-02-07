package com.example.demo;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.netty.http.client.HttpClient;
import org.springframework.web.reactive.function.client.WebClient;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DemoApplicationTests {

	@Rule
	public WireMockRule wireMockRule = new WireMockRule(9005);

	@Test
	public void contextLoads() {
	}

	private static final String TEST_CONTENT = "Test body content Test body content Test body content Test body content Test body content Test body content Test body content Test body content Test body content Test body content Test body content Test body content";

	@Test
	public void noReproBufferLeakOnTimeout() {
		stubFor(get("/testEndpoint/").willReturn(
					aResponse()
						.withStatus(200)
						.withBody(TEST_CONTENT)
						.withChunkedDribbleDelay(TEST_CONTENT.length()/10, 40)));

		for(int i = 0; i < 20; ++i) {
			try {
				String responseContent = HttpClient.create()
												.get()
												.uri("http://localhost:9005/testEndpoint/")
												.responseContent()
												.aggregate()
												.asString()
												.timeout(Duration.ofMillis(ThreadLocalRandom.current().nextInt(1, 35)))
												.block();
				System.out.println("HERE: " + responseContent);
			} catch (RuntimeException re) {
				System.out.println("HERE: timeout exception: " + re.toString());
			}
		}
		System.gc();
		for(int i = 0; i < 100000; ++i) {
			int[] arr = new int[100000];
		}
		System.gc();
	}

	@Test
	public void reproBufferLeakOnTimeout() {
		stubFor(get("/testEndpoint/").willReturn(
					aResponse()
						.withStatus(200)
						.withBody(TEST_CONTENT)
						.withChunkedDribbleDelay(TEST_CONTENT.length()/10, 40)));

		for(int i = 0; i < 20; ++i) {
			try {
				String responseContent = WebClient.create()
												.get()
												.uri("http://localhost:9005/testEndpoint/")
												.retrieve()
												.bodyToMono(String.class)
												.timeout(Duration.ofMillis(ThreadLocalRandom.current().nextInt(1, 35)))
												.block();
				System.out.println("HERE: " + responseContent);
			} catch (RuntimeException re) {
				System.out.println("HERE: timeout exception: " + re.toString());
			}
		}
		System.gc();
		for(int i = 0; i < 100000; ++i) {
			int[] arr = new int[100000];
		}
		System.gc();
	}

}
