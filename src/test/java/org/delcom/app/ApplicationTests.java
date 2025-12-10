package org.delcom.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

class ApplicationTests {

	@Test
	@DisplayName("Main method should run Spring Application")
	void mainMethod_ShouldRunSpringApplication() {
		// Mock SpringApplication.run to prevent actual startup
		try (var mockedSpring = mockStatic(SpringApplication.class)) {
			ConfigurableApplicationContext mockContext = mock(ConfigurableApplicationContext.class);
			mockedSpring.when(() -> SpringApplication.run(Application.class, new String[] {}))
					.thenReturn(mockContext);

			// Execute main method
			assertDoesNotThrow(() -> Application.main(new String[] {}));

			// Verify
			mockedSpring.verify(() -> SpringApplication.run(Application.class, new String[] {}));
		}
	}

	@Test
	@DisplayName("Application class should have @SpringBootApplication annotation")
	void applicationClass_ShouldHaveAnnotation() {
		assertNotNull(
				Application.class.getAnnotation(org.springframework.boot.autoconfigure.SpringBootApplication.class));
	}

	@Test
	@DisplayName("Application class should be instantiable")
	void applicationClass_ShouldBeInstantiable() {
		assertDoesNotThrow(() -> {
			Application app = new Application();
			assertNotNull(app);
		});
	}
}
