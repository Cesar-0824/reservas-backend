package com.canchas.reservas;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")  // 👈 Esto le dice a Spring que use application-test.properties
class ReservasApplicationTests {

	@Test
	void contextLoads() {
	}
}
